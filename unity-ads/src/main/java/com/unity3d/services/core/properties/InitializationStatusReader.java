package com.unity3d.services.core.properties;

public class InitializationStatusReader {

	private static final String STATE_NOT_INITIALIZED = "not_initialized";
	private static final String STATE_INITIALIZING = "initializing";
	private static final String STATE_INITIALIZED_SUCCESSFULLY = "initialized_successfully";
	private static final String STATE_INITIALIZED_FAILED = "initialized_failed";

	public String getInitializationStateString(SdkProperties.InitializationState state) {
		switch (state) {
			case NOT_INITIALIZED:
				return STATE_NOT_INITIALIZED;
			case INITIALIZING:
				return STATE_INITIALIZING;
			case INITIALIZED_SUCCESSFULLY:
				return STATE_INITIALIZED_SUCCESSFULLY;
			case INITIALIZED_FAILED:
				return STATE_INITIALIZED_FAILED;
			default:
				return null;
		}
	}
}