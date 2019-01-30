package com.unity3d.services.core.configuration;

import com.unity3d.services.ads.configuration.AdsModuleConfiguration;
import com.unity3d.services.analytics.core.configuration.AnalyticsModuleConfiguration;
import com.unity3d.services.banners.configuration.BannersModuleConfiguration;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.NetworkIOException;
import com.unity3d.services.core.request.WebRequest;
import com.unity3d.services.monetization.core.configuration.MonetizationModuleConfiguration;
import com.unity3d.services.purchasing.core.configuration.PurchasingModuleConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Configuration {
	private String _webViewUrl;
	private String _webViewHash;
	private String _webViewVersion;
	private String _webViewData;
	private String _url;
	private Map<String, IModuleConfiguration> _moduleConfigurations;

	private String[] _moduleConfigurationList = {
			"com.unity3d.services.core.configuration.CoreModuleConfiguration",
			"com.unity3d.services.ads.configuration.AdsModuleConfiguration",
			"com.unity3d.services.monetization.core.configuration.MonetizationModuleConfiguration",
			"com.unity3d.services.purchasing.core.configuration.PurchasingModuleConfiguration",
			"com.unity3d.services.analytics.core.configuration.AnalyticsModuleConfiguration",
			"com.unity3d.services.ar.configuration.ARModuleConfiguration",
			"com.unity3d.services.banners.configuration.BannersModuleConfiguration"
	};

	private Class[] _webAppApiClassList;

	public Configuration () {
	}

	public Configuration (String configUrl) {
		_url = configUrl;
	}

	public void setConfigUrl (String url) {
		_url = url;
	}

	public String getConfigUrl () {
		return _url;
	}

	public Class[] getWebAppApiClassList () {
		if (_webAppApiClassList == null) {
			createWebAppApiClassList();
		}

		return _webAppApiClassList;
	}

	public String[] getModuleConfigurationList () { return _moduleConfigurationList; }

	public String getWebViewUrl() {
		return _webViewUrl;
	}

	public void setWebViewUrl (String url) {
		_webViewUrl = url;
	}

	public String getWebViewHash() {
		return _webViewHash;
	}

	public void setWebViewHash (String hash) {
		_webViewHash = hash;
	}

	public String getWebViewVersion () {
		return _webViewVersion;
	}

	public String getWebViewData () {
		return _webViewData;
	}

	public void setWebViewData (String data) {
		_webViewData = data;
	}

	public IModuleConfiguration getModuleConfiguration(String moduleName) {
		if (_moduleConfigurations != null && _moduleConfigurations.containsKey(moduleName)) {
			return _moduleConfigurations.get(moduleName);
		}

		try {
			IModuleConfiguration module = (IModuleConfiguration)Class.forName(moduleName).newInstance();
			if (module != null) {
				if (_moduleConfigurations == null) {
					_moduleConfigurations = new HashMap<>();
					_moduleConfigurations.put(moduleName, module);
				}

				return module;
			}

			return null;
		} catch (Exception e) {
			return null;
		}
	}

	protected String buildQueryString () {
		String queryString = "?ts=" + System.currentTimeMillis() + "&sdkVersion=" + SdkProperties.getVersionCode() + "&sdkVersionName=" + SdkProperties.getVersionName();
		return queryString;
	}

	protected void makeRequest () throws IOException, JSONException, IllegalStateException, NetworkIOException, IllegalArgumentException {
		if (_url == null) {
			throw new MalformedURLException("Base URL is null");
		}

		String url = _url + buildQueryString();
		DeviceLog.debug("Requesting configuration with: " + url);

		WebRequest request = new WebRequest(url, "GET", null);
		String data = request.makeRequest();
		JSONObject config = new JSONObject(data);

		_webViewUrl = config.getString("url");
		if(!config.isNull("hash")) {
			_webViewHash = config.getString("hash");
		}
		if(config.has("version")) {
			_webViewVersion = config.getString("version");
		}

		if (_webViewUrl == null || _webViewUrl.isEmpty()) {
			throw new MalformedURLException("Invalid data. Web view URL is null or empty");
		}
	}

	private void createWebAppApiClassList() {
		List<Class> apiList = new ArrayList<>();

		for (String moduleConfigClass : getModuleConfigurationList()) {
			IModuleConfiguration moduleConfiguration = getModuleConfiguration(moduleConfigClass);
			if (moduleConfiguration != null) {
				if (moduleConfiguration.getWebAppApiClassList() != null) {
					apiList.addAll(Arrays.asList(moduleConfiguration.getWebAppApiClassList()));
				}
			}
		}

		_webAppApiClassList = apiList.toArray(new Class[apiList.size()]);
	}
}
