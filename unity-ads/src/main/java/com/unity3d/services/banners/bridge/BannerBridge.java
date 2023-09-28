package com.unity3d.services.banners.bridge;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.services.ads.operation.load.LoadBannerModule;
import com.unity3d.services.ads.operation.load.LoadBannerOperationState;
import com.unity3d.services.banners.BannerErrorCode;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.BannerViewCache;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;
import com.unity3d.services.core.webview.bridge.WebViewBridgeInvoker;

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

	public static void load(String placementId, final String bannerAdId, UnityBannerSize bannerSize, UnityAdsLoadOptions loadOptions) {
		final BannerView bannerAdView = BannerViewCache.getInstance().getBannerView(bannerAdId);
		if (bannerAdView == null) return;
		// success events are handled from webview callbacks, load module failures need to be mapped
		IUnityAdsLoadListener listener = new IUnityAdsLoadListener() {
			@Override
			public void onUnityAdsAdLoaded(String placementId) {
			}

			@Override
			public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
				BannerView bannerAdView = BannerViewCache.getInstance().getBannerView(bannerAdId);
				if (bannerAdView == null || bannerAdView.getListener() == null) {
					return;
				}
				BannerErrorInfo bannerErrorInfo = BannerErrorInfo.fromLoadError(error, message);
				bannerAdView.getListener().onBannerFailedToLoad(bannerAdView, bannerErrorInfo);
			}
		};

		LoadBannerModule.getInstance().executeAdOperation(
			new WebViewBridgeInvoker(),
			new LoadBannerOperationState(placementId, bannerAdId, bannerSize, listener, loadOptions, new ConfigurationReader().getCurrentConfiguration())
		);
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

	public static void didAttachScarBanner(String bannerAdId) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.BANNER, BannerEvent.SCAR_BANNER_ATTACHED, bannerAdId);
		}
	}

	public static void didDetachScarBanner(String bannerAdId) {
		WebViewApp webViewApp = WebViewApp.getCurrentApp();
		if (webViewApp != null) {
			webViewApp.sendEvent(WebViewEventCategory.BANNER, BannerEvent.SCAR_BANNER_DETACHED, bannerAdId);
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
		BANNER_DESTROY_BANNER,
		// Used for SCAR banners only
		SCAR_BANNER_LOADED,
		SCAR_BANNER_LOAD_FAILED,
		SCAR_BANNER_ATTACHED,
		SCAR_BANNER_DETACHED,
		SCAR_BANNER_OPENED,
		SCAR_BANNER_CLOSED,
		SCAR_BANNER_IMPRESSION,
		SCAR_BANNER_CLICKED
	}
}
