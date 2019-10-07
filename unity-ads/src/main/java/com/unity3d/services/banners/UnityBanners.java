package com.unity3d.services.banners;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.placement.Placement;
import com.unity3d.services.banners.view.BannerPosition;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.misc.ViewUtilities;
import com.unity3d.services.core.properties.ClientProperties;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.RelativeLayout;

import com.unity3d.services.banners.properties.BannerRefreshInfo;

import java.util.concurrent.TimeUnit;

public final class UnityBanners {

	private static UnityBanners instance;

	private static UnityBanners getInstance() {
		if (instance == null) {
			instance = new UnityBanners();
		}
		return instance;
	}

	private class BannerWrapper extends FrameLayout {

		private BannerPosition _bannerPosition;
		private BannerAdRefreshView _bannerAdRefreshView;

		public BannerWrapper(Context context, BannerAdRefreshView bannerAdRefreshView) {
			super(context);
			_bannerPosition = BannerPosition.NONE;
			_bannerAdRefreshView = bannerAdRefreshView;
			this.addView(_bannerAdRefreshView);
			this.setupLayoutConstraints();
			this.setBackgroundColor(Color.TRANSPARENT);
		}

		public void setBannerPosition(BannerPosition bannerPosition) {
			_bannerPosition = bannerPosition;
			this.setupLayoutConstraints();
		}

		private void setupLayoutConstraints() {
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.gravity = this._bannerPosition.getGravity();
			this.setLayoutParams(params);
		}

