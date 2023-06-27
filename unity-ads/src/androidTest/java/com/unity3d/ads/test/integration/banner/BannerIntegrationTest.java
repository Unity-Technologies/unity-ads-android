package com.unity3d.ads.test.integration.banner;

import androidx.test.rule.ActivityTestRule;
import android.view.View;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.test.hybrid.HybridTestActivity;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.IUnityBannerListener;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.UnityBanners;
import com.unity3d.services.banners.view.BannerPosition;
import com.unity3d.services.core.configuration.ErrorState;
import com.unity3d.services.core.configuration.IInitializationListener;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;
import com.unity3d.services.core.misc.Utilities;


import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.concurrent.Semaphore;

import static junit.framework.Assert.*;

public class BannerIntegrationTest {
	@ClassRule
	public static final ActivityTestRule<HybridTestActivity> _activityRule = new ActivityTestRule<>(HybridTestActivity.class);
	BannerView callbackbannerView;
	View oldBannerView;
	BannerView bannerView;
	static IInitializationListener initializationListener;

	@BeforeClass
	public static void setUpOnce() throws InterruptedException {
		final Semaphore initializeSemaphore = new Semaphore(0);
		initializationListener = new IInitializationListener() {
			@Override
			public void onSdkInitialized() {
				InitializationNotificationCenter.getInstance().removeListener(initializationListener);
				initializeSemaphore.release();
			}

			@Override
			public void onSdkInitializationFailed(String message, ErrorState errorState, int code) {
				InitializationNotificationCenter.getInstance().removeListener(initializationListener);
				fail("Failed to initialize");
				initializeSemaphore.release();
			}
		};
		InitializationNotificationCenter.getInstance().addListener(initializationListener);
		UnityAds.initialize(_activityRule.getActivity(), "14851", true);
		initializeSemaphore.acquire();
	}

	@Test(timeout = 100000)
	public void LegacyBannerTest() throws InterruptedException {
		final Semaphore _loadedSemaphore = new Semaphore(0);
		final Semaphore _shownSemaphore = new Semaphore(0);
		final UnityBannerListener listener = new UnityBannerListener() {
			@Override
			public void onUnityBannerLoaded(String placementId, View view)
			{
				oldBannerView = view;
				_loadedSemaphore.release();
				Utilities.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (oldBannerView.getParent() == null) {
							_activityRule.getActivity().addContentView(oldBannerView, oldBannerView.getLayoutParams());
						}
					}
				});
			}

			@Override
			public void onUnityBannerShow(String placementId) {
				_shownSemaphore.release();
			}

			@Override
			public void onUnityBannerClick(String placementId) {

			}

			@Override
			public void onUnityBannerError(String message) {
				_loadedSemaphore.release();
				_shownSemaphore.release();
				fail("Banner error encountered " + message);
			}
		};
		UnityBanners.setBannerListener(listener);
		UnityBanners.setBannerPosition(BannerPosition.BOTTOM_CENTER);
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				UnityBanners.loadBanner(_activityRule.getActivity(), "bannerads");
				// TODO temporary solution until onUnityBannerShow is triggered from webview
				listener.onUnityBannerShow("bannerads");
			}
		});
		_loadedSemaphore.acquire();
		assertNotNull(oldBannerView);

		_shownSemaphore.acquire();
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				UnityBanners.destroy();
			}
		});
		oldBannerView = null;
	}

//	@Test(timeout = 100000)
//	public void BannerTest() throws InterruptedException {
//		// There may be a timing issue with this test where webview has not finished initializing fully
//		// Even though webview tells native it is initialized
//		UnityBannerSize unityBannerSize = new UnityBannerSize(320, 50);
//		bannerView = new BannerView(_activityRule.getActivity(), "bannerads", unityBannerSize);
//		final Semaphore _loadedSemaphore = new Semaphore(0);
//		final Semaphore _clickSemaphore = new Semaphore(0);
//		final Semaphore _shownSemaphore = new Semaphore(0);
//
//		final BannerView.IListener listener = new BannerView.IListener() {
//			public void onBannerLoaded(BannerView bannerAdView) {
//				callbackbannerView = bannerAdView;
//				_loadedSemaphore.release();
//			}
//
//			@Override
//			public void onBannerShown(BannerView bannerAdView) {
//				callbackbannerView = bannerAdView;
//				_shownSemaphore.release();
//			}
//
//			public void onBannerClick(BannerView bannerAdView) {
//				callbackbannerView = bannerAdView;
//				_clickSemaphore.release();
//			}
//
//			public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo bannerErrorInfo) {
//				_loadedSemaphore.release();
//				_clickSemaphore.release();
//				_shownSemaphore.release();
//				fail("Banner error encountered " + bannerErrorInfo.errorMessage);
//			}
//
//			public void onBannerLeftApplication(BannerView bannerView) {
//
//			}
//		};
//		bannerView.setListener(listener);
//		bannerView.load();
//		_loadedSemaphore.acquire();
//		assertEquals(bannerView, callbackbannerView);
//
//		Utilities.runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				if (bannerView.getParent() == null) {
//					_activityRule.getActivity().addContentView(bannerView, bannerView.getLayoutParams());
//					// TODO temporary solution until onUnityBannerShow is triggered from webview
//					listener.onBannerShown(bannerView);
//				}
//			}
//		});
//	}

	@Test(timeout = 100000)
	public void BannerTestFailedToLoad() throws InterruptedException {
		// There may be a timing issue with this test where webview has not finished initializing fully
		// Even though webview tells native it is initialized
		UnityBannerSize unityBannerSize = new UnityBannerSize(320, 50);
		bannerView = new BannerView(_activityRule.getActivity(), "garbagePlacement", unityBannerSize);
		final Semaphore _errorSemaphore = new Semaphore(0);
		bannerView.setListener(new BannerView.IListener() {
			public void onBannerLoaded(BannerView bannerAdView) {
				_errorSemaphore.release();
				fail("onBannerLoaded should not be called");
			}

			@Override
			public void onBannerShown(BannerView bannerAdView) {
				_errorSemaphore.release();
				fail("onBannerShown should not be called");
			}

			public void onBannerClick(BannerView bannerAdView) {
				_errorSemaphore.release();
				fail("onBannerClick should not be called");
			}

			public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo bannerErrorInfo) {
				callbackbannerView = bannerAdView;
				_errorSemaphore.release();
			}

			public void onBannerLeftApplication(BannerView bannerView) {
				_errorSemaphore.release();
				fail("onBannerLeftApplication should not be called");
			}
		});
		bannerView.load();
		_errorSemaphore.acquire();
		assertEquals(bannerView, callbackbannerView);
	}

	private class UnityBannerListener implements IUnityBannerListener {

		@Override
		public void onUnityBannerLoaded(String placementId, View view) {

		}

		@Override
		public void onUnityBannerUnloaded(String placementId) {

		}

		@Override
		public void onUnityBannerShow(String placementId) {

		}

		@Override
		public void onUnityBannerClick(String placementId) {

		}

		@Override
		public void onUnityBannerHide(String placementId) {

		}

		@Override
		public void onUnityBannerError(String message) {

		}
	}
}
