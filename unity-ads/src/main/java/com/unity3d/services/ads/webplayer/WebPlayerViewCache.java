package com.unity3d.services.ads.webplayer;

import java.util.HashMap;

public class WebPlayerViewCache {

	private static WebPlayerViewCache instance;

	public static WebPlayerViewCache getInstance() {
		if (instance == null) {
			instance = new WebPlayerViewCache();
		}
		return instance;
	}

	private HashMap<String, WebPlayerView> _webPlayerMap;

	public WebPlayerViewCache() {
		_webPlayerMap = new HashMap<>();
	}

	public synchronized void addWebPlayer(String viewId, WebPlayerView webPlayerView) {
		_webPlayerMap.put(viewId, webPlayerView);
	}

	public synchronized void removeWebPlayer(String viewId) {
		if (_webPlayerMap.containsKey(viewId)) {
			_webPlayerMap.remove(viewId);
		}
	}

	public synchronized WebPlayerView getWebPlayer(String viewId) {
		if (_webPlayerMap.containsKey(viewId)) {
			return _webPlayerMap.get(viewId);
		} else {
			return null;
		}
	}

}
