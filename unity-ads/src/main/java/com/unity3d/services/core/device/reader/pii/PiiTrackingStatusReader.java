package com.unity3d.services.core.device.reader.pii;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.PRIVACY_MODE_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.PRIVACY_SPM_KEY;

import com.unity3d.services.core.misc.IJsonStorageReader;

public class PiiTrackingStatusReader {

	private final IJsonStorageReader _jsonStorageReader;
	private final NonBehavioralFlagReader _nonBehavioralFlagReader;

	public PiiTrackingStatusReader(IJsonStorageReader jsonStorageReader) {
		_jsonStorageReader = jsonStorageReader;
		_nonBehavioralFlagReader = new NonBehavioralFlagReader(jsonStorageReader);
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
		// This is to keep the previous behavior of this method. (where unknown and false both returned false)
		return _nonBehavioralFlagReader.getUserNonBehavioralFlag() == NonBehavioralFlag.TRUE;
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
