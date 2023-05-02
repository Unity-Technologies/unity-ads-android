package com.unity3d.services.core.configuration;

import com.unity3d.services.core.extensions.AbortRetryException;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.network.core.HttpClient;
import com.unity3d.services.core.network.mapper.WebRequestToHttpRequestKt;
import com.unity3d.services.core.network.model.HttpRequest;
import com.unity3d.services.core.network.model.HttpResponse;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.WebRequest;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

public class PrivacyConfigurationLoader implements IConfigurationLoader {
	private final IConfigurationLoader _configurationLoader;
	private final ConfigurationRequestFactory _configurationRequestFactory;
	private final PrivacyConfigStorage _privacyConfigStorage;
	private final HttpClient _httpClient = Utilities.getService(HttpClient.class);

	public PrivacyConfigurationLoader(IConfigurationLoader configurationLoader, ConfigurationRequestFactory configurationRequestFactory, PrivacyConfigStorage privacyConfigStorage) {
		_configurationLoader = configurationLoader;
		_configurationRequestFactory = configurationRequestFactory;
		_privacyConfigStorage = privacyConfigStorage;
	}

	@Override
	public void loadConfiguration(final IConfigurationLoaderListener configurationLoaderListener) throws Exception {
		AtomicBoolean gameDisabled = new AtomicBoolean(false);
		// Only do the privacy config request if it was not done before (in case of retires)
		if (_privacyConfigStorage.getPrivacyConfig().getPrivacyStatus() == PrivacyConfigStatus.UNKNOWN) {
			load(new IPrivacyConfigurationListener() {
				@Override
				public void onSuccess(PrivacyConfig privacyMode) {
					_privacyConfigStorage.setPrivacyConfig(privacyMode);
				}

				@Override
				public void onError(PrivacyCallError privacyCallError, String errorMsg) {
					DeviceLog.warning("Couldn't fetch privacy configuration: " + errorMsg);
					// If we encounter error trying to do privacy request, default to "not allowed"
					_privacyConfigStorage.setPrivacyConfig(new PrivacyConfig());
					// Fail fast if we have a 423 status
					if (privacyCallError == PrivacyCallError.LOCKED_423) {
						gameDisabled.set(true);
					}
				}
			});
		}
		if (gameDisabled.get()) {
			// Abort! Game is disabled.
			throw new AbortRetryException("Game is disabled");
		}
		_configurationLoader.loadConfiguration(configurationLoaderListener);
	}

	@Override
	public Configuration getLocalConfiguration() {
		return _configurationLoader.getLocalConfiguration();
	}

	private void load(IPrivacyConfigurationListener privacyLoaderListener) throws Exception {
		WebRequest request;
		try {
			request = _configurationRequestFactory.getWebRequest();
		} catch (Exception ex) {
			privacyLoaderListener.onError(PrivacyCallError.NETWORK_ISSUE, "Could not create web request: " + ex);
			return;
		}

		HttpRequest httpRequest = WebRequestToHttpRequestKt.toHttpRequest(request);
		InitializeEventsMetricSender.getInstance().didPrivacyConfigRequestStart();
		HttpResponse response = _httpClient.executeBlocking(httpRequest);
		try {
			boolean is2XXResponseCode = (response.getStatusCode() / 100) == 2;
			if (is2XXResponseCode) {
				InitializeEventsMetricSender.getInstance().didPrivacyConfigRequestEnd(true);
				privacyLoaderListener.onSuccess(new PrivacyConfig(new JSONObject(response.getBody().toString())));
			} else if (response.getStatusCode() == 423) {
				InitializeEventsMetricSender.getInstance().didPrivacyConfigRequestEnd(false);
				privacyLoaderListener.onError(PrivacyCallError.LOCKED_423, "Game ID is disabled " + ClientProperties.getGameId());
			} else {
				InitializeEventsMetricSender.getInstance().didPrivacyConfigRequestEnd(false);
				privacyLoaderListener.onError(PrivacyCallError.NETWORK_ISSUE, "Privacy request failed with code: " + response.getStatusCode());
			}
		} catch (Exception e) {
			InitializeEventsMetricSender.getInstance().didPrivacyConfigRequestEnd(false);
			privacyLoaderListener.onError(PrivacyCallError.NETWORK_ISSUE, "Could not create web request");
		}
	}
}
