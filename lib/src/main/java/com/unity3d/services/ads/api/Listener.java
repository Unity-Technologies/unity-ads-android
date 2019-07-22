package com.unity3d.services.ads.api;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.mediation.IUnityAdsExtendedListener;
import com.unity3d.services.ads.properties.AdsProperties;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class Listener {
	@WebViewExposed
	public static void sendReadyEvent(final String placementId, WebViewCallback callback) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (IUnityAdsListener listener : AdsProperties.getListeners()) {
					listener.onUnityAdsReady(placementId);
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
				for (IUnityAdsListener listener : AdsProperties.getListeners()) {
					listener.onUnityAdsStart(placementId);
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
				for (IUnityAdsListener listener : AdsProperties.getListeners()) {
					listener.onUnityAdsFinish(placementId, UnityAds.FinishState.valueOf(result));
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
				for (IUnityAdsListener listener : AdsProperties.getListeners()) {
					if (listener instanceof IUnityAdsExtendedListener) {
						((IUnityAdsExtendedListener)listener).onUnityAdsClick(placementId);
					}

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
				for (IUnityAdsListener listener : AdsProperties.getListeners()) {
					if (listener instanceof IUnityAdsExtendedListener) {
						((IUnityAdsExtendedListener)listener).onUnityAdsPlacementStateChanged(placementId, UnityAds.PlacementState.valueOf(oldState), UnityAds.PlacementState.valueOf(newState));
					}
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
				for (IUnityAdsListener listener : AdsProperties.getListeners()) {
					if (listener instanceof IUnityAdsExtendedListener) {
						listener.onUnityAdsError(UnityAds.UnityAdsError.valueOf(error), message);
					}
				}
			}
		});

		callback.invoke();
	}
}