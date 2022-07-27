package com.unity3d.services.core.configuration;

public interface IConfigurationLoader {
	void loadConfiguration(IConfigurationLoaderListener configurationLoaderListener) throws Exception;
	Configuration getLocalConfiguration();
}
