package com.unity3d.services.banners;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.unity3d.services.ads.webplayer.WebPlayerSettingsCache;
import com.unity3d.services.banners.bridge.BannerBridge;
import com.unity3d.services.banners.view.BannerWebPlayerContainer;
import com.unity3d.services.core.configuration.IInitializationListener;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.misc.ViewUtilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;

import org.json.JSONObject;

import java.util.UUID;

public class BannerView extends RelativeLayout {

	private String placementId;
	private String viewId;
	private UnityBannerSize size;
	private IListener listener;
	private BannerWebPlayerContainer bannerWebPlayerContainer;
	private IInitializationListener initializationListener;

	// Public

	public BannerView(Activity activity, String placementId, UnityBannerSize size) {
		super(activity);
		this.viewId = UUID.randomUUID().toString();
		this.placementId = placementId;
		this.size = size;
		this.setupLayoutParams();
		this.setBackgroundColor(Color.TRANSPARENT);
		ClientProperties.setActivity(activity);
		BannerViewCache.getInstance().addBannerView(this);
	}

	public String getPlacementId() {
		return placementId;
	}

	public UnityBannerSize getSize() {
		return size;
	}

	public void setListener(IListener listener) {
		this.listener = listener;
	}

	public IListener getListener() {
		return listener;
	}

	public void load() {
		if (SdkProperties.isInitialized()) {
			this.bridgeLoad();
		} else {
			this.registerInitializeListener();
		}
	}

	public void destroy() {
		// Remove this view from cache
		BannerViewCache.getInstance().removeBannerView(this.viewId);

		// Remove this view from initialize notification center
		this.unregisterInitializeListener();

		// Inform WebView that this banner is being destroyed
		BannerBridge.destroy(this.placementId);

		// Remove from parent view
		final BannerView self = this;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ViewParent parent = self.getParent();
				if (parent != null && parent instanceof android.view.ViewManager) {
					((android.view.ViewManager) parent).removeView(self);
				}
			}
		});

		// Destroy the view
		if (bannerWebPlayerContainer != null) {
			bannerWebPlayerContainer.destroy();
		}

		// Log the banner was destroyed
		DeviceLog.info("Banner [" + this.placementId + "] was destroyed");

		// Null all instance variables
		this.placementId = null;
		this.viewId = null;
		this.size = null;
		this.listener = null;
		this.bannerWebPlayerContainer = null;
	}

	public interface IListener {
		void onBannerLoaded(BannerView bannerAdView);

		void onBannerClick(BannerView bannerAdView);

		void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo);

		void onBannerLeftApplication(BannerView bannerView);
	}

	public static abstract class Listener implements IListener {
		@Override
		public void onBannerLoaded(BannerView bannerAdView) {
		}

		@Override
		public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
		}

		@Override
		public void onBannerClick(BannerView bannerAdView) {
		}

		@Override
		public void onBannerLeftApplication(BannerView bannerAdView) {
		}
	}

	// Module Private

	void loadWebPlayer(final UnityBannerSize unityBannerSize) {
		final BannerView self = this;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				JSONObject settings = WebPlayerSettingsCache.getInstance().getWebSettings(self.viewId);
				JSONObject playerSettings = WebPlayerSettingsCache.getInstance().getWebPlayerSettings(self.viewId);
				JSONObject eventSettings = WebPlayerSettingsCache.getInstance().getWebPlayerEventSettings(self.viewId);
				if (self.bannerWebPlayerContainer == null) {
					self.bannerWebPlayerContainer = new BannerWebPlayerContainer(self.getContext(), self.viewId, settings, playerSettings, eventSettings, unityBannerSize);
					self.addView(self.bannerWebPlayerContainer);
				} else {
					self.bannerWebPlayerContainer.setWebPlayerSettings(settings, playerSettings);
					self.bannerWebPlayerContainer.setWebPlayerEventSettings(eventSettings);
				}
			}
		});

	}

	String getViewId() {
		return viewId;
	}

	// Private

	private void registerInitializeListener() {
		this.unregisterInitializeListener();
		final BannerView bannerView = this;
		this.initializationListener = new IInitializationListener() {
			public void onSdkInitialized() {
				bannerView.unregisterInitializeListener();
				bannerView.bridgeLoad();
			}

			@Override
			public void onSdkInitializationFailed(String message, int code) {
				bannerView.unregisterInitializeListener();
				if (bannerView.getListener() != null) {
					bannerView.getListener().onBannerFailedToLoad(bannerView, new BannerErrorInfo("UnityAds sdk initialization failed", BannerErrorCode.NATIVE_ERROR));
				}
			}
		};
		InitializationNotificationCenter.getInstance().addListener(this.initializationListener);
	}

	private void unregisterInitializeListener() {
		if (this.initializationListener != null) {
			InitializationNotificationCenter.getInstance().removeListener(this.initializationListener);
		}
		this.initializationListener = null;
	}

	private void setupLayoutParams() {
		final int width = Math.round(ViewUtilities.pxFromDp(getContext(), this.size.getWidth()));
		final int height = Math.round(ViewUtilities.pxFromDp(getContext(), this.size.getHeight()));
		LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
		this.setLayoutParams(layoutParams);
		this.setGravity(Gravity.CENTER);
		this.requestLayout();
	}

	private void bridgeLoad() {
		BannerBridge.load(this.placementId, this.viewId, this.size);
	}
}
