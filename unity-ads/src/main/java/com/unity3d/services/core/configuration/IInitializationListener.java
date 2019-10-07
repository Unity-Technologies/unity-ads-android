package com.unity3d.services.core.configuration;

public interface IInitializationListener {

	void onSdkInitialized();

	void onSdkInitializationFailed(String message, int code);

}
