package com.unity3d.services.core.configuration;

import com.unity3d.services.core.request.metrics.Metric;

import java.util.Map;

public interface IInitializeEventsMetricSender {

	void didInitStart();

	void didConfigRequestStart();

	void didConfigRequestEnd(boolean success);

	void didPrivacyConfigRequestStart();

	void didPrivacyConfigRequestEnd(boolean success);

	void sdkDidInitialize();

	void onRetryConfig();

	void onRetryWebview();

	Long initializationStartTimeStamp();

	void sdkInitializeFailed(String message, ErrorState errorState);

	void sdkTokenDidBecomeAvailableWithConfig(boolean withConfig);

	Long duration();

	Long tokenDuration();

	Long privacyConfigDuration();

	Long configRequestDuration();

	Map<String, String> getRetryTags();

	void sendMetric(Metric metric);
}
