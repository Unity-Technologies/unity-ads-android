package com.unity3d.services.ads.properties;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.services.banners.IUnityBannerListener;

public class AdsProperties {
	private static IUnityAdsListener _listener;
	private static int _showTimeout = 5000;
	private static IUnityBannerListener bannerListener;

	public static IUnityAdsListener getListener () {
		return _listener;
	}

	public static void setListener (IUnityAdsListener listener) {
		_listener = listener;
	}

	public static void setShowTimeout(int timeout) {
		_showTimeout = timeout;
	}

	public static int getShowTimeout() {
		return _showTimeout;
	}

	public static void setBannerListener(IUnityBannerListener bannerListener) {
		AdsProperties.bannerListener = bannerListener;
	}

	public static IUnityBannerListener getBannerListener() {
		return bannerListener;
	}
}
