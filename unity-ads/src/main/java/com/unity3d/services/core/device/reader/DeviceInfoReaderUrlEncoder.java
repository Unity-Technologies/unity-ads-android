package com.unity3d.services.core.device.reader;

import android.util.Base64;

import com.unity3d.services.core.log.DeviceLog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class DeviceInfoReaderUrlEncoder {
	private final IDeviceInfoDataContainer _deviceInfoDataContainer;

	public DeviceInfoReaderUrlEncoder(IDeviceInfoDataContainer deviceInfoDataContainer) {
		_deviceInfoDataContainer = deviceInfoDataContainer;
	}

	public String getUrlEncodedData() {
		byte[] rawData = _deviceInfoDataContainer.getDeviceData();
		String queryStringData = "";
		try {
			queryStringData = URLEncoder.encode(Base64.encodeToString(rawData, Base64.NO_WRAP), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			DeviceLog.error("Could not encode device data using UTF-8.");
		}
		return queryStringData;
	}

}
