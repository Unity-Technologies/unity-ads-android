package com.unity3d.ads.configuration;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.request.NetworkIOException;
import com.unity3d.ads.request.WebRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;

public class Configuration {
	private String _webViewUrl;
	private String _webViewHash;
	private String _webViewVersion;
	private String _webViewData;
	private String _url;

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

	public void setWebAppApiClassList (Class[] apiClassList) {
		_webAppApiClassList = apiClassList;
	}

	public Class[] getWebAppApiClassList () {
		return _webAppApiClassList;
	}

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

	protected String buildQueryString () {
		String queryString = "?ts=" + System.currentTimeMillis() + "&sdkVersion=" + SdkProperties.getVersionCode() + "&sdkVersionName=" + SdkProperties.getVersionName();
		return queryString;
	}

	protected void makeRequest () throws IOException, JSONException, IllegalStateException, NetworkIOException {
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
}