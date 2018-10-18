package com.unity3d.services.core.configuration;

public interface IModuleConfiguration {
	Class[] getWebAppApiClassList();
	boolean resetState(Configuration configuration);
	boolean initModuleState(Configuration configuration);
	boolean initErrorState(Configuration configuration, String state, String message);
	boolean initCompleteState(Configuration configuration);
}
