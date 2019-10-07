package com.unity3d.services.banners.api;

import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.bridge.BannerBridge;
import com.unity3d.services.banners.BannerViewCache;
import com.unity3d.services.banners.properties.BannerRefreshInfo;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class Banner {

	private enum BannerViewType {
		WEB_PLAYER,
		UNKNOWN;

		public static BannerViewType fromString(String type) {
			try {
				return BannerViewType.valueOf(type);
			} catch (IllegalArgumentException e) {
				return BannerViewType.UNKNOWN;
			}
		}
	}

	@WebViewExposed
	public static void load(final String bannerViewTypeString, final Integer width, final Integer height, final String bannerAdId, final WebViewCallback callback) {
		final BannerViewType bannerViewType = BannerViewType.fromString(bannerViewTypeString);
		switch (bannerViewType) {
			case WEB_PLAYER:
				boolean successfullyLoaded = BannerViewCache.getInstance().loadWebPlayer(bannerAdId, new UnityBannerSize(width, height));
				if (successfullyLoaded) {
					BannerBridge.didLoad(bannerAdId);
				}
				break;
			case UNKNOWN:
				// do nothing
				break;
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void setRefreshRate(final String placementId, final Integer refreshRate, WebViewCallback callback) {
		if (placementId != null && refreshRate != null) {
			BannerRefreshInfo.getInstance().setRefreshRate(placementId, refreshRate);
		}
		callback.invoke();
	}

}
