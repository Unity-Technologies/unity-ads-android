package com.unity3d.ads.api;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.mediation.IUnityAdsExtendedListener;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

public class Listener {
	@WebViewExposed
	public static void sendReadyEvent(final String placementId, WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (UnityAds.getListener() != null) {
					UnityAds.getListener().onUnityAdsReady(placementId);
				}
			}
		});

		callback.invoke();
	}

	@WebViewExposed
	public static void sendStartEvent(final String placementId, WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (UnityAds.getListener() != null) {
					UnityAds.getListener().onUnityAdsStart(placementId);
				}
			}
		});

		callback.invoke();
	}

	@WebViewExposed
	public static void sendFinishEvent(final String placementId, final String result, WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (UnityAds.getListener() != null) {
					UnityAds.getListener().onUnityAdsFinish(placementId, UnityAds.FinishState.valueOf(result));
				}
			}
		});

		callback.invoke();
	}

	@WebViewExposed
	public static void sendClickEvent(final String placementId, WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (UnityAds.getListener() != null && UnityAds.getListener() instanceof IUnityAdsExtendedListener) {
					((IUnityAdsExtendedListener)UnityAds.getListener()).onUnityAdsClick(placementId);
				}
			}
		});

		callback.invoke();
	}

	@WebViewExposed
	public static void sendPlacementStateChangedEvent(final String placementId, final String oldState, final String newState, WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (UnityAds.getListener() != null && UnityAds.getListener() instanceof IUnityAdsExtendedListener) {
					((IUnityAdsExtendedListener)UnityAds.getListener()).onUnityAdsPlacementStateChanged(placementId, UnityAds.PlacementState.valueOf(oldState), UnityAds.PlacementState.valueOf(newState));
				}
			}
		});

		callback.invoke();
	}

	@WebViewExposed
	public static void sendErrorEvent(final String error, final String message, WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (UnityAds.getListener() != null) {
					UnityAds.getListener().onUnityAdsError(UnityAds.UnityAdsError.valueOf(error), message);
				}
			}
		});

		callback.invoke();
	}
}