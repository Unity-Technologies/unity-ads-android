package com.unity3d.services.banners.properties;

import java.util.HashMap;

public class BannerRefreshInfo {

	private static BannerRefreshInfo instance;

	public static BannerRefreshInfo getInstance() {
		if (instance == null) {
			instance = new BannerRefreshInfo();
		}
		return instance;
	}

	private HashMap<String, Integer> _refreshRateMap;

	public BannerRefreshInfo() {
		_refreshRateMap = new HashMap<>();
	}

	public synchronized void setRefreshRate(String placementId, Integer rate) {
		_refreshRateMap.put(placementId, rate);
	}

	public synchronized Integer getRefreshRate(String placementId) {
		return _refreshRateMap.get(placementId);
	}

}
