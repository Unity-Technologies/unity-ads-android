package com.unity3d.services.core.device.reader;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.SESSION_ID_KEY;

import com.unity3d.services.core.properties.Session;

import java.util.Map;

public class DeviceInfoReaderWithSessionId implements IDeviceInfoReader {
	private final IDeviceInfoReader _deviceInfoReader;
	private final Session _session;

	public DeviceInfoReaderWithSessionId(IDeviceInfoReader deviceInfoReader, Session sessionId) {
		_deviceInfoReader = deviceInfoReader;
		_session = sessionId;
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> originalData = _deviceInfoReader.getDeviceInfoData();
		originalData.put(SESSION_ID_KEY, _session.getId());
		return originalData;
	}

}
