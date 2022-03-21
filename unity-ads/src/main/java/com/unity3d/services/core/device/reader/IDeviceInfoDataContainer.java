package com.unity3d.services.core.device.reader;

import java.util.Map;

public interface IDeviceInfoDataContainer {
	byte[] getDeviceData();
	Map<String, Object> getDeviceInfo();
}
