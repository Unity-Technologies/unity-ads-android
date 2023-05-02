package com.unity3d.services.core.configuration;

import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.network.core.HttpClient;
import com.unity3d.services.core.network.mapper.WebRequestToHttpRequestKt;
import com.unity3d.services.core.network.model.HttpRequest;
import com.unity3d.services.core.network.model.HttpResponse;
import com.unity3d.services.core.request.WebRequest;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;
import com.unity3d.services.core.request.metrics.TSIMetric;

import org.json.JSONObject;

public class ConfigurationLoader implements IConfigurationLoader {
	private final Configuration _localConfiguration;
	private final ConfigurationRequestFactory _configurationRequestFactory;
	private HttpClient _httpClient = Utilities.getService(HttpClient.class);
	private final SDKMetricsSender _sdkMetricsSender;

	public ConfigurationLoader(ConfigurationRequestFactory configurationRequestFactory, SDKMetricsSender sdkMetricsSender) {
		_localConfiguration = configurationRequestFactory.getConfiguration();
		_configurationRequestFactory = configurationRequestFactory;
		_sdkMetricsSender = sdkMetricsSender;
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

		HttpRequest httpRequest = WebRequestToHttpRequestKt.toHttpRequest(request);
		InitializeEventsMetricSender.getInstance().didConfigRequestStart();

		HttpResponse response = _httpClient.executeBlocking(httpRequest);
		String data = response.getBody().toString();
		boolean is2XXResponseCode = (response.getStatusCode() / 100) == 2;
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
		if (unifiedAuctionToken == null || unifiedAuctionToken.isEmpty()) {
			_sdkMetricsSender.sendMetric(TSIMetric.newMissingToken());
		}

		if (stateId == null || stateId.isEmpty()) {
			_sdkMetricsSender.sendMetric(TSIMetric.newMissingStateId());
		}
	}
}
