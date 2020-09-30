package com.unity3d.services.core.request;

import android.text.TextUtils;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.SdkProperties;

import org.json.JSONObject;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SDKMetrics {
	private static ISDKMetrics _instance = new NullInstance();

	public static void setConfiguration(Configuration configuration) {

		if (configuration == null) {
			DeviceLog.debug("Metrics will not be sent from the device for this session due to misconfiguration");
			return;
		}

		// Always attempt to shutdown previous MetricInstance ExecutorService Thread
		if (_instance instanceof MetricInstance) {
			((MetricInstance) _instance).shutdown();
		}

		if (configuration.getMetricSampleRate() >= new Random().nextInt(99) + 1) {
			_instance = new MetricInstance(configuration.getMetricsUrl());
		} else {
			DeviceLog.debug("Metrics will not be sent from the device for this session");
			_instance = new NullInstance();
		}
	}

	public static ISDKMetrics getInstance() {
		return _instance;
	}

	private final static class NullInstance implements ISDKMetrics {
		public void sendEvent(final String event) {
			DeviceLog.debug("Metric " + event + " was skipped from being sent");
		}
		public void sendEventWithTags(final String event, final Map<String, String> tags) {
			sendEvent(event);
		}
	}

	private final static class MetricInstance implements ISDKMetrics {

		private final String _metricsUrl;
		private final ExecutorService _executorService;

		public MetricInstance(String url) {
			_metricsUrl = url;
			_executorService = Executors.newSingleThreadExecutor();
		}

		protected void shutdown() {
			// Allow scheduled tasks to finish execution
			_executorService.shutdown();
		}

		public void sendEvent(final String event) {
			sendEventWithTags(event, null);
		}

		public void sendEventWithTags(final String event, final Map<String, String> tags) {
			if (TextUtils.isEmpty(event)) {
				DeviceLog.debug("Metric event not sent due to being nil or empty: " + event);
				return;
			}

			if (TextUtils.isEmpty(_metricsUrl)) {
				DeviceLog.debug("Metric: " + event + " was not sent to nil or empty endpoint: " + _metricsUrl);
				return;
			}

			if (_executorService.isShutdown()) {
				DeviceLog.debug("Metric " + event + " was not sent due to misconfiguration");
				return;
			}

			_executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						String tagString = "";
						if (tags != null) {
							JSONObject jsonTags = new JSONObject(tags);
							tagString = ",\"t\":" + jsonTags.toString();
						}
						String postBody = "{\"m\":[{\"n\":\"" + event + "\"" + tagString + "}],\"t\":{\"iso\":\""
							+ Device.getNetworkCountryISO() + "\",\"plt\":\"android\",\"sdv\":\""
							+ SdkProperties.getVersionName() + "\"}}";

						WebRequest request = new WebRequest(_metricsUrl, "POST", null);
						request.setBody(postBody);
						request.makeRequest();

						boolean is2XXResponseCode = (request.getResponseCode() / 100) == 2;
						if (is2XXResponseCode) {
							DeviceLog.debug("Metric " + event + " sent to " + _metricsUrl);
						} else {
							DeviceLog.debug("Metric " + event + " failed to send with response code: " + request.getResponseCode());
						}
					} catch (Exception e) {
						DeviceLog.debug("Metric " + event + " failed to send from exception: " + e.getMessage());
					}
				}
			});
		}
	}
}

