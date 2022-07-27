package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.configuration.InitRequestType;

import java.util.Map;

public class DeviceInfoReaderWithRequestType implements IDeviceInfoReader {
	private final IDeviceInfoReader _deviceInfoReader;
	private final InitRequestType _initRequestType;

	public DeviceInfoReaderWithRequestType(IDeviceInfoReader deviceInfoReader, InitRequestType initRequestType) {
		_deviceInfoReader = deviceInfoReader;
		_initRequestType = initRequestType;
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> originalData = _deviceInfoReader.getDeviceInfoData();
		if (_initRequestType != null) {
			originalData.put("callType", _initRequestType.toString().toLowerCase());
		}
		return originalData;
	}
}
