package com.unity3d.services.core.device.reader.builder;

import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.InitRequestType;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.Storage;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.device.reader.DeviceInfoReaderFilterProvider;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithBehavioralFlag;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithFilter;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithStorageInfo;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.device.reader.IGameSessionIdReader;
import com.unity3d.services.core.device.reader.pii.NonBehavioralFlagReader;
import com.unity3d.services.core.misc.JsonFlattenerRules;
import com.unity3d.services.core.misc.JsonStorageAggregator;

import java.util.Arrays;
import java.util.Collections;

public class DeviceInfoReaderPrivacyBuilder extends DeviceInfoReaderBuilder {
	public DeviceInfoReaderPrivacyBuilder(ConfigurationReader configurationReader, PrivacyConfigStorage privacyConfigStorage, IGameSessionIdReader gameSessionIdReader) {
		super(configurationReader, privacyConfigStorage, gameSessionIdReader);
	}

	@Override
	public IDeviceInfoReader build() {
		Storage privateStorage = StorageManager.getStorage(StorageManager.StorageType.PRIVATE);
		Storage publicStorage = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);
		JsonStorageAggregator storageAggregator = new JsonStorageAggregator(Arrays.asList(publicStorage, privateStorage));
		NonBehavioralFlagReader nonBehavioralFlagReader = new NonBehavioralFlagReader(storageAggregator);
		DeviceInfoReaderFilterProvider deviceInfoReaderFilterProvider = new DeviceInfoReaderFilterProvider(privateStorage);
		IDeviceInfoReader deviceInfoReader = buildWithRequestType(InitRequestType.PRIVACY);
		DeviceInfoReaderWithBehavioralFlag deviceInfoReaderWithBehavioralFlag = new DeviceInfoReaderWithBehavioralFlag(deviceInfoReader, nonBehavioralFlagReader);
		DeviceInfoReaderWithStorageInfo deviceInfoReaderWithStorageInfo = new DeviceInfoReaderWithStorageInfo(deviceInfoReaderWithBehavioralFlag, getPrivacyRequestStorageRules(), privateStorage, publicStorage);
		return new DeviceInfoReaderWithFilter(deviceInfoReaderWithStorageInfo, deviceInfoReaderFilterProvider.getFilterList());
	}

	private JsonFlattenerRules getPrivacyRequestStorageRules() {
		return new JsonFlattenerRules(Arrays.asList(
			"privacy",
			"gdpr",
			"unity",
			"pipl"
		),
			Collections.singletonList("value"),
			Arrays.asList(
				"ts",
				"exclude",
				"mode"
			)
		);
	}
}
