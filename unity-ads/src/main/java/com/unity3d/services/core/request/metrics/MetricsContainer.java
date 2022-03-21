package com.unity3d.services.core.request.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsContainer {

	private static final String METRICS_CONTAINER = "m";
	private static final String METRICS_CONTAINER_TAGS = "t";

	private final MetricCommonTags _commonTags;
	private final List<Metric> _metrics;

	public MetricsContainer(MetricCommonTags commonTags, List<Metric> metrics) {
		this._commonTags = commonTags;
		this._metrics = metrics;
	}

	public Map<String, Object> asMap() {
		Map<String, Object> result = new HashMap<>();
		List<Object> metricsMaps = new ArrayList<>();

		for (Metric metric: _metrics) {
			metricsMaps.add(metric.asMap());
		}

		result.put(METRICS_CONTAINER, metricsMaps);
		result.put(METRICS_CONTAINER_TAGS, _commonTags.asMap());

		return result;
	}
}
