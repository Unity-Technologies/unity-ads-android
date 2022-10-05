package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.configuration.IExperiments;
import com.unity3d.services.core.configuration.InitRequestType;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.Storage;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.device.reader.pii.PiiDataProvider;
import com.unity3d.services.core.device.reader.pii.PiiDataSelector;
import com.unity3d.services.core.device.reader.pii.PiiTrackingStatusReader;
import com.unity3d.services.core.lifecycle.CachedLifecycle;
import com.unity3d.services.core.misc.IJsonStorageReader;
import com.unity3d.services.core.misc.JsonFlattenerRules;
import com.unity3d.services.core.misc.JsonStorageAggregator;

import java.util.Arrays;
import java.util.Collections;

public class DeviceInfoReaderBuilder {
	private final ConfigurationReader _configurationReader;
	private final PrivacyConfigStorage _privacyConfigStorage;
	private final IGameSessionIdReader _gameSessionIdReader;

	public DeviceInfoReaderBuilder(ConfigurationReader configurationReader, PrivacyConfigStorage privacyConfigStorage, IGameSessionIdReader gameSessionIdReader) {
		_configurationReader = configurationReader;
		_privacyConfigStorage = privacyConfigStorage;
		_gameSessionIdReader = gameSessionIdReader;
	}

	public IDeviceInfoReader build() {
		Storage privateStorage = StorageManager.getStorage(StorageManager.StorageType.PRIVATE);
		Storage publicStorage = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		JsonStorageAggregator storageAggregator = new JsonStorageAggregator(Arrays.<IJsonStorageReader>asList(publicStorage, privateStorage));
		DeviceInfoReaderFilterProvider deviceInfoReaderFilterProvider = new DeviceInfoReaderFilterProvider(privateStorage);
		DeviceInfoReaderWithLifecycle deviceInfoReaderWithLifecycle = new DeviceInfoReaderWithLifecycle(new DeviceInfoReaderExtended(buildWithRequestType(InitRequestType.TOKEN)), CachedLifecycle.getLifecycleListener());
		DeviceInfoReaderWithStorageInfo deviceInfoReaderWithStorageInfo = new DeviceInfoReaderWithStorageInfo(deviceInfoReaderWithLifecycle, getTsiRequestStorageRules(), privateStorage, publicStorage);

		IDeviceInfoReader deviceInfoReaderPrivacyDecorated;
		PiiTrackingStatusReader piiTrackingStatusReader = new PiiTrackingStatusReader(storageAggregator);
		if (_privacyConfigStorage != null && getCurrentExperiments().isPrivacyRequestEnabled()) {
			deviceInfoReaderPrivacyDecorated = new DeviceInfoReaderWithPrivacy(deviceInfoReaderWithStorageInfo, _privacyConfigStorage, new PiiDataProvider(), piiTrackingStatusReader);
		} else {
			PiiDataSelector piiDataSelector = new PiiDataSelector(piiTrackingStatusReader, privateStorage, getCurrentExperiments());
			deviceInfoReaderPrivacyDecorated = new DeviceInfoReaderWithPII(deviceInfoReaderWithStorageInfo, piiDataSelector, new PiiDataProvider());
		}
		DeviceInfoReaderWithFilter deviceInfoReaderWithFilter = new DeviceInfoReaderWithFilter(deviceInfoReaderPrivacyDecorated, deviceInfoReaderFilterProvider.getFilterList());
		return new DeviceInfoReaderWithMetrics(deviceInfoReaderWithFilter);
	}

	protected IDeviceInfoReader buildWithRequestType(InitRequestType initRequestType) {
		return new DeviceInfoReaderWithRequestType(new MinimalDeviceInfoReader(_gameSessionIdReader), initRequestType);
	}

	private IExperiments getCurrentExperiments() {
		if (_configurationReader == null || _configurationReader.getCurrentConfiguration() == null) return new Experiments();
		return _configurationReader.getCurrentConfiguration().getExperiments();
	}

	private JsonFlattenerRules getTsiRequestStorageRules() {
		return new JsonFlattenerRules(Arrays.asList(
			"privacy",
			"gdpr",
			"framework",
			"adapter",
			"mediation",
			"unity",
			"pipl",
			"configuration",
			"user",
			"unifiedconfig"
		),
			Collections.singletonList("value"),
			Arrays.asList(
				"ts",
				"exclude",
				"pii",
				"nonBehavioral",
				"nonbehavioral"
			)
		);
	}



}
