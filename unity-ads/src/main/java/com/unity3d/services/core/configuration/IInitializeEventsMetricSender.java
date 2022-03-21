package com.unity3d.services.core.configuration;

import com.unity3d.services.core.request.metrics.Metric;

import java.util.Map;

public interface IInitializeEventsMetricSender {

	void didInitStart();

	void didConfigRequestStart();

	void sdkDidInitialize();

	Long initializationStartTimeStamp();

	void sdkInitializeFailed(String message);

	void sdkTokenDidBecomeAvailableWithConfig(boolean withConfig);

	Long duration();

	Long tokenDuration();

	void setMetricTags(Map<String, String> metricTags);

	Map<String, String> getMetricTags();

	void sendMetric(Metric metric);
}
