package com.unity3d.services.ads.webplayer;

import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

// WebPlayerView Events going to WebView
public class WebPlayerEventBridge {

	public static void error(String viewId, String failingUrl, String errorMessage) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.ERROR, failingUrl, errorMessage, viewId);
		}
	}

	public static void sendFrameUpdate(String viewId, int x, int y, int width, int height, float alpha) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.FRAME_UPDATE, viewId, x, y, width, height, alpha);
		}
	}

	public static void sendGetFrameResponse(String callId, String viewId, int x, int y, int width, int height, float alpha) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.GET_FRAME_RESPONSE, callId, viewId, x, y, width, height, alpha);
		}
	}

}
