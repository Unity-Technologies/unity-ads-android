package com.unity3d.services.core.device.reader;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_KEY;

import com.unity3d.services.core.device.reader.pii.NonBehavioralFlag;
import com.unity3d.services.core.device.reader.pii.NonBehavioralFlagReader;
import com.unity3d.services.core.device.reader.pii.PiiTrackingStatusReader;

import java.util.Map;

public class DeviceInfoReaderWithBehavioralFlag implements IDeviceInfoReader {
	private final IDeviceInfoReader _deviceInfoReader;
	private final NonBehavioralFlagReader _nonBehavioralFlagReader;

	public DeviceInfoReaderWithBehavioralFlag(IDeviceInfoReader deviceInfoReader, NonBehavioralFlagReader nonBehavioralFlagReader) {
		_deviceInfoReader = deviceInfoReader;
		_nonBehavioralFlagReader = nonBehavioralFlagReader;
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> originalData = _deviceInfoReader.getDeviceInfoData();
		if (_nonBehavioralFlagReader.getUserNonBehavioralFlag() != NonBehavioralFlag.UNKNOWN) {
			// If its UNKNOWN, simply omit from adding the flag to the payload
			originalData.put(USER_NON_BEHAVIORAL_KEY, _nonBehavioralFlagReader.getUserNonBehavioralFlag() == NonBehavioralFlag.TRUE);
		}
		return originalData;
	}

}
