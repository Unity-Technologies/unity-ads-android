package com.unity3d.services.banners;

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
			final BannerView.IListener listener = bannerView.getListener();
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listener.onBannerLoaded(bannerView);
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
					listener.onBannerClick(bannerView);
				}
			});
		}
	}

	public synchronized void triggerBannerErrorEvent(String bannerAdId, final BannerErrorInfo bannerErrorInfo) {
		final BannerView bannerView = this.getBannerView(bannerAdId);
		if (bannerView != null && bannerView.getListener() != null) {
			final BannerView.IListener listener = bannerView.getListener();
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listener.onBannerFailedToLoad(bannerView, bannerErrorInfo);
				}
			});
		}
	}

	public synchronized void triggerBannerLeftApplicationEvent(String bannerAdId) {
		final BannerView bannerView = this.getBannerView(bannerAdId);
		if (bannerView != null && bannerView.getListener() != null) {
			final BannerView.IListener listener = bannerView.getListener();
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listener.onBannerLeftApplication(bannerView);
				}
			});
		}
	}

}
