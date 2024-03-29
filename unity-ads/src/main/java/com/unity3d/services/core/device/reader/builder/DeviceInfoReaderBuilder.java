package com.unity3d.services.core.device.reader.builder;

import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.InitRequestType;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.Storage;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.device.reader.DeviceInfoReaderExtended;
import com.unity3d.services.core.device.reader.DeviceInfoReaderFilterProvider;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithAuid;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithFilter;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithLifecycle;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithMetrics;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithPrivacy;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithRequestType;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithSessionId;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithStorageInfo;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.device.reader.IGameSessionIdReader;
import com.unity3d.services.core.device.reader.MinimalDeviceInfoReader;
import com.unity3d.services.core.device.reader.pii.PiiDataProvider;
import com.unity3d.services.core.device.reader.pii.PiiTrackingStatusReader;
import com.unity3d.services.core.lifecycle.CachedLifecycle;
import com.unity3d.services.core.misc.JsonFlattenerRules;
import com.unity3d.services.core.misc.JsonStorageAggregator;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.Session;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;

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
		JsonStorageAggregator storageAggregator = new JsonStorageAggregator(Arrays.asList(publicStorage, privateStorage));
		DeviceInfoReaderFilterProvider deviceInfoReaderFilterProvider = new DeviceInfoReaderFilterProvider(privateStorage);
		IDeviceInfoReader deviceInfoReader = buildWithRequestType(InitRequestType.TOKEN);
		DeviceInfoReaderWithSessionId deviceInfoReaderWithSessionId = new DeviceInfoReaderWithSessionId(deviceInfoReader, Session.Default);
		DeviceInfoReaderWithAuid deviceInfoReaderWithAuid = new DeviceInfoReaderWithAuid(deviceInfoReaderWithSessionId);
		DeviceInfoReaderWithLifecycle deviceInfoReaderWithLifecycle = new DeviceInfoReaderWithLifecycle(new DeviceInfoReaderExtended(deviceInfoReaderWithAuid), CachedLifecycle.getLifecycleListener());
		DeviceInfoReaderWithStorageInfo deviceInfoReaderWithStorageInfo = new DeviceInfoReaderWithStorageInfo(deviceInfoReaderWithLifecycle, getTsiRequestStorageRules(), privateStorage, publicStorage);

		PiiTrackingStatusReader piiTrackingStatusReader = new PiiTrackingStatusReader(storageAggregator);
		IDeviceInfoReader deviceInfoReaderPrivacyDecorated = new DeviceInfoReaderWithPrivacy(deviceInfoReaderWithStorageInfo, _privacyConfigStorage, new PiiDataProvider(), piiTrackingStatusReader);

		DeviceInfoReaderWithFilter deviceInfoReaderWithFilter = new DeviceInfoReaderWithFilter(deviceInfoReaderPrivacyDecorated, deviceInfoReaderFilterProvider.getFilterList());
		SDKMetricsSender sdkMetricsSender = Utilities.getService(SDKMetricsSender.class);
		return new DeviceInfoReaderWithMetrics(deviceInfoReaderWithFilter, sdkMetricsSender);
	}

	protected IDeviceInfoReader buildWithRequestType(InitRequestType initRequestType) {
		return new DeviceInfoReaderWithRequestType(new MinimalDeviceInfoReader(_gameSessionIdReader), initRequestType);
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
