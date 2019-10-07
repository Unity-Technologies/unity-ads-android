package com.unity3d.services.ads.webplayer;

import org.json.JSONObject;

import java.util.HashMap;

public class WebPlayerSettingsCache {

	private static WebPlayerSettingsCache instance;

	public static WebPlayerSettingsCache getInstance() {
		if (instance == null) {
			instance = new WebPlayerSettingsCache();
		}
		return instance;
	}

	private HashMap<String, JSONObject> _webSettings;
	private HashMap<String, JSONObject> _webPlayerSettings;
	private HashMap<String, JSONObject> _webPlayerEventSettings;

	public WebPlayerSettingsCache() {
		_webSettings = new HashMap<>();
		_webPlayerSettings = new HashMap<>();
		_webPlayerEventSettings = new HashMap<>();
	}

	//================================================================================
	// WebSettings
	//================================================================================

	public synchronized void addWebSettings(String viewId, JSONObject webSettings) {
		_webSettings.put(viewId, webSettings);
	}

	public synchronized void removeWebSettings(String viewId) {
		if (_webSettings.containsKey(viewId)) {
			_webSettings.remove(viewId);
		}
	}

	public synchronized JSONObject getWebSettings(String viewId) {
		if (_webSettings.containsKey(viewId)) {
			return _webSettings.get(viewId);
		} else {
			return new JSONObject();
		}
	}

	//================================================================================
	// WebPlayerSettings
	//================================================================================

	public synchronized void addWebPlayerSettings(String viewId, JSONObject webPlayerSettings) {
		_webPlayerSettings.put(viewId, webPlayerSettings);
	}

	public synchronized void removeWebPlayerSettings(String viewId) {
		if (_webPlayerSettings.containsKey(viewId)) {
			_webPlayerSettings.remove(viewId);
		}
	}

	public synchronized JSONObject getWebPlayerSettings(String viewId) {
		if (_webPlayerSettings.containsKey(viewId)) {
			return _webPlayerSettings.get(viewId);
		} else {
			return new JSONObject();
		}
	}

	//================================================================================
	// WebPlayerEventSettings
	//================================================================================

	public synchronized void addWebPlayerEventSettings(String viewId, JSONObject webPlayerEventSettings) {
		_webPlayerEventSettings.put(viewId, webPlayerEventSettings);
	}

	public synchronized void removeWebPlayerEventSettings(String viewId) {
		if (_webPlayerEventSettings.containsKey(viewId)) {
			_webPlayerEventSettings.remove(viewId);
		}
	}

	public synchronized JSONObject getWebPlayerEventSettings(String viewId) {
		if (_webPlayerEventSettings.containsKey(viewId)) {
			return _webPlayerEventSettings.get(viewId);
		} else {
			return new JSONObject();
		}
	}

}
