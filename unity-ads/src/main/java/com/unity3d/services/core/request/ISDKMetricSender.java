package com.unity3d.services.core.request;

import java.util.HashMap;

public interface ISDKMetricSender {
	void SendSDKMetricEvent(SDKMetricEvents metricEvent);
	void SendSDKMetricEventWithTag(SDKMetricEvents metricEvent, HashMap<String, String> tags);
}
