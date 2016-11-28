package com.unity3d.ads.api;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.mediation.IUnityAdsExtendedListener;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

public class Listener {
	@WebViewExposed
	public static void sendReadyEvent(final String placementId, WebViewCallback callback) {
		if(UnityAds.getListener() != null) {
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					UnityAds.getListener().onUnityAdsReady(placementId);
				}
			});
		}
		callback.invoke();
	}

	@WebViewExposed
	public static void sendStartEvent(final String placementId, WebViewCallback callback) {
		if(UnityAds.getListener() != null) {
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					UnityAds.getListener().onUnityAdsStart(placementId);
				}
			});
		}
		callback.invoke();
	}

	@WebViewExposed
	public static void sendFinishEvent(final String placementId, final String result, WebViewCallback callback) {
		if(UnityAds.getListener() != null) {
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					UnityAds.getListener().onUnityAdsFinish(placementId, UnityAds.FinishState.valueOf(result));
				}
			});
		}
		callback.invoke();
	}

	@WebViewExposed
	public static void sendClickEvent(final String placementId, WebViewCallback callback) {
		if(UnityAds.getListener() != null && UnityAds.getListener() instanceof IUnityAdsExtendedListener) {
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					((IUnityAdsExtendedListener)UnityAds.getListener()).onUnityAdsClick(placementId);
				}
			});
		}
		callback.invoke();
	}

	@WebViewExposed
	public static void sendErrorEvent(final String error, final String message, WebViewCallback callback) {
		if(UnityAds.getListener() != null) {
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					UnityAds.getListener().onUnityAdsError(UnityAds.UnityAdsError.valueOf(error), message);
				}
			});
		}
		callback.invoke();
	}
}