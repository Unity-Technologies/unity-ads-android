package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;

import java.util.Map;

public class DeviceInfoReaderBuilderWithExtras extends DeviceInfoReaderBuilder {

	private Map<String, String> _extras;

	public DeviceInfoReaderBuilderWithExtras(ConfigurationReader configurationReader, PrivacyConfigStorage privacyConfigStorage, IGameSessionIdReader gameSessionIdReader) {
		super(configurationReader, privacyConfigStorage, gameSessionIdReader);
	}

	public void setExtras(Map<String, String> extras) {
		_extras = extras;
	}

	@Override
	public IDeviceInfoReader build() {
		if (_extras == null) {
			return super.build();
		}
		return new DeviceInfoReaderWithExtras(super.build(), _extras);
	}
}
