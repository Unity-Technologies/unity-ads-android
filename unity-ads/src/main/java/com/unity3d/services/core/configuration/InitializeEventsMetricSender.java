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

	private long _startTime = 0L;
	private long _privacyConfigStartTime = 0L;
	private long _privacyConfigEndTime = 0L;
	private long _configStartTime = 0L;
	private int _configRetryCount = 0;
	private int _webviewRetryCount = 0;
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
		_configRetryCount = 0;
		_webviewRetryCount = 0;
		sendMetric(TSIMetric.newInitStarted());
	}

	@Override
	public void didConfigRequestStart() {
		_configStartTime = System.nanoTime();
	}

	@Override
	public void didPrivacyConfigRequestStart() {
		_privacyConfigStartTime = System.nanoTime();
	}

	@Override
	public void didPrivacyConfigRequestEnd(boolean success) {
		_privacyConfigEndTime = System.nanoTime();

		sendPrivacyResolutionRequestIfNeeded(success);
	}

	@Override
	public synchronized void sdkDidInitialize() {
		if (initializationStartTimeStamp() == 0L) {
			DeviceLog.debug("sdkDidInitialize called before didInitStart, skipping metric");
			return;
		}

		if (!_initMetricSent) {
			sendMetric(TSIMetric.newInitTimeSuccess(duration(), getRetryTags()));
			_initMetricSent = true;
		}
	}

	@Override
	public Long initializationStartTimeStamp() {
		return _startTime;
	}

	@Override
	public synchronized void sdkInitializeFailed(String message, ErrorState errorState) {
		if (_startTime == 0L) {
			DeviceLog.debug("sdkInitializeFailed called before didInitStart, skipping metric");
			return;
		}

		if (!_initMetricSent) {
			sendMetric(TSIMetric.newInitTimeFailure(duration(), getErrorStateTags(errorState)));
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
		Map<String, String> tags = getRetryTags();

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

		sendMetric(TSIMetric.newTokenResolutionRequestLatency(tokenDuration(), getRetryTags()));
	}

	private void sendPrivacyResolutionRequestIfNeeded(boolean success) {
		if (_privacyConfigStartTime == 0L || _privacyConfigEndTime == 0L) {
			DeviceLog.debug("sendTokenResolutionRequestMetricIfNeeded called before didInitStart, skipping metric");
			return;
		}

		if (success) {
			sendMetric(TSIMetric.newPrivacyResolutionRequestLatencySuccess(privacyConfigDuration()));
		} else {
			sendMetric(TSIMetric.newPrivacyResolutionRequestLatencyFailure(privacyConfigDuration()));
		}
	}

	@Override
	public void onRetryConfig() {
		_configRetryCount++;
	}

	@Override
	public void onRetryWebview() {
		_webviewRetryCount++;
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
	public Long privacyConfigDuration() {
		return TimeUnit.NANOSECONDS.toMillis(_privacyConfigEndTime - _privacyConfigStartTime);
	}

	public Map<String, String> getErrorStateTags(ErrorState errorState) {
		Map<String, String> tags = getRetryTags();
		tags.put("stt", errorState.getMetricName());
		return tags;
	}

	@Override
	public Map<String, String> getRetryTags() {
		return new HashMap<String, String>(){{
			put("c_retry", String.valueOf(_configRetryCount));
			put("wv_retry", String.valueOf(_webviewRetryCount));
		}};
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
	public void onSdkInitializationFailed(String message, ErrorState errorState, int code) {
		sdkInitializeFailed(message, errorState);
	}
}
