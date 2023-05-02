package com.unity3d.services.core.configuration;

public interface IPrivacyConfigurationListener {
	void onSuccess(PrivacyConfig privacyConfig);
	void onError(PrivacyCallError privacyCallError, String errorMsg);
}
