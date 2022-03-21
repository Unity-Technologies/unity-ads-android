package com.unity3d.services.core.request.metrics;


import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.log.DeviceLog;

import java.util.List;
import java.util.Map;
import java.util.Random;

public final class SDKMetrics {

	private static final String NULL_INSTANCE_METRICS_URL = "nullInstanceMetricsUrl";

	private static ISDKMetrics _instance;
	private static MetricSenderWithBatch _batchedSender;

	public static void setConfiguration(Configuration configuration) {

		if (configuration == null) {
			DeviceLog.debug("Metrics will not be sent from the device for this session due to misconfiguration");
			return;
		}

		// Always attempt to shutdown previous MetricInstance ExecutorService Thread
		if (_instance instanceof MetricSender) {
			((MetricSender) _instance).shutdown();
		}

		if (configuration.getMetricSampleRate() >= new Random().nextInt(99) + 1) {
			_instance = new MetricSender(configuration.getMetricsUrl());
		} else {
			DeviceLog.debug("Metrics will not be sent from the device for this session");
			_instance = new NullInstance(NULL_INSTANCE_METRICS_URL);
		}

		if (_batchedSender == null) {
			_batchedSender = new MetricSenderWithBatch(_instance);
		} else {
			_batchedSender.updateOriginal(_instance);
		}

		_batchedSender.sendQueueIfNeeded();
	}

	public static synchronized ISDKMetrics getInstance() {

		if (_instance == null) {
			_instance = new NullInstance(null);
		}

		if (_batchedSender == null) {
			_batchedSender = new MetricSenderWithBatch(_instance);
		}

		return _batchedSender;
	}

	private final static class NullInstance implements ISDKMetrics {

		private final String _metricEndpoint;

		public NullInstance(String url) {
			_metricEndpoint = url;
		}

		public void sendEvent(final String event) {
			DeviceLog.debug("Metric " + event + " was skipped from being sent");
		}

		public void sendEvent(String event, String value, Map<String, String> tags) {
			sendEvent(event);
		}

		public void sendEvent(final String event, final Map<String, String> tags) {
			sendEvent(event);
		}

		public void sendMetric(Metric metric) {
			DeviceLog.debug("Metric " + metric + " was skipped from being sent");
		}

		public void sendMetrics(List<Metric> metrics) {
			DeviceLog.debug("Metrics: " + metrics + " was skipped from being sent");
		}

		public String getMetricEndPoint() {
			return _metricEndpoint;
		}
	}
}

