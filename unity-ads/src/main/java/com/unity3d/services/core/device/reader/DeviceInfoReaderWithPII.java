package com.unity3d.services.core.device.reader;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.ADVERTISING_TRACKING_ID_NORMALIZED_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_KEY;

import com.unity3d.services.core.device.reader.pii.DataSelectorResult;
import com.unity3d.services.core.device.reader.pii.PiiDataProvider;
import com.unity3d.services.core.device.reader.pii.PiiDataSelector;
import com.unity3d.services.core.device.reader.pii.PiiDecisionData;

import java.util.HashMap;
import java.util.Map;

public class DeviceInfoReaderWithPII implements IDeviceInfoReader {
	private IDeviceInfoReader _deviceInfoReader;
	private PiiDataProvider _piiDataProvider;
	private PiiDataSelector _piiDataSelector;

	public DeviceInfoReaderWithPII(IDeviceInfoReader deviceInfoReader, PiiDataSelector piiDataSelector, PiiDataProvider piiDataProvider) {
		_deviceInfoReader = deviceInfoReader;
		_piiDataSelector = piiDataSelector;
		_piiDataProvider = piiDataProvider;
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> originalData = _deviceInfoReader.getDeviceInfoData();
		PiiDecisionData piiDecisionData = _piiDataSelector.whatToDoWithPII();
		DataSelectorResult dataSelectorResult = piiDecisionData.getResultType();
		switch (dataSelectorResult) {
			case INCLUDE:
				originalData.putAll(getPiiAttributesFromStorage(piiDecisionData));
				break;
			case UPDATE:
				originalData.putAll(getPiiAttributesFromDevice(piiDecisionData));
				break;
			case EXCLUDE:
				return originalData;
		}
		return originalData;
	}

	private Map<String, Object> getPiiAttributesFromStorage(PiiDecisionData decisionData) {
		return decisionData.getAttributes();
	}

	private Map<String, Object> getPiiAttributesFromDevice(PiiDecisionData decisionData) {
		Map<String, Object> piiData = new HashMap<>();
		Object advertisingId = _piiDataProvider.getAdvertisingTrackingId();
		if (advertisingId != null) {
			piiData.put(ADVERTISING_TRACKING_ID_NORMALIZED_KEY, advertisingId);
		}
		if (decisionData.getUserNonBehavioralFlag() != null) {
			piiData.put(USER_NON_BEHAVIORAL_KEY, decisionData.getUserNonBehavioralFlag());
		}
		return piiData;
	}
}
