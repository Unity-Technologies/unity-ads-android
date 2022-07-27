package com.unity3d.services.core.configuration;

public enum PrivacyConfigStatus {
	UNKNOWN,
	ALLOWED,
	DENIED;

	public String toLowerCase() {
		return name().toLowerCase();
	}
}
