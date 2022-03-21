package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.device.Storage;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.device.reader.pii.PiiDataProvider;
import com.unity3d.services.core.device.reader.pii.PiiDataSelector;
import com.unity3d.services.core.device.reader.pii.PiiTrackingStatusReader;
import com.unity3d.services.core.lifecycle.CachedLifecycle;
import com.unity3d.services.core.misc.IJsonStorageReader;
import com.unity3d.services.core.misc.JsonStorageAggregator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DeviceInfoReaderBuilder {
	private ConfigurationReader _configurationReader;

	public DeviceInfoReaderBuilder(ConfigurationReader configurationReader) {
		_configurationReader = configurationReader;
	}

	public IDeviceInfoReader build() {
		Storage privateStorage = StorageManager.getStorage(StorageManager.StorageType.PRIVATE);
		Storage publicStorage = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		JsonStorageAggregator storageAggregator = new JsonStorageAggregator(Arrays.<IJsonStorageReader>asList(publicStorage, privateStorage));
		DeviceInfoReaderFilterProvider deviceInfoReaderFilterProvider = new DeviceInfoReaderFilterProvider(privateStorage);
		DeviceInfoReaderWithLifecycle deviceInfoReaderWithLifecycle = new DeviceInfoReaderWithLifecycle(new DeviceInfoReader(), CachedLifecycle.getLifecycleListener());
		DeviceInfoReaderWithStorageInfo deviceInfoReaderWithStorageInfo = new DeviceInfoReaderWithStorageInfo(deviceInfoReaderWithLifecycle, privateStorage, publicStorage);
		PiiTrackingStatusReader piiTrackingStatusReader = new PiiTrackingStatusReader(storageAggregator);
		PiiDataSelector piiDataSelector = new PiiDataSelector(piiTrackingStatusReader, privateStorage, getCurrentExperiments());
		DeviceInfoReaderWithPII deviceInfoReaderWithPII = new DeviceInfoReaderWithPII(deviceInfoReaderWithStorageInfo, piiDataSelector, new PiiDataProvider());
        DeviceInfoReaderWithFilter deviceInfoReaderWithFilter = new DeviceInfoReaderWithFilter(deviceInfoReaderWithPII, deviceInfoReaderFilterProvider.getFilterList());
		return new DeviceInfoReaderWithMetrics(deviceInfoReaderWithFilter, getCurrentMetricTags());
	}

	private Experiments getCurrentExperiments() {
		if (_configurationReader == null || _configurationReader.getCurrentConfiguration() == null) return new Experiments();
		return _configurationReader.getCurrentConfiguration().getExperiments();
	}

	private Map<String, String> getCurrentMetricTags() {
		if (_configurationReader == null || _configurationReader.getCurrentConfiguration() == null) return new HashMap<>();
		return _configurationReader.getCurrentConfiguration().getMetricTags();
	}
}
