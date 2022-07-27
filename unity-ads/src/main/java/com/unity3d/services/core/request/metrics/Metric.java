package com.unity3d.services.core.request.metrics;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Metric {

	private static final String METRIC_NAME = "n";
	private static final String METRIC_VALUE = "v";
	private static final String METRIC_TAGS = "t";

	private final String name;
	private final Object value;
	private final Map<String, String> tags;

	public Metric(String name, Object value, Map<String, String> tags) {
		this.name = name;
		this.value = value;
		this.tags = tags;
	}

	Map<String, Object> asMap() {
		Map<String, Object> result = new HashMap<>();

		if (name != null) {
			result.put(METRIC_NAME, name);
		}

		if (value != null) {
			result.put(METRIC_VALUE, value);
		}

		if (tags != null) {
			result.put(METRIC_TAGS, tags);
		}

		return result;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

	public Map<String, String> getTags() {
		return tags;
	}

	@Override
	public String toString() {
		return "Metric{" +
			"name='" + name + '\'' +
			", value='" + value + '\'' +
			", tags=" + tags +
			'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Metric metric = (Metric) o;
		return name.equals(metric.name) && value.equals(metric.value) && tags.equals(metric.tags);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value, tags);
	}
}
