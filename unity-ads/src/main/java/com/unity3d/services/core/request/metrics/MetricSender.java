package com.unity3d.services.core.request.metrics;


import android.text.TextUtils;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.InitializationStatusReader;
import com.unity3d.services.core.request.WebRequest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MetricSender extends MetricSenderBase {

	private final MetricCommonTags _commonTags;
	private final String _metricEndpoint;
	private final String _metricSampleRate;
	private final String _sessionToken;
	private final ExecutorService _executorService;

	public MetricSender(Configuration configuration, InitializationStatusReader initializationStatusReader) {
		super(initializationStatusReader);
		_metricEndpoint = configuration.getMetricsUrl();
		_executorService = Executors.newSingleThreadExecutor();
		_metricSampleRate = String.valueOf((int) Math.round(configuration.getMetricSampleRate()));
		_sessionToken = configuration.getSessionToken();
		_commonTags = new MetricCommonTags();
		_commonTags.updateWithConfig(configuration);
	}

	@Override
	public boolean areMetricsEnabledForCurrentSession() {
		return true;
	}

	public void sendEvent(String event) {
		sendEvent(event, null);
	}

	public void sendEvent(String event, Map<String, String> tags) {
		if (event == null || event.isEmpty()) {
			DeviceLog.debug("Metric event not sent due to being null or empty: " + event);
			return;
		}

		sendEvent(event, null, tags);
	}

	public void sendEvent(String event, String value, Map<String, String> tags) {
		sendMetrics(new ArrayList<>(Collections.singletonList(new Metric(event, value, tags))));
	}

	public void sendMetric(Metric metric) {
		sendMetrics(new ArrayList<>(Collections.singletonList(metric)));
	}

	public void sendMetrics(final List<Metric> metrics) {

		if (metrics == null || metrics.size() <= 0) {
			DeviceLog.debug("Metrics event not send due to being null or empty");
			return;
		}

		if (TextUtils.isEmpty(_metricEndpoint)) {
			DeviceLog.debug("Metrics: " + metrics + " was not sent to null or empty endpoint: " + _metricEndpoint);
			return;
		}

		if (_executorService.isShutdown()) {
			DeviceLog.debug("Metrics " + metrics + " was not sent due to misconfiguration");
			return;
		}

		_executorService.submit(new Runnable() {
			@Override
			public void run() {
				try {
					MetricsContainer container = new MetricsContainer(_metricSampleRate, _commonTags, metrics, _sessionToken);
					String postBody = new JSONObject(container.asMap()).toString();

					WebRequest request = new WebRequest(_metricEndpoint, "POST", null);
					request.setBody(postBody);
					request.makeRequest();

					boolean is2XXResponseCode = (request.getResponseCode() / 100) == 2;
					if (is2XXResponseCode) {
						DeviceLog.debug("Metric " + metrics + " sent to " + _metricEndpoint);
					} else {
						DeviceLog.debug("Metric " + metrics + " failed to send with response code: " + request.getResponseCode());
					}
				} catch (Exception e) {
					DeviceLog.debug("Metric " + metrics + " failed to send from exception: " + e.getMessage());
				}
			}
		});

	}

	public String getMetricEndPoint() {
		return _metricEndpoint;
	}

	void shutdown() {
		_commonTags.shutdown();
		// Allow scheduled tasks to finish execution
		_executorService.shutdown();
	}
}
