package com.unity3d.services.ads.api;

import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class AdsProperties {
	@WebViewExposed
	public static void setShowTimeout(Integer timeout, WebViewCallback callback) {
		com.unity3d.ads.properties.AdsProperties.setShowTimeout(timeout);
		callback.invoke();
	}
}
