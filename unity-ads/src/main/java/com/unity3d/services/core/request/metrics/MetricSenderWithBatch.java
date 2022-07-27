package com.unity3d.services.core.request.metrics;

import android.text.TextUtils;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.InitializationStatusReader;
import com.unity3d.services.core.properties.SdkProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class MetricSenderWithBatch implements IMetricSenderWithBatch {

	private final LinkedBlockingQueue<Metric> _queue = new LinkedBlockingQueue<>();
	private ISDKMetrics _original;
	private final InitializationStatusReader _initStatusReader = new InitializationStatusReader();

	public MetricSenderWithBatch(ISDKMetrics metrics) {
		this._original = metrics;
	}
	@Override
	public void updateOriginal(ISDKMetrics metrics) {
		_original = metrics;
	}

	@Override
	public boolean areMetricsEnabledForCurrentSession() {
		return _original.areMetricsEnabledForCurrentSession();
	}

	@Override
	public void sendEvent(String event) {
		sendEvent(event, null);
	}

	@Override
	public void sendEvent(String event, String value, Map<String, String> tags) {
		if (event == null || event.isEmpty()) {
			DeviceLog.debug("Metric event not sent due to being null or empty: " + event);
			return;
		}

		sendMetrics(new ArrayList<>(Collections.singletonList(new Metric(event, value, tags))));
	}

	@Override
	public void sendEvent(String event, Map<String, String> tags) {
		sendEvent(event, null, tags);
	}

	@Override
	public void sendMetric(Metric metric) {
		sendMetrics(new ArrayList<>(Collections.singletonList(metric)));
	}

	@Override
	public synchronized void sendMetrics(List<Metric> metrics) {
		_queue.addAll(metrics);

		if (!TextUtils.isEmpty(_original.getMetricEndPoint()) && _queue.size() > 0) {
			List<Metric> eventsToSend = new ArrayList<>();
			_queue.drainTo(eventsToSend);
			_original.sendMetrics(eventsToSend);
		}

	}

	@Override
	public void sendMetricWithInitState(Metric metric) {
		if (metric == null || metric.getTags() == null) return;
		metric.getTags().put("state", _initStatusReader.getInitializationStateString(SdkProperties.getCurrentInitializationState()));
		sendMetric(metric);
	}

	@Override
	public String getMetricEndPoint() {
		return _original == null ? null : _original.getMetricEndPoint();
	}

	@Override
	public void sendQueueIfNeeded() {
		sendMetrics(new ArrayList<Metric>());
	}
}
