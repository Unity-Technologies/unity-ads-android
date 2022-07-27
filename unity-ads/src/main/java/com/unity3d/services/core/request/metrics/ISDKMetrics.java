package com.unity3d.services.core.request.metrics;

import java.util.List;
import java.util.Map;

public interface ISDKMetrics {
	boolean areMetricsEnabledForCurrentSession();
	void sendEvent(final String event);
	void sendEvent(final String event, String value, final Map<String, String> tags);
	void sendEvent(final String event, final Map<String, String> tags);
	void sendMetric(final Metric metric);
	void sendMetrics(final List<Metric> metrics);
	void sendMetricWithInitState(final Metric metric);
	String getMetricEndPoint();
}
