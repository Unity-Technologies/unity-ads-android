package com.unity3d.services.banners.api;

import static com.unity3d.ads.UnityAds.UnityAdsLoadError.INTERNAL_ERROR;

import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.services.ads.operation.load.ILoadOperation;
import com.unity3d.services.ads.operation.load.LoadBannerModule;
import com.unity3d.services.ads.operation.load.LoadBannerOperationState;
import com.unity3d.services.ads.operation.load.LoadOperationState;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.bridge.BannerBridge;
import com.unity3d.services.banners.BannerViewCache;
import com.unity3d.services.banners.properties.BannerRefreshInfo;
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
				final LoadBannerOperationState state = getBannerOperationState(bannerAdId);
				if (state == null) {
					break;
				}

				state.setSize(new UnityBannerSize(width, height));
				BannerViewCache.getInstance().loadBanner(state);
				break;
			case UNKNOWN:
				LoadBannerModule.getInstance().onUnityAdsFailedToLoad(bannerAdId, INTERNAL_ERROR, "Unknown banner type");
				break;
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void loadScar(final String bannerAdId, final String placementId, final String queryId, final String adUnitId, final String adString, final Integer width, final Integer height, final WebViewCallback callback) {
		final LoadBannerOperationState state = getBannerOperationState(bannerAdId);
		if (state == null) {
			callback.invoke();
			return;
		}

		state.setSize(new UnityBannerSize(width, height));

		ScarAdMetadata scarAdMetadata = new ScarAdMetadata(placementId, queryId, adUnitId, adString, 0);
		state.setScarAdMetadata(scarAdMetadata);

		BannerViewCache.getInstance().loadBanner(state);

		callback.invoke();
	}

	@WebViewExposed
	public static void setRefreshRate(final String placementId, final Integer refreshRate, WebViewCallback callback) {
		if (placementId != null && refreshRate != null) {
			BannerRefreshInfo.getInstance().setRefreshRate(placementId, refreshRate);
		}
		callback.invoke();
	}

	private static LoadBannerOperationState getBannerOperationState(String bannerAdId) {
		ILoadOperation operationState = LoadBannerModule.getInstance().get(bannerAdId);
		if (operationState == null || operationState.getLoadOperationState() == null) {
			LoadBannerModule.getInstance().onUnityAdsFailedToLoad(bannerAdId, INTERNAL_ERROR, "No operation found for requested banner");
			return null;
		}

		LoadOperationState state = operationState.getLoadOperationState();
		if (state instanceof LoadBannerOperationState) {
			return (LoadBannerOperationState) state;
		}

		LoadBannerModule.getInstance().onUnityAdsFailedToLoad(bannerAdId, INTERNAL_ERROR, "Operation state found is not for banner ad");
		return null;
	}

}
