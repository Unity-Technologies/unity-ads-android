package com.unity3d.services.core.request.metrics;

import java.util.Map;

public interface ISDKMetricSender {
	void sendSDKMetricEvent(SDKMetricEvents metricEvent);
	void sendSDKMetricEventWithTag(SDKMetricEvents metricEvent, Map<String, String> tags);
}
