package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.log.DeviceLog;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class DeviceInfoReaderCompressor implements IDeviceInfoDataCompressor {
	private final IDeviceInfoReader _deviceInfoReader;

	public DeviceInfoReaderCompressor(IDeviceInfoReader deviceInfoReader) {
		_deviceInfoReader = deviceInfoReader;
	}

	@Override
	public byte[] getDeviceData() {
		Map<String, Object> deviceDataMap = getDeviceInfo();
		return compressDeviceInfo(deviceDataMap);
	}

	@Override
	public Map<String, Object> getDeviceInfo() {
		return _deviceInfoReader.getDeviceInfoData();
	}

	@Override
	public byte[] compressDeviceInfo(Map<String, Object> deviceData) {
		byte[] zippedData = null;
		if (deviceData != null) {
			JSONObject jsonData = new JSONObject(deviceData);
			String jsonString = jsonData.toString();
			ByteArrayOutputStream os = new ByteArrayOutputStream(jsonString.length());
			GZIPOutputStream gos;
			try {
				gos = new GZIPOutputStream(os);
				gos.write(jsonString.getBytes());
				gos.flush();
				gos.close();
				os.close();
				zippedData = os.toByteArray();
			} catch (IOException e) {
				DeviceLog.error("Error occurred while trying to compress device data.");
			}
		} else {
			DeviceLog.error("Invalid DeviceInfoData: Expected non null map provided by reader");
		}
		return zippedData;
	}

}
