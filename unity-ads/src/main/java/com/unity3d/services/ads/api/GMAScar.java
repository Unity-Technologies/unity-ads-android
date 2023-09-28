package com.unity3d.services.ads.api;

import com.unity3d.scar.adapter.common.scarads.UnityAdFormat;
import com.unity3d.services.ads.gmascar.GMAScarAdapterBridge;
import com.unity3d.services.ads.gmascar.GMA;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;

public class GMAScar {

	private static final GMAScarAdapterBridge gmaScarAdapterBridge = GMA.getInstance().getBridge();

	@WebViewExposed
	public static void initializeScar(final WebViewCallback callback) {
		gmaScarAdapterBridge.initializeScar();
		callback.invoke();
	}

	@WebViewExposed
	public static void getVersion(final WebViewCallback callback) {
		gmaScarAdapterBridge.getVersion();
		callback.invoke();
	}

	public static void isInitialized(final WebViewCallback callback) {
		gmaScarAdapterBridge.isInitialized();
		callback.invoke();
	}

	@WebViewExposed
	public static void getSCARSignal(final String placementId, final String adFormatStr, final WebViewCallback callback) {
		gmaScarAdapterBridge.getSCARSignal(placementId, UnityAdFormat.valueOf(adFormatStr.toUpperCase()));
		callback.invoke();
	}

	@WebViewExposed
	public static void load(final String placementId, final String queryId, final Boolean canSkip, final String adUnitId, final String adString, final Integer videoLengthMs, final WebViewCallback callback) {
		gmaScarAdapterBridge.load(canSkip, placementId, queryId, adString, adUnitId, videoLengthMs);
		callback.invoke();
	}

	@WebViewExposed
	public static void show(final String placementId, final String queryId, final Boolean canSkip, final WebViewCallback callback) {
		gmaScarAdapterBridge.show(placementId, queryId, canSkip);
		callback.invoke();
	}
}
