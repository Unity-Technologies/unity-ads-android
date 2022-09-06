package com.unity3d.services.core.request.metrics;

import static com.unity3d.services.core.request.metrics.AdOperationMetric.INIT_STATE;

import com.unity3d.services.core.properties.InitializationStatusReader;
import com.unity3d.services.core.properties.SdkProperties;

import java.util.HashMap;

abstract class MetricSenderBase implements ISDKMetrics {

	private final InitializationStatusReader _initStatusReader;

	public MetricSenderBase(InitializationStatusReader initializationStatusReader) {
		_initStatusReader = initializationStatusReader;
	}

	@Override
	public void sendMetricWithInitState(Metric metric) {
		if (metric == null) return;

		if (metric.getTags() == null) {
			metric = new Metric(metric.getName(), metric.getValue(), new HashMap<String, String>());
		}

		metric.getTags().put(INIT_STATE, _initStatusReader.getInitializationStateString(SdkProperties.getCurrentInitializationState()));

		sendMetric(metric);
	}

}
