package com.unity3d.services.banners.bridge;

import com.unity3d.services.banners.BannerErrorCode;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.BannerViewCache;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

public class BannerBridge {

	public static void load(String placementId, String bannerAdId, UnityBannerSize bannerSize) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_LOAD_PLACEMENT, placementId, bannerAdId, bannerSize.getWidth(), bannerSize.getHeight());
		} else {
			BannerView bannerAdView = BannerViewCache.getInstance().getBannerView(bannerAdId);
			if (bannerAdView != null && bannerAdView.getListener() != null) {
				bannerAdView.getListener().onBannerFailedToLoad(bannerAdView, new BannerErrorInfo("WebViewApp was not available, this is likely because UnityAds has not been initialized", BannerErrorCode.WEBVIEW_ERROR));
			}
		}
	}

	public static void destroy(String bannerAdId) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_DESTROY_BANNER, bannerAdId);
		}
	}

	public static void resize(String bannerAdId, int left, int top, int right, int bottom, float alpha) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_RESIZED, bannerAdId, left, top, right, bottom, alpha);
		}
	}

	public static void visibilityChanged(String bannerAdId, int visibility) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_VISIBILITY_CHANGED, bannerAdId, visibility);
		}
	}

	public static void didLoad(String bannerAdId) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_LOADED, bannerAdId);
		}
	}

	public static void didDestroy(String bannerAdId) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_DESTROYED, bannerAdId);
		}
	}

	public static void didAttach(String bannerAdId) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_ATTACHED, bannerAdId);
		}
	}

	public static void didDetach(String bannerAdId) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_DETACHED, bannerAdId);
		}
	}

	public enum BannerEvent {
		BANNER_VISIBILITY_CHANGED,
		BANNER_RESIZED,
		BANNER_LOADED,
		BANNER_DESTROYED,
		BANNER_ATTACHED,
		BANNER_DETACHED,
		BANNER_LOAD_PLACEMENT,
		BANNER_DESTROY_BANNER
	}
}
