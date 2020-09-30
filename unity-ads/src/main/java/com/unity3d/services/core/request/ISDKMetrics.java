package com.unity3d.services.core.request;

import java.util.Map;

public interface ISDKMetrics {
	void sendEvent(final String event);
	void sendEventWithTags(final String event, final Map<String, String> tags);
}
