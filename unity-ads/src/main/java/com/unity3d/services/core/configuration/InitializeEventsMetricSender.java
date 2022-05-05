package com.unity3d.services.core.configuration;


import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.request.metrics.Metric;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.TSIMetric;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class InitializeEventsMetricSender implements IInitializeEventsMetricSender, IInitializationListener{

	private static InitializeEventsMetricSender _instance;

    private Map<String, String> _metricTags;

	private long _startTime = 0L;
	private long _configStartTime = 0L;
	private boolean _initMetricSent = false;
	private boolean _tokenMetricSent = false;

	public static IInitializeEventsMetricSender getInstance() {
		if (_instance == null) {
			_instance = new InitializeEventsMetricSender();
		}
		return _instance;
	}

	private InitializeEventsMetricSender() {
		InitializationNotificationCenter.getInstance().addListener(this);
	}

	@Override
	public void didInitStart() {
		_startTime = System.nanoTime();
		sendMetric(TSIMetric.newInitStarted(getMetricTags()));
	}

	@Override
	public void didConfigRequestStart() {
		_configStartTime = System.nanoTime();
	}

	@Override
	public synchronized void sdkDidInitialize() {
		if (_startTime == 0L) {
			DeviceLog.debug("sdkDidInitialize called before didInitStart, skipping metric");
			return;
		}

		if (!_initMetricSent) {
			sendMetric(TSIMetric.newInitTimeSuccess(duration(), getMetricTags()));
			_initMetricSent = true;
		}
	}

	@Override
	public Long initializationStartTimeStamp() {
		return _startTime;
	}

	@Override
	public synchronized void sdkInitializeFailed(String message) {
		if (_startTime == 0L) {
			DeviceLog.debug("sdkInitializeFailed called before didInitStart, skipping metric");
			return;
		}

		if (!_initMetricSent) {
			sendMetric(TSIMetric.newInitTimeFailure(duration(), getMetricTags()));
			_initMetricSent = true;
		}
	}

	@Override
	public synchronized void sdkTokenDidBecomeAvailableWithConfig(boolean withConfig) {
		if (!_tokenMetricSent) {
			sendTokenAvailabilityMetricWithConfig(withConfig);

			if (withConfig) {
				sendTokenResolutionRequestMetricIfNeeded();
			}
			_tokenMetricSent = true;
		}
	}

	private void sendTokenAvailabilityMetricWithConfig(boolean withConfig) {
		if (_startTime == 0L) {
			DeviceLog.debug("sendTokenAvailabilityMetricWithConfig called before didInitStart, skipping metric");
			return;
		}

		Long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - _startTime);
		Map<String, String> tags = getMetricTags();
		Metric metric = withConfig
			? TSIMetric.newTokenAvailabilityLatencyConfig(duration, tags)
			: TSIMetric.newTokenAvailabilityLatencyWebview(duration, tags);

		sendMetric(metric);
	}

	private void sendTokenResolutionRequestMetricIfNeeded() {
		if (_configStartTime == 0L) {
			DeviceLog.debug("sendTokenResolutionRequestMetricIfNeeded called before didInitStart, skipping metric");
			return;
		}

		sendMetric(TSIMetric.newTokenResolutionRequestLatency(tokenDuration(), getMetricTags()));
	}

	@Override
	public Long duration() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - _startTime);
	}

	@Override
	public Long tokenDuration() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - _configStartTime);
	}

	@Override
	public void setMetricTags(Map<String, String> metricTags) {
		_metricTags = metricTags;
	}

	@Override
	public Map<String, String> getMetricTags() {
		if (_metricTags != null) {
			return _metricTags;
		} else {
			return new HashMap<>();
		}
	}

	@Override
	public void sendMetric(Metric metric) {
		SDKMetrics.getInstance().sendMetric(metric);
	}

	@Override
	public void onSdkInitialized() {
		sdkDidInitialize();
	}

	@Override
	public void onSdkInitializationFailed(String message, int code) {
		sdkInitializeFailed(message);
	}
}
