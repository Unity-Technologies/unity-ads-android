package com.unity3d.services.core.device.reader.pii;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.PRIVACY_MODE_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.PRIVACY_SPM_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_VALUE_ALT_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_VALUE_KEY;

import com.unity3d.services.core.misc.IJsonStorageReader;

public class PiiTrackingStatusReader {

	private final IJsonStorageReader _jsonStorageReader;

	public PiiTrackingStatusReader(IJsonStorageReader jsonStorageReader) {
		_jsonStorageReader = jsonStorageReader;
	}

	public PiiPrivacyMode getPrivacyMode() {
			if (getUserPrivacyMode() == PiiPrivacyMode.NULL && getSpmPrivacyMode() == PiiPrivacyMode.NULL) {
				return PiiPrivacyMode.NULL;
			}

			if (getUserPrivacyMode() == PiiPrivacyMode.APP || getSpmPrivacyMode() == PiiPrivacyMode.APP) {
				return PiiPrivacyMode.APP;
			}

			if (getUserPrivacyMode() == PiiPrivacyMode.MIXED || getSpmPrivacyMode() == PiiPrivacyMode.MIXED) {
				return PiiPrivacyMode.MIXED;
			}

			if (getUserPrivacyMode() == PiiPrivacyMode.NONE || getSpmPrivacyMode() == PiiPrivacyMode.NONE) {
				return PiiPrivacyMode.NONE;
			}

			return PiiPrivacyMode.UNDEFINED;
	}

	public boolean getUserNonBehavioralFlag() {
		boolean userNonBehavioralFlag = false;
		if (_jsonStorageReader != null) {
			Object privacyModeObj = _jsonStorageReader.get(USER_NON_BEHAVIORAL_VALUE_KEY);
			if (privacyModeObj == null) {
				privacyModeObj = _jsonStorageReader.get(USER_NON_BEHAVIORAL_VALUE_ALT_KEY);
			}
			if (privacyModeObj instanceof String) {
				userNonBehavioralFlag = Boolean.parseBoolean((String)privacyModeObj);
			} else if (privacyModeObj instanceof Boolean) {
				userNonBehavioralFlag = (boolean) privacyModeObj;
			}
		}
		return userNonBehavioralFlag;
	}

	private PiiPrivacyMode getUserPrivacyMode() {
		return getPrivacyMode(PRIVACY_MODE_KEY);
	}

	private PiiPrivacyMode getSpmPrivacyMode() {
		return getPrivacyMode(PRIVACY_SPM_KEY);
	}

	private PiiPrivacyMode getPrivacyMode(String storageKey) {
		String privacyMode = null;
		if (_jsonStorageReader != null) {
			Object privacyModeObj = _jsonStorageReader.get(storageKey);
			if (privacyModeObj instanceof String) {
				privacyMode = (String)privacyModeObj;
			}
		}
		return PiiPrivacyMode.getPiiPrivacyMode(privacyMode);
	}
}
