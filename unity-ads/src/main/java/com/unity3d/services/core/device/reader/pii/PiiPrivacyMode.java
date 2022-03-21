package com.unity3d.services.core.device.reader.pii;

import java.util.Locale;

public enum PiiPrivacyMode {
	APP,
	NONE,
	MIXED,
	UNDEFINED,
	NULL;

	public static PiiPrivacyMode getPiiPrivacyMode(String privacyModeStr) {
		if (privacyModeStr == null) return NULL;
		PiiPrivacyMode piiPrivacyMode = UNDEFINED;
		try {
			piiPrivacyMode = valueOf(privacyModeStr.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException ex) {
			// If it can't find the value, it will default to UNDEFINED.
		}
		return piiPrivacyMode;

	}
}
