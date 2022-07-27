package com.unity3d.services.core.configuration;

import org.json.JSONObject;

public class PrivacyConfig {
	private final PrivacyConfigStatus _privacyConfigStatus;

	public PrivacyConfig() {
		this(PrivacyConfigStatus.UNKNOWN);
	}

	public PrivacyConfig(JSONObject privacyResponse) {
		_privacyConfigStatus = isPrivacyAllowed(privacyResponse) ? PrivacyConfigStatus.ALLOWED : PrivacyConfigStatus.DENIED;
	}

	public PrivacyConfig(PrivacyConfigStatus privacyConfigStatus) {
		_privacyConfigStatus = privacyConfigStatus;
	}

	public boolean allowedToSendPii() {
		return _privacyConfigStatus.equals(PrivacyConfigStatus.ALLOWED);
	}

	public PrivacyConfigStatus getPrivacyStatus() {
		return _privacyConfigStatus;
	}

	private boolean isPrivacyAllowed(JSONObject responseJson) {
		return responseJson.optBoolean("pas", false);
	}
}
