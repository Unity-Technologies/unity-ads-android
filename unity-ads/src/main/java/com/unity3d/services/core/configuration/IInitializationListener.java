package com.unity3d.services.core.configuration;

public interface IInitializationListener {

	void onSdkInitialized();

	void onSdkInitializationFailed(String message, ErrorState errorState, int code);

}
