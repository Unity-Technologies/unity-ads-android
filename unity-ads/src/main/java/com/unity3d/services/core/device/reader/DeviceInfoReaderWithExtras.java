package com.unity3d.services.core.device.reader;

import java.util.Map;

public class DeviceInfoReaderWithExtras implements IDeviceInfoReader {

	private final IDeviceInfoReader _deviceInfoReader;
	private final Map<String, String>  _extras;

	public DeviceInfoReaderWithExtras(IDeviceInfoReader deviceInfoReader, Map<String, String> extras) {
		_deviceInfoReader = deviceInfoReader;
		_extras = extras;
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> deviceInfoData = _deviceInfoReader.getDeviceInfoData();
		if (deviceInfoData != null && _extras != null) {
			deviceInfoData.putAll(_extras);
		}
		return deviceInfoData;
	}
}
