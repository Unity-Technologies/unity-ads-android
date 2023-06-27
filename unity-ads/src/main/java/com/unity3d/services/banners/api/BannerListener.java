package com.unity3d.services.banners.api;

import com.unity3d.services.banners.BannerErrorCode;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerViewCache;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class BannerListener {

	@WebViewExposed
	public static void sendLoadEvent(final String bannerAdId, WebViewCallback callback) {
		BannerViewCache.getInstance().triggerBannerLoadEvent(bannerAdId);
		callback.invoke();
	}

	@WebViewExposed
	public static void sendShowEvent(final String bannerAdId, WebViewCallback callback) {
		BannerViewCache.getInstance().triggerBannerShowEvent(bannerAdId);
		callback.invoke();
	}

	@WebViewExposed
	public static void sendClickEvent(final String bannerAdId, WebViewCallback callback) {
		BannerViewCache.getInstance().triggerBannerClickEvent(bannerAdId);
		callback.invoke();
	}

	@WebViewExposed
	public static void sendErrorEvent(final String bannerAdId, final Integer errorCode, final String message, WebViewCallback callback) {
		BannerErrorInfo bannerErrorInfo = new BannerErrorInfo(message, BannerErrorCode.values()[errorCode]);
		BannerViewCache.getInstance().triggerBannerErrorEvent(bannerAdId, bannerErrorInfo);
		callback.invoke();
	}

	@WebViewExposed
	public static void sendLeaveApplicationEvent(final String bannerAdId, WebViewCallback callback) {
		BannerViewCache.getInstance().triggerBannerLeftApplicationEvent(bannerAdId);
		callback.invoke();
	}

}
