package com.unity3d.services.banners;

import com.unity3d.ads.UnityAds;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.services.ads.operation.load.*;
import com.unity3d.services.banners.bridge.BannerBridge;
import com.unity3d.services.core.misc.Utilities;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class BannerViewCache {

	private static BannerViewCache instance;

	public static BannerViewCache getInstance() {
		if (instance == null) {
			instance = new BannerViewCache();
		}
		return instance;
	}

	private HashMap<String, WeakReference<BannerView>> _bannerViews;

	public BannerViewCache() {
		_bannerViews = new HashMap<>();
	}

	public synchronized String addBannerView(BannerView bannerAdView) {
		WeakReference<BannerView> weakBannerView = new WeakReference<>(bannerAdView);
		_bannerViews.put(bannerAdView.getViewId(), weakBannerView);
		return bannerAdView.getViewId();
	}

	public synchronized BannerView getBannerView(String bannerAdId) {
		WeakReference<BannerView> weakBannerView =  _bannerViews.get(bannerAdId);
		if (weakBannerView != null && weakBannerView.get() != null) {
			return weakBannerView.get();
		} else {
			return null;
		}
	}

	public synchronized void removeBannerView(String bannerAdId) {
		_bannerViews.remove(bannerAdId);
	}

	public synchronized void loadBanner(LoadBannerOperationState state) {
		String bannerAdId = state.getId();
		UnityBannerSize size = state.getSize();

		if (state.isScarAd()) {
			ScarAdMetadata scarAdMetadata = state.getScarAdMetadata();
			loadScarPlayer(bannerAdId, scarAdMetadata, size);
		} else {
			boolean successfullyLoaded = loadWebPlayer(bannerAdId, size);
			if (successfullyLoaded) {
				BannerBridge.didLoad(bannerAdId);
			}
		}
	}

	public synchronized void loadScarPlayer(String bannerAdId, ScarAdMetadata scarAdMetadata, UnityBannerSize size) {
		BannerView bannerView = this.getBannerView(bannerAdId);

		if (bannerView != null) {
			bannerView.loadScarPlayer(bannerAdId, scarAdMetadata, size);
		}
	}

	public synchronized void addScarContainer(String bannerAdId) {
		BannerView bannerView = this.getBannerView(bannerAdId);

		if (bannerView != null) {
			bannerView.addScarContainer();
		}
	}

	public synchronized boolean loadWebPlayer(String bannerAdId, UnityBannerSize size) {
		BannerView bannerView = this.getBannerView(bannerAdId);
		if (bannerView != null) {
			bannerView.loadWebPlayer(size);
			return true;
		} else {
			return false;
		}
	}

	public synchronized void triggerBannerLoadEvent(String bannerAdId) {
		final BannerView bannerView = this.getBannerView(bannerAdId);
		if (bannerView != null && bannerView.getListener() != null) {
			LoadBannerModule.getInstance().onUnityAdsAdLoaded(bannerAdId);

			final BannerView.IListener listener = bannerView.getListener();
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (listener != null) {
						listener.onBannerLoaded(bannerView);
					}
				}
			});
		}
	}

	public synchronized void triggerBannerShowEvent(String bannerAdId) {
		final BannerView bannerView = this.getBannerView(bannerAdId);
		if (bannerView != null && bannerView.getListener() != null) {
			final BannerView.IListener listener = bannerView.getListener();
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (listener != null) {
						listener.onBannerShown(bannerView);
					}
				}
			});
		}
	}

	public synchronized void triggerBannerClickEvent(String bannerAdId) {
		final BannerView bannerView = this.getBannerView(bannerAdId);
		if (bannerView != null && bannerView.getListener() != null) {
			final BannerView.IListener listener = bannerView.getListener();
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (listener != null) {
						listener.onBannerClick(bannerView);
					}
				}
			});
		}
	}

	public synchronized void triggerBannerErrorEvent(String bannerAdId, final BannerErrorInfo bannerErrorInfo) {
		final UnityAds.UnityAdsLoadError unityAdsLoadError = bannerErrorInfo.toLoadError();
		LoadBannerModule.getInstance().onUnityAdsFailedToLoad(bannerAdId, unityAdsLoadError, bannerErrorInfo.errorMessage);
	}

	public synchronized void triggerBannerLeftApplicationEvent(String bannerAdId) {
		final BannerView bannerView = this.getBannerView(bannerAdId);
		if (bannerView != null && bannerView.getListener() != null) {
			final BannerView.IListener listener = bannerView.getListener();
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (listener != null) {
						listener.onBannerLeftApplication(bannerView);
					}
				}
			});
		}
	}

}
