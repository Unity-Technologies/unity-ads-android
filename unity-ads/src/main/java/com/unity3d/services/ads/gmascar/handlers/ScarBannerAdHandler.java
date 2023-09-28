package com.unity3d.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.IScarBannerAdListenerWrapper;
import com.unity3d.services.banners.BannerViewCache;
import com.unity3d.services.banners.bridge.BannerBridge;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

public class ScarBannerAdHandler implements IScarBannerAdListenerWrapper {

	private String _operationId;

	public ScarBannerAdHandler(String operationId) {
		_operationId = operationId;
	}

	@Override
	public void onAdLoaded() {
		BannerViewCache.getInstance().addScarContainer(_operationId);
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.BANNER, BannerBridge.BannerEvent.SCAR_BANNER_LOADED, _operationId);
	}

	@Override
	public void onAdFailedToLoad(int errorCode, String errorString) {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.BANNER, BannerBridge.BannerEvent.SCAR_BANNER_LOAD_FAILED, _operationId, errorCode, errorString);
	}

	@Override
	public void onAdOpened() {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.BANNER, BannerBridge.BannerEvent.SCAR_BANNER_OPENED, _operationId);
	}

	@Override
	public void onAdClicked() {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.BANNER, BannerBridge.BannerEvent.SCAR_BANNER_CLICKED, _operationId);
	}

	@Override
	public void onAdClosed() {
		// Code to be executed when the user is about to return to the app after tapping on an ad.
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.BANNER, BannerBridge.BannerEvent.SCAR_BANNER_CLOSED, _operationId);
	}

	@Override
	public void onAdImpression() {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.BANNER, BannerBridge.BannerEvent.SCAR_BANNER_IMPRESSION, _operationId);
	}
}
