package com.unity3d.services.core.device.reader;


import com.unity3d.services.core.lifecycle.LifecycleCache;

import java.util.Map;

public class DeviceInfoReaderWithLifecycle implements IDeviceInfoReader {

	private final IDeviceInfoReader _deviceInfoReader;
	private final LifecycleCache _lifecycleCache;

	public DeviceInfoReaderWithLifecycle(IDeviceInfoReader deviceInfoReader, LifecycleCache lifecycleCache) {
		_deviceInfoReader = deviceInfoReader;
		_lifecycleCache = lifecycleCache;
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> deviceInfoData = _deviceInfoReader.getDeviceInfoData();
		deviceInfoData.put("appActive", _lifecycleCache.isAppActive());
		return deviceInfoData;
	}

}
