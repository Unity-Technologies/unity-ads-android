package com.unity3d.services.core.device.reader;

import java.util.List;
import java.util.Map;

public class DeviceInfoReaderWithFilter implements IDeviceInfoReader {
	IDeviceInfoReader _deviceInfoReader;
	List<String> _keysToExclude;

	public DeviceInfoReaderWithFilter(IDeviceInfoReader deviceInfoReader, List<String> keysToExclude) {
		_deviceInfoReader = deviceInfoReader;
		_keysToExclude = keysToExclude;
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> filteredDeviceInfoData = _deviceInfoReader.getDeviceInfoData();
		if (_keysToExclude != null) {
			for (String keyToExclude : _keysToExclude) {
				filteredDeviceInfoData.remove(keyToExclude);
			}
		}
		return filteredDeviceInfoData;
	}
}
