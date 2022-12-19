package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.InitRequestType;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;

public class DeviceInfoDataFactory {
	public IDeviceInfoDataContainer getDeviceInfoData(InitRequestType callType) {
		switch(callType) {
			case TOKEN:
				return getTokenDeviceInfoData();
			case PRIVACY:
				return getPrivacyDeviceInfoData();
			default:
				return null;
		}
	}

	private IDeviceInfoDataContainer getPrivacyDeviceInfoData() {
		PrivacyConfigStorage privacyConfigStorage = PrivacyConfigStorage.getInstance();
		DeviceInfoReaderBuilder deviceInfoReaderPrivacyBuilder = new DeviceInfoReaderPrivacyBuilder(new ConfigurationReader(), privacyConfigStorage, GameSessionIdReader.getInstance());
		return new DeviceInfoReaderCompressor(deviceInfoReaderPrivacyBuilder.build());
	}

	private IDeviceInfoDataContainer getTokenDeviceInfoData() {
		PrivacyConfigStorage privacyConfigStorage = PrivacyConfigStorage.getInstance();
		DeviceInfoReaderBuilder deviceInfoReaderBuilder = new DeviceInfoReaderBuilder(new ConfigurationReader(), privacyConfigStorage, GameSessionIdReader.getInstance());
		return new DeviceInfoReaderCompressorWithMetrics(new DeviceInfoReaderCompressor(deviceInfoReaderBuilder.build()));
	}
}
