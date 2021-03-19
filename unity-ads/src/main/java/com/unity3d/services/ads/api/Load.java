package com.unity3d.services.ads.api;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.operation.load.LoadModule;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class Load {
	@WebViewExposed
	public static void sendAdLoaded(final String placementId, final String listenerId, WebViewCallback callback) {
		LoadModule.getInstance().onUnityAdsAdLoaded(listenerId);
		callback.invoke();
	}

	@WebViewExposed
	public static void sendAdFailedToLoad(final String placementId, final String listenerId, final String error, final String message,  WebViewCallback callback) {
		LoadModule.getInstance().onUnityAdsFailedToLoad(listenerId, UnityAds.UnityAdsLoadError.valueOf(error), message);
		callback.invoke();
	}

}
