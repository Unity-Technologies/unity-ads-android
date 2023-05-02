package com.unity3d.services.core.configuration;

import org.json.JSONObject;

public class PrivacyConfig {
	private PrivacyConfigStatus _privacyConfigStatus;
	private boolean _shouldSendNonBehavioral;

	public PrivacyConfig() {
		this(PrivacyConfigStatus.UNKNOWN);
	}

	public PrivacyConfig(JSONObject privacyResponse) {
		parsePrivacyResponse(privacyResponse);
	}

	public PrivacyConfig(PrivacyConfigStatus privacyConfigStatus) {
		_privacyConfigStatus = privacyConfigStatus;
		_shouldSendNonBehavioral = false;
	}

	public boolean allowedToSendPii() {
		return _privacyConfigStatus.equals(PrivacyConfigStatus.ALLOWED);
	}

	public boolean shouldSendNonBehavioral() {
		return _shouldSendNonBehavioral;
	}

	public PrivacyConfigStatus getPrivacyStatus() {
		return _privacyConfigStatus;
	}

	private void parsePrivacyResponse(JSONObject privacyResponse) {
		_privacyConfigStatus = privacyResponse.optBoolean("pas", false) ? PrivacyConfigStatus.ALLOWED : PrivacyConfigStatus.DENIED;
		_shouldSendNonBehavioral = privacyResponse.optBoolean("snb", false);
	}
}
