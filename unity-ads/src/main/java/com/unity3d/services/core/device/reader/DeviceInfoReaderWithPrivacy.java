package com.unity3d.services.core.device.reader;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.ADVERTISING_TRACKING_ID_NORMALIZED_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_KEY;

import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.reader.pii.PiiDataProvider;
import com.unity3d.services.core.device.reader.pii.PiiTrackingStatusReader;

import java.util.HashMap;
import java.util.Map;

public class DeviceInfoReaderWithPrivacy implements IDeviceInfoReader {
	private final IDeviceInfoReader _deviceInfoReader;
	private final PrivacyConfigStorage _privacyConfigStorage;
	private final PiiDataProvider _piiDataProvider;
	private final PiiTrackingStatusReader _piiTrackingStatusReader;

	public DeviceInfoReaderWithPrivacy(IDeviceInfoReader deviceInfoReader, PrivacyConfigStorage privacyConfigStorage, PiiDataProvider piiDataProvider, PiiTrackingStatusReader piiTrackingStatusReader) {
		_deviceInfoReader = deviceInfoReader;
		_privacyConfigStorage = privacyConfigStorage;
		_piiDataProvider = piiDataProvider;
		_piiTrackingStatusReader = piiTrackingStatusReader;
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> originalData = _deviceInfoReader.getDeviceInfoData();
		if (_privacyConfigStorage != null && _privacyConfigStorage.getPrivacyConfig() != null) {
			if (_privacyConfigStorage.getPrivacyConfig().allowedToSendPii()) {
				originalData.putAll(getPiiAttributesFromDevice());
			}
			if (_privacyConfigStorage.getPrivacyConfig().shouldSendNonBehavioral()) {
				originalData.put(USER_NON_BEHAVIORAL_KEY, _piiTrackingStatusReader.getUserNonBehavioralFlag());
			}
		}
		return originalData;
	}

	private Map<String, Object> getPiiAttributesFromDevice() {
		Map<String, Object> piiData = new HashMap<>();
		Object advertisingId = _piiDataProvider.getAdvertisingTrackingId();
		if (advertisingId != null) {
			piiData.put(ADVERTISING_TRACKING_ID_NORMALIZED_KEY, advertisingId);
		}
		return piiData;
	}

}
