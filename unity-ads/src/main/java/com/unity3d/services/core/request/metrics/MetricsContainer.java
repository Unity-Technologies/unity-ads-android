package com.unity3d.services.core.request.metrics;

import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsContainer {

	private static final String METRICS_CONTAINER = "m";
	private static final String METRICS_CONTAINER_TAGS = "t";
	private static final String METRIC_CONTAINER_SAMPLE_RATE = "msr";
	private static final String METRIC_CONTAINER_SESSION_TOKEN = "sTkn";
	private static final String METRIC_CONTAINER_SHARED_SESSION_ID = "shSid";
	private static final String METRIC_CONTAINER_API_LEVEL = "apil";
	private static final String METRIC_CONTAINER_DEVICE_MAKE = "deviceMake";
	private static final String METRIC_CONTAINER_DEVICE_NAME = "deviceName";
	private static final String METRIC_CONTAINER_DEVICE_MODEL = "deviceModel";
	private static final String METRIC_CONTAINER_GAME_ID = "gameId";

	private final MetricCommonTags _commonTags;
	private final List<Metric> _metrics;
	private final String _metricSampleRate;
	private final String _shSid;
	private final String _sTkn;
	private final String _apiLevel;
	private final String _deviceModel;
	private final String _deviceName;
	private final String _deviceManufacturer;
	private final String _gameId;

	public MetricsContainer(String metricSampleRate, MetricCommonTags commonTags, List<Metric> metrics, String sTkn) {
		this._metricSampleRate = metricSampleRate;
		this._commonTags = commonTags;
		this._metrics = metrics;
		this._shSid = Session.Default.getId();
		this._sTkn = sTkn;
		this._apiLevel = String.valueOf(Device.getApiLevel());
		this._deviceModel = Device.getModel();
		this._deviceName = Device.getDevice();
		this._deviceManufacturer = Device.getManufacturer();
		this._gameId = ClientProperties.getGameId();
	}

	public Map<String, Object> asMap() {
		Map<String, Object> result = new HashMap<>();
		List<Object> metricsMaps = new ArrayList<>();

		for (Metric metric: _metrics) {
			metricsMaps.add(metric.asMap());
		}

		result.put(METRIC_CONTAINER_SAMPLE_RATE, _metricSampleRate);
		result.put(METRICS_CONTAINER, metricsMaps);
		result.put(METRICS_CONTAINER_TAGS, _commonTags.asMap());
		result.put(METRIC_CONTAINER_SESSION_TOKEN, _sTkn);
		result.put(METRIC_CONTAINER_SHARED_SESSION_ID, _shSid);
		if (_apiLevel != null) result.put(METRIC_CONTAINER_API_LEVEL, _apiLevel);
		if (_deviceModel != null) result.put(METRIC_CONTAINER_DEVICE_MODEL, _deviceModel);
		if (_deviceName != null) result.put(METRIC_CONTAINER_DEVICE_NAME, _deviceName);
		if (_deviceManufacturer != null) result.put(METRIC_CONTAINER_DEVICE_MAKE, _deviceManufacturer);
		if (_gameId != null) result.put(METRIC_CONTAINER_GAME_ID, _gameId);

		return result;
	}
}
