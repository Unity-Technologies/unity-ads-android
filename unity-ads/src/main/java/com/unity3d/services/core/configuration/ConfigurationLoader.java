package com.unity3d.services.core.configuration;

import com.unity3d.services.core.device.reader.DeviceInfoReaderBuilder;
import com.unity3d.services.core.request.WebRequest;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.TSIMetric;

import org.json.JSONObject;

import java.util.Map;

public class ConfigurationLoader {
	private final Configuration _localConfiguration;
	private final ConfigurationRequestFactory _configurationRequestFactory;

	public ConfigurationLoader(Configuration localConfiguration) {
		_localConfiguration = localConfiguration;
		DeviceInfoReaderBuilder deviceInfoReaderBuilder = new DeviceInfoReaderBuilder(new ConfigurationReader());
		_configurationRequestFactory = new ConfigurationRequestFactory(localConfiguration, deviceInfoReaderBuilder.build(), localConfiguration.getConfigUrl());

	}

	public void loadConfiguration(IConfigurationLoaderListener configurationLoaderListener) throws Exception {
		if (_localConfiguration.getConfigUrl() == null) {
			configurationLoaderListener.onError("Base URL is null");
			return;
		}

		WebRequest request;
		try {
			request = _configurationRequestFactory.getWebRequest();
		} catch (Exception e) {
			configurationLoaderListener.onError("Could not create web request");
			return;
		}

		InitializeEventsMetricSender.getInstance().didConfigRequestStart();

		String data = request.makeRequest();
		boolean is2XXResponseCode = (request.getResponseCode() / 100) == 2;
		if (!is2XXResponseCode) {
			configurationLoaderListener.onError("Non 2xx HTTP status received from ads configuration request.");
			return;
		}
		try {
			_localConfiguration.handleConfigurationData(new JSONObject(data));
		} catch (Exception e) {
			configurationLoaderListener.onError("Could not create web request");
			return;
		}
		sendConfigMetrics(_localConfiguration.getUnifiedAuctionToken(), _localConfiguration.getStateId());
		configurationLoaderListener.onSuccess(_localConfiguration);
	}

	private void sendConfigMetrics(String unifiedAuctionToken, String stateId) {
		Map<String, String> tags = _localConfiguration.getMetricTags();

		if (_localConfiguration.getExperiments() != null && _localConfiguration.getExperiments().isTwoStageInitializationEnabled()) {
			if (unifiedAuctionToken == null || unifiedAuctionToken.isEmpty()) {
				SDKMetrics.getInstance().sendMetric(TSIMetric.newMissingToken(tags));
			}

			if (stateId == null || stateId.isEmpty()) {
				SDKMetrics.getInstance().sendMetric(TSIMetric.newMissingStateId(tags));
			}
		}
	}
}
