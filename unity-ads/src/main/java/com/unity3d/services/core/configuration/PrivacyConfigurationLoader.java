package com.unity3d.services.core.configuration;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.request.WebRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class PrivacyConfigurationLoader implements IConfigurationLoader {
	private final IConfigurationLoader _configurationLoader;
	private final ConfigurationRequestFactory _configurationRequestFactory;
	private final PrivacyConfigStorage _privacyConfigStorage;

	public PrivacyConfigurationLoader(IConfigurationLoader configurationLoader, ConfigurationRequestFactory configurationRequestFactory, PrivacyConfigStorage privacyConfigStorage) {
		_configurationLoader = configurationLoader;
		_configurationRequestFactory = configurationRequestFactory;
		_privacyConfigStorage = privacyConfigStorage;
	}

	@Override
	public void loadConfiguration(final IConfigurationLoaderListener configurationLoaderListener) throws Exception {
		// Only do the privacy config request if it was not done before (in case of retires)
		if (_privacyConfigStorage.getPrivacyConfig().getPrivacyStatus() == PrivacyConfigStatus.UNKNOWN) {
			load(new IPrivacyConfigurationListener() {
				@Override
				public void onSuccess(PrivacyConfig privacyMode) {
					_privacyConfigStorage.setPrivacyConfig(privacyMode);
				}

				@Override
				public void onError(String errorMsg) {
					DeviceLog.warning("Couldn't fetch privacy configuration: " + errorMsg);
					// If we encounter error trying to do privacy request, default to "not allowed"
					_privacyConfigStorage.setPrivacyConfig(new PrivacyConfig());
				}
			});
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
			privacyLoaderListener.onError("Could not create web request: " + ex);
			return;
		}

		InitializeEventsMetricSender.getInstance().didPrivacyConfigRequestStart();

		String response = request.makeRequest();

		try {
			boolean is2XXResponseCode = (request.getResponseCode() / 100) == 2;
			if (is2XXResponseCode) {
				InitializeEventsMetricSender.getInstance().didPrivacyConfigRequestEnd(true);
				privacyLoaderListener.onSuccess(new PrivacyConfig(new JSONObject(response)));
			} else {
				InitializeEventsMetricSender.getInstance().didPrivacyConfigRequestEnd(false);
				privacyLoaderListener.onError("Privacy request failed with code: " + request.getResponseCode());
			}
		} catch (Exception e) {
			InitializeEventsMetricSender.getInstance().didPrivacyConfigRequestEnd(false);
			privacyLoaderListener.onError("Could not create web request");
		}
	}
}