		public void destroy() {
			final BannerWrapper bannerWrapper = this;
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					bannerWrapper.removeAllViews();
					ViewUtilities.removeViewFromParent(bannerWrapper);
				}
			});

			if (this._bannerAdRefreshView != null) {
				this._bannerAdRefreshView.destroy();
				this._bannerAdRefreshView = null;
			}
		}

	}

	private IUnityBannerListener _bannerListener;
	private BannerPosition _currentBannerPosition;
	private BannerWrapper _currentBannerWrapper;

	private UnityBanners() {
		_currentBannerPosition = BannerPosition.NONE;
	}

	private void _loadBanner(Activity activity, String placementId) {
		if (_currentBannerWrapper == null) {
			BannerAdRefreshView bannerAdRefreshView = new BannerAdRefreshView(activity, placementId, UnityBannerSize.getDynamicSize(activity));
			final BannerWrapper bannerWrapper = new BannerWrapper(activity, bannerAdRefreshView);
			bannerWrapper.setBannerPosition(_currentBannerPosition);
			_currentBannerWrapper = bannerWrapper;
			final UnityBanners self = this;
			bannerAdRefreshView.setListener(new BannerView.Listener() {
				@Override
				public void onBannerLoaded(BannerView bannerView) {
					if (self._bannerListener != null) {
						self._bannerListener.onUnityBannerLoaded(bannerView.getPlacementId(), bannerWrapper);
					}
				}

				@Override
				public void onBannerFailedToLoad(BannerView bannerView, BannerErrorInfo errorInfo) {
					if (self._bannerListener != null) {
						self._bannerListener.onUnityBannerError(bannerView.getPlacementId()+ " " + errorInfo.errorMessage);
					}
				}

				@Override
				public void onBannerClick(BannerView bannerView) {
					if (self._bannerListener != null) {
						self._bannerListener.onUnityBannerClick(bannerView.getPlacementId());
					}
				}
			});
			bannerAdRefreshView.load();
		} else {
			sendError("A Banner is already in use, please call destroy before loading another banner!");
		}
	}

	private void _destroy() {
		if (_currentBannerWrapper != null) {
			_currentBannerWrapper.destroy();
			_currentBannerWrapper = null;
		}
	}

	private static void sendError(final String message) {
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				IUnityBannerListener listener = getBannerListener();
				if (listener != null) {
					listener.onUnityBannerError(message);
				}
			}
		});
	}

	// ********* CAUTION START *****************
	// OLD BANNER API THAT DOES NOT SUPPORT MULTIPLE BANNERS

	@Deprecated
	public static void loadBanner(Activity activity) {
		UnityBanners.loadBanner(activity, Placement.getDefaultBannerPlacement());
	}

	@Deprecated
	public static void loadBanner(final Activity activity, final String placementId) {
		DeviceLog.entered();
		if (!UnityAds.isSupported()) {
			sendError("Unity Ads is not supported on this device.");
		}
		if (!UnityAds.isInitialized()) {
			sendError("UnityAds is not initialized.");
			return;
		}
		if (!UnityAds.isReady(placementId)) {
			sendError("Banner placement " + placementId + " is not ready");
			return;
		}

		ClientProperties.setActivity(activity);
		UnityBanners.getInstance()._loadBanner(activity, placementId);
	}

	@Deprecated
	public static void destroy() {
		UnityBanners.getInstance()._destroy();
	}

	/**
	 * Change listener for IUnityAdsListener callbacks
	 *
	 * @param listener New listener for IUnityAdsListener callbacks
	 */
	@Deprecated
	public static void setBannerListener(IUnityBannerListener listener) {
		UnityBanners.getInstance()._bannerListener = listener;
	}

	/**
	 * Get current listener for IUnityAdsListener callbacks
	 *
	 * @return Current listener for IUnityAdsListener callbacks
	 */
	@Deprecated
	public static IUnityBannerListener getBannerListener() {
		return UnityBanners.getInstance()._bannerListener;
	}

	@Deprecated
	public static void setBannerPosition(BannerPosition position) {
		UnityBanners.getInstance()._currentBannerPosition = position;
	}

	//Private class UnityBannerAdRefreshView

	private class BannerAdRefreshView extends RelativeLayout {

		private String placementId;
		private boolean didLoad = false;
		private long refreshRate = 30;
		private BannerView bannerView;
		private boolean didShow = false;
		private boolean didSubscribeToLifecycle = false;
		private LifecycleListener lifecycleListener; // Do not use unless api version 14 or greater

		private Handler refreshHandler;
		private Runnable reloadRunnable;
		private long refreshTime;

		public BannerAdRefreshView(Activity activity, String placementId, UnityBannerSize size) {
			super(activity);
			this.placementId = placementId;
			this.refreshHandler = new Handler();
			final BannerAdRefreshView self = this;
			this.reloadRunnable = new Runnable() {
				@Override
				public void run() {
					self.reload();
				}
			};
			this.setupLayoutParams();
			this.setBackgroundColor(Color.TRANSPARENT);
			this.bannerView = new BannerView(activity, placementId, size);
			this.addView(this.bannerView);
		}

		public String getPlacementId() {
			return this.bannerView.getPlacementId();
		}

		public UnityBannerSize getSize() {
			return this.bannerView.getSize();
		}

		public void setListener(BannerView.IListener listener) {
			this.bannerView.setListener(listener);
		}

		public BannerView.IListener getListener() {
			return this.bannerView.getListener();
		}

		public void load() {
			if (!didLoad) {
				didLoad = true;
				Integer refreshRateInteger = BannerRefreshInfo.getInstance().getRefreshRate(this.placementId);
				if (refreshRateInteger != null) {
					this.refreshRate = refreshRateInteger.longValue();
				}
				this.reload();
			}
		}

		public void destroy() {
			this.stopReloadTask();
			this.bannerView.destroy();
			// remove all sub views
			final BannerAdRefreshView self = this;
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					self.removeAllViews();
				}
			});

			this.bannerView = null;
		}

		private void startReloadTask() {
			long currentTime = SystemClock.uptimeMillis();
			if (currentTime < this.refreshTime) {
				// The timer would not have been triggered yet so we should schedule to trigger at that time
				this.refreshHandler.postAtTime(this.reloadRunnable, this.refreshTime);
			} else {
				// then we should refresh and start the timer from 0
				this.reload();
			}
		}

		private void stopReloadTask() {
			if (this.refreshHandler != null && this.reloadRunnable != null) {
				this.refreshHandler.removeCallbacks(this.reloadRunnable);
			}
		}

		private void reload() {
			this.bannerView.load();
			// Schedule the next reload
			long currentTime = SystemClock.uptimeMillis();
			this.refreshTime = currentTime + TimeUnit.SECONDS.toMillis(this.refreshRate);
			this.refreshHandler.postAtTime(this.reloadRunnable, this.refreshTime);
		}

		private void subscribeToLifecycle() {
			if (!this.didSubscribeToLifecycle
				&& Build.VERSION.SDK_INT >= 14
				&& ClientProperties.getApplication() != null) {
				final BannerAdRefreshView self = this;
				this.lifecycleListener = new LifecycleListener() {
					@Override
					public void onActivityPaused(Activity activity) {
						self.stopReloadTask();
					}

					@Override
					public void onActivityStopped(Activity activity) {
						self.stopReloadTask();
					}

					@Override
					public void onActivityDestroyed(Activity activity) {
						self.stopReloadTask();
					}

					@Override
					public void onActivityResumed(Activity activity) {
						self.startReloadTask();
					}
				};
				this.didSubscribeToLifecycle = true;
				ClientProperties.getApplication().registerActivityLifecycleCallbacks(lifecycleListener);
			}
		}

		private void unsubscribeFromLifecycle() {
			if (this.didSubscribeToLifecycle
				&& Build.VERSION.SDK_INT >= 14
				&& this.lifecycleListener != null
				&& ClientProperties.getApplication() != null) {
				this.didSubscribeToLifecycle = false;
				ClientProperties.getApplication().unregisterActivityLifecycleCallbacks(this.lifecycleListener);
			}
		}

		@Override
		protected void onDetachedFromWindow() {
			super.onDetachedFromWindow();
			this.stopReloadTask();
			this.unsubscribeFromLifecycle();
		}

		@Override
		protected void onAttachedToWindow() {
			super.onAttachedToWindow();
			this.subscribeToLifecycle();
			if (this.didShow) {
				this.startReloadTask();
			} else {
				// skip the first attach
				this.didShow = true;
			}
		}

		private void setupLayoutParams() {
			LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			this.setLayoutParams(layoutParams);
		}

		@TargetApi(14)
		private class LifecycleListener implements Application.ActivityLifecycleCallbacks {
			@Override
			public void onActivityCreated(Activity activity, Bundle bundle) {
				// do nothing
			}

			@Override
			public void onActivityStarted(Activity activity) {
				// do nothing
			}

			@Override
			public void onActivityResumed(Activity activity) {
				// do nothing
			}

			@Override
			public void onActivityPaused(Activity activity) {
				// do nothing
			}

			@Override
			public void onActivityStopped(Activity activity) {
				// do nothing
			}

			@Override
			public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
				// do nothing
			}

			@Override
			public void onActivityDestroyed(Activity activity) {
				// do nothing
			}
		}

	}

}

