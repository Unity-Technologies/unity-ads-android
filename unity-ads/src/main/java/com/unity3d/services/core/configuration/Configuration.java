package com.unity3d.services.core.configuration;

import android.text.TextUtils;

import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.device.reader.DeviceInfoReaderBuilder;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.WebRequest;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.TSIMetric;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Configuration {
	private String _webViewUrl;
	private String _webViewHash;
	private String _webViewVersion;
	private String _webViewData;
	private String _sdkVersion;
	private boolean _delayWebViewUpdate;
	private int _resetWebAppTimeout;
	private int _maxRetries;
	private long _retryDelay;
	private double _retryScalingFactor;
	private int _connectedEventThresholdInMs;
	private int _maximumConnectedEvents;
	private long _networkErrorTimeout;
	private int _showTimeout;
	private int _loadTimeout;
	private int _webViewBridgeTimeout;
	private String _metricsUrl;
	private double _metricSampleRate;
	private long _webViewAppCreateTimeout;

	private String _configJsonString;
	private String _configUrl;
	private String _unifiedAuctionToken;
	private String _stateId;
	private Experiments _experiments;
	private int _tokenTimeout;
	private String _src;
	private ConfigurationRequestFactory _configurationRequestFactory;

	private Map<String, IModuleConfiguration> _moduleConfigurations;

	private String[] _moduleConfigurationList = {
			"com.unity3d.services.core.configuration.CoreModuleConfiguration",
			"com.unity3d.services.ads.configuration.AdsModuleConfiguration",
			"com.unity3d.services.analytics.core.configuration.AnalyticsModuleConfiguration",
			"com.unity3d.services.banners.configuration.BannersModuleConfiguration",
			"com.unity3d.services.store.core.configuration.StoreModuleConfiguration"
	};

	private Class[] _webAppApiClassList;

	public Configuration () {
		this.setOptionalFields(new JSONObject());
	}

	public Configuration (String configUrl) {
		this();
		_configUrl = configUrl;
	}

	public Configuration (JSONObject configData) throws MalformedURLException, JSONException {
		handleConfigurationData(configData);
	}

	public Configuration(String configUrl, Experiments experiments) {
		this(configUrl);
		DeviceInfoReaderBuilder deviceInfoReaderBuilder = new DeviceInfoReaderBuilder(new ConfigurationReader());
		_configurationRequestFactory = new ConfigurationRequestFactory(this, deviceInfoReaderBuilder.build(), _configUrl);
		_experiments = experiments;
	}

	public String getConfigUrl () { return _configUrl; }

	public Class[] getWebAppApiClassList () {
		if (_webAppApiClassList == null) {
			createWebAppApiClassList();
		}

		return _webAppApiClassList;
	}

	public String[] getModuleConfigurationList () { return _moduleConfigurationList; }

	public String getWebViewUrl() { return _webViewUrl; }

	public void setWebViewUrl (String url) { _webViewUrl = url; }

	public String getWebViewHash() { return _webViewHash; }

	public void setWebViewHash (String hash) { _webViewHash = hash; }

	public String getWebViewVersion () { return _webViewVersion; }

	public String getWebViewData () { return _webViewData; }

	public void setWebViewData (String data) { _webViewData = data; }

	public String getSdkVersion() { return _sdkVersion; }

	public boolean getDelayWebViewUpdate() { return _delayWebViewUpdate; }

	public int getResetWebappTimeout() { return _resetWebAppTimeout; }

	public int getMaxRetries() { return _maxRetries; }

	public long getRetryDelay() { return _retryDelay; }

	public double getRetryScalingFactor() { return _retryScalingFactor; }

	public int getConnectedEventThreshold() { return _connectedEventThresholdInMs; }

	public int getMaximumConnectedEvents() { return _maximumConnectedEvents; }

	public long getNetworkErrorTimeout() { return _networkErrorTimeout; }

	public int getShowTimeout() { return _showTimeout; }

	public int getLoadTimeout() { return _loadTimeout; }

	public int getWebViewBridgeTimeout() { return _webViewBridgeTimeout; }

	public String getMetricsUrl() { return _metricsUrl; }

	public double getMetricSampleRate() { return _metricSampleRate; }

	public long getWebViewAppCreateTimeout() { return _webViewAppCreateTimeout; }

	public String getStateId() { return (_stateId != null) ? _stateId : ""; }

	public String getUnifiedAuctionToken() { return _unifiedAuctionToken; }

	public Experiments getExperiments() { return _experiments; }

	public int getTokenTimeout() { return _tokenTimeout; }

	public String getSrc() {return (_src != null) ? _src : ""; }

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

	public String getJSONString() { return _configJsonString; }

	protected void makeRequest () throws Exception {
		if (_configUrl == null) {
			throw new MalformedURLException("Base URL is null");
		}

		WebRequest request = _configurationRequestFactory.getWebRequest();
		InitializeEventsMetricSender.getInstance().didConfigRequestStart();
		String data = request.makeRequest();

		try {
			handleConfigurationData(new JSONObject(data));
		} catch (Exception e) {
			throw e;
		}
		saveToDisk();
	}

	protected void handleConfigurationData(JSONObject configData) throws MalformedURLException, JSONException {

		String url = null;

		try {
			url = !configData.isNull("url") ? configData.getString("url") : null;
		} catch (JSONException e) {}


		if (TextUtils.isEmpty(url)) {
			throw new MalformedURLException("WebView URL is null or empty");
		}
		_webViewUrl = url;

		// Workaround to allow hash to be set to null by swallowing error
		try {
			_webViewHash = !configData.isNull("hash") ? configData.getString("hash") : null;
		} catch (JSONException e) {
			_webViewHash = null;
		}

		_unifiedAuctionToken = !configData.isNull("tkn") ? configData.optString("tkn") : null;
		_stateId = !configData.isNull("sid") ? configData.optString("sid") : null;

		this.setOptionalFields(configData);
		_configJsonString = getFilteredConfigJson(configData).toString();
	}

	private void setOptionalFields(JSONObject configData) {
		_webViewVersion = configData.optString("version", null);
		_delayWebViewUpdate = configData.optBoolean("dwu", false);
		_resetWebAppTimeout = configData.optInt("rwt", 10000);
		_maxRetries = configData.optInt("mr", 6);
		_retryDelay = configData.optLong("rd", 5000L);
		_retryScalingFactor = configData.optDouble("rcf", 2.0d);
		_connectedEventThresholdInMs = configData.optInt("cet", 10000);
		_maximumConnectedEvents = configData.optInt("mce", 500);
		_networkErrorTimeout = configData.optLong("net", 60000L);
		_sdkVersion = configData.optString("sdkv", "");
		_showTimeout = configData.optInt("sto", 10000);
		_loadTimeout = configData.optInt("lto", 30000);
		_webViewBridgeTimeout = configData.optInt("wto", 5000);
		_metricsUrl = configData.optString("murl", "");
		_metricSampleRate = configData.optDouble("msr", 100d);
		_webViewAppCreateTimeout = configData.optLong("wct", 60000L);
		_tokenTimeout = configData.optInt("tto", 5000);
		_src = configData.optString("src", null);
		_experiments = new Experiments(configData.optJSONObject("exp"));
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

	public void saveToDisk() {

		Utilities.writeFile(new File(SdkProperties.getLocalConfigurationFilepath()), getJSONString());
	}

	private JSONObject getFilteredConfigJson(JSONObject jsonConfig) throws JSONException {
		JSONObject filteredConfig = new JSONObject();
		for (Iterator<String> it = jsonConfig.keys(); it.hasNext(); ) {
			String currentKey = it.next();
			Object currentValue = jsonConfig.opt(currentKey);
			if (!currentKey.equals("tkn") && !currentKey.equals("sid"))
			filteredConfig.put(currentKey, currentValue);

		}
		return filteredConfig;
	}

	public Map<String, String> getMetricTags() {
		Map<String, String> tags = new HashMap<>();

		if (_experiments != null) {
			tags.putAll(_experiments.getExperimentTags());
		}

		tags.put("src", getSrc());

		return tags;
	}
}
