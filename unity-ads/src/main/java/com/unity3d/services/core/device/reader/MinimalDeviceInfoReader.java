package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;

import java.util.HashMap;
import java.util.Map;

public class MinimalDeviceInfoReader implements IDeviceInfoReader {

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> deviceInfoData = new HashMap<>();
		deviceInfoData.put("platform", "android");
		deviceInfoData.put("sdkVersion", SdkProperties.getVersionCode());
		deviceInfoData.put("sdkVersionName", SdkProperties.getVersionName());
		deviceInfoData.put("idfi", Device.getIdfi());

		// Misc
		deviceInfoData.put("ts", System.currentTimeMillis());
		deviceInfoData.put("gameId", ClientProperties.getGameId());
		return deviceInfoData;
	}
	
}