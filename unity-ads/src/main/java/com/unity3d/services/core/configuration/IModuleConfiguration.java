package com.unity3d.services.core.configuration;

public interface IModuleConfiguration {
	Class[] getWebAppApiClassList();
	boolean resetState(Configuration configuration);
	boolean initErrorState(Configuration configuration, ErrorState state, String message);
	boolean initCompleteState(Configuration configuration);
}
