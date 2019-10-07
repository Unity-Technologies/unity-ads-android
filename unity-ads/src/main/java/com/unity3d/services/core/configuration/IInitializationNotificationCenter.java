package com.unity3d.services.core.configuration;

public interface IInitializationNotificationCenter {

	void addListener(IInitializationListener listener);

	void removeListener(IInitializationListener listener);

	void triggerOnSdkInitialized();

	void triggerOnSdkInitializationFailed(String message, int code);
}
