package com.unity3d.services.ads.api;

import com.unity3d.services.ads.load.LoadModule;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class Load {
	@WebViewExposed
	public static void sendAdLoaded(final String placementId, final String listenerId, WebViewCallback callback) {
		LoadModule.getInstance().sendAdLoaded(placementId, listenerId);
		callback.invoke();
	}

	@WebViewExposed
	public static void sendAdFailedToLoad(final String placementId, final String listenerId, WebViewCallback callback) {
		LoadModule.getInstance().sendAdFailedToLoad(placementId, listenerId);
		callback.invoke();
	}

}
