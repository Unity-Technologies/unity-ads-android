
package com.unity3d.services.ads.api;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.operation.show.ShowModule;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class Show {
	@WebViewExposed
	public static void sendShowFailedEvent(final String placementId, final String listenerId, final String error, final String message, WebViewCallback callback) {
		ShowModule.getInstance().onUnityAdsShowFailure(listenerId, UnityAds.UnityAdsShowError.valueOf(error), message);
		callback.invoke();
	}

	@WebViewExposed
	public static void sendShowStartEvent(final String placementId, final String listenerId, WebViewCallback callback) {
		ShowModule.getInstance().onUnityAdsShowStart(listenerId);
		callback.invoke();
	}

	@WebViewExposed
	public static void sendShowClickEvent(final String placementId, final String listenerId, WebViewCallback callback) {
		ShowModule.getInstance().onUnityAdsShowClick(listenerId);
		callback.invoke();
	}

	@WebViewExposed
	public static void sendShowCompleteEvent(final String placementId, final String listenerId, final String finishState, WebViewCallback callback) {
		ShowModule.getInstance().onUnityAdsShowComplete(listenerId, UnityAds.UnityAdsShowCompletionState.valueOf(finishState));
		callback.invoke();
	}
}
