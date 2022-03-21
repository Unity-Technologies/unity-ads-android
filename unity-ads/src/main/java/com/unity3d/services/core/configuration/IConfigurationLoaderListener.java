package com.unity3d.services.core.configuration;

public interface IConfigurationLoaderListener {
	void onSuccess(Configuration configuration);
	void onError(String errorMsg);
}
