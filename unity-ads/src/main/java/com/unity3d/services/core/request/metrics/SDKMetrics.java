package com.unity3d.services.core.request.metrics;


import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.InitializationStatusReader;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SDKMetrics {

	private static final String NULL_INSTANCE_METRICS_URL = "nullInstanceMetricsUrl";

	private static SDKMetricsSender _instance;
	private static MetricSenderWithBatch _batchedSender;
	private static final AtomicBoolean _configurationIsSet = new AtomicBoolean(false);

	public static void setConfiguration(Configuration configuration) {
		if (configuration == null) {
			DeviceLog.debug("Metrics will not be sent from the device for this session due to misconfiguration");
			return;
		}

		// Only allow configuration to be set once for an application life cycle.
		if (!isAllowedToSetConfiguration(configuration)) return;

		// Always attempt to shutdown previous MetricInstance ExecutorService Thread
		if (_instance instanceof MetricSender) {
			((MetricSender) _instance).shutdown();
		}

		if (configuration.getMetricSampleRate() >= new Random().nextInt(99) + 1) {
			_instance = new MetricSender(configuration, new InitializationStatusReader());
		} else {
			DeviceLog.debug("Metrics will not be sent from the device for this session");
			_instance = new NullInstance(NULL_INSTANCE_METRICS_URL);
		}

		if (_batchedSender == null) {
			_batchedSender = new MetricSenderWithBatch(_instance, new InitializationStatusReader());
		} else {
			_batchedSender.updateOriginal(_instance);
		}

		_batchedSender.sendQueueIfNeeded();
	}

	public static synchronized SDKMetricsSender getInstance() {

		if (_instance == null) {
			_instance = new NullInstance(null);
		}

		if (_batchedSender == null) {
			_batchedSender = new MetricSenderWithBatch(_instance, new InitializationStatusReader());
		}

		return _batchedSender;
	}

	private static boolean isAllowedToSetConfiguration(Configuration configuration) {
		return !TextUtils.isEmpty(configuration.getMetricsUrl()) &&
			_configurationIsSet.compareAndSet(false, true);
	}

	private final static class NullInstance implements SDKMetricsSender {

		private final String _metricEndpoint;

		public NullInstance(String url) {
			_metricEndpoint = url;
		}

		@Override
		public boolean areMetricsEnabledForCurrentSession() {
			return false;
		}

		public void sendEvent(@NonNull final String event) {
			DeviceLog.debug("Metric " + event + " was skipped from being sent");
		}

		public void sendEvent(@NonNull String event, String value, Map<String, String> tags) {
			sendEvent(event);
		}

		public void sendEvent(@NonNull final String event, final Map<String, String> tags) {
			sendEvent(event);
		}

		public void sendMetric(@NonNull Metric metric) {
			DeviceLog.debug("Metric " + metric + " was skipped from being sent");
		}

		public void sendMetrics(@NonNull List<Metric> metrics) {
			DeviceLog.debug("Metrics: " + metrics + " was skipped from being sent");
		}

		public void sendMetricWithInitState(Metric metric) {
			sendMetric(metric);
		}

		public String getMetricEndPoint() {
			return _metricEndpoint;
		}
	}
}

