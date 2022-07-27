package com.unity3d.services.core.configuration;

import com.unity3d.services.core.request.WebRequest;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.TSIMetric;

import org.json.JSONObject;

public class ConfigurationLoader implements IConfigurationLoader {
	private final Configuration _localConfiguration;
	private final ConfigurationRequestFactory _configurationRequestFactory;

	public ConfigurationLoader(ConfigurationRequestFactory configurationRequestFactory) {
		_localConfiguration = configurationRequestFactory.getConfiguration();
		_configurationRequestFactory = configurationRequestFactory;
	}

	@Override
	public void loadConfiguration(IConfigurationLoaderListener configurationLoaderListener) throws Exception {
		WebRequest request;
		try {
			request = _configurationRequestFactory.getWebRequest();
		} catch (Exception ex) {
			configurationLoaderListener.onError("Could not create web request: " + ex);
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
			_localConfiguration.handleConfigurationData(new JSONObject(data), true);
		} catch (Exception e) {
			configurationLoaderListener.onError("Could not create web request");
			return;
		}
		sendConfigMetrics(_localConfiguration.getUnifiedAuctionToken(), _localConfiguration.getStateId());
		configurationLoaderListener.onSuccess(_localConfiguration);
	}

	@Override
	public Configuration getLocalConfiguration() {
		return _localConfiguration;
	}

	private void sendConfigMetrics(String unifiedAuctionToken, String stateId) {
		if (_localConfiguration.getExperiments() != null && _localConfiguration.getExperiments().isTwoStageInitializationEnabled()) {
			if (unifiedAuctionToken == null || unifiedAuctionToken.isEmpty()) {
				SDKMetrics.getInstance().sendMetric(TSIMetric.newMissingToken());
			}

			if (stateId == null || stateId.isEmpty()) {
				SDKMetrics.getInstance().sendMetric(TSIMetric.newMissingStateId());
			}
		}
	}
}
