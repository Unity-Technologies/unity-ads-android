package com.unity3d.services.core.request.metrics;

import java.util.Map;

public class SDKMetricSender implements ISDKMetricSender {
	public void sendSDKMetricEvent(SDKMetricEvents metricEvent) {
		if (metricEvent == null) return;
		ISDKMetrics sdkMetric = SDKMetrics.getInstance();
		if (sdkMetric == null) return;
		sdkMetric.sendEvent(metricEvent.toString());
	}

	public void sendSDKMetricEventWithTag(SDKMetricEvents metricEvent, Map<String, String> tags) {
		if (metricEvent == null) return;
		ISDKMetrics sdkMetric = SDKMetrics.getInstance();
		if (sdkMetric == null) return;
		sdkMetric.sendEvent(metricEvent.toString(), tags);
	}
}
