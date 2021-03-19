package com.unity3d.services.core.request;

import java.util.HashMap;

public class SDKMetricSender implements ISDKMetricSender {
	public void SendSDKMetricEvent(SDKMetricEvents metricEvent) {
		if (metricEvent == null) return;
		ISDKMetrics sdkMetric = SDKMetrics.getInstance();
		if (sdkMetric == null) return;
		sdkMetric.sendEvent(metricEvent.toString());
	}

	public void SendSDKMetricEventWithTag(SDKMetricEvents metricEvent, HashMap tags) {
		if (metricEvent == null) return;
		ISDKMetrics sdkMetric = SDKMetrics.getInstance();
		if (sdkMetric == null) return;
		sdkMetric.sendEventWithTags(metricEvent.toString(), tags);
	}
}
