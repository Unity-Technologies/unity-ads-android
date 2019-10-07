package com.unity3d.ads.test.instrumentation.services.banners;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.test.instrumentation.InstrumentationTestActivity;
import com.unity3d.services.banners.BannerErrorCode;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.BannerViewCache;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Semaphore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class BannerViewCacheTests {

	@Rule
	public final ActivityTestRule<InstrumentationTestActivity> _activityRule = new ActivityTestRule<>(InstrumentationTestActivity.class);

	@Test
	public void testAddBannerView() {
		BannerViewCache bannerViewCache = new BannerViewCache();
		BannerView bannerView = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		String bannerAdId = bannerViewCache.addBannerView(bannerView);
		BannerView storedBannerView = bannerViewCache.getBannerView(bannerAdId);
		assertEquals(bannerView, storedBannerView);
	}

	@Test
	public void testMultipleAddBannerView() {
		BannerViewCache bannerViewCache = new BannerViewCache();
		BannerView bannerView = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		BannerView bannerView2 = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		String bannerAdId = bannerViewCache.addBannerView(bannerView);
		String bannerAdId2 = bannerViewCache.addBannerView(bannerView2);
		BannerView storedBannerView = bannerViewCache.getBannerView(bannerAdId);
		assertEquals(bannerView, storedBannerView);
		BannerView storedBannerView2 = bannerViewCache.getBannerView(bannerAdId2);
		assertEquals(bannerView2, storedBannerView2);
	}

	@Test
	public void testRemoveBannerView() {
		BannerViewCache bannerViewCache = new BannerViewCache();
		BannerView bannerView = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		BannerView bannerView2 = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		String bannerAdId = bannerViewCache.addBannerView(bannerView);
		bannerViewCache.addBannerView(bannerView2);
		bannerViewCache.removeBannerView(bannerAdId);
		BannerView storedBannerView = bannerViewCache.getBannerView(bannerAdId);
		assertNull(storedBannerView);
	}

	@Test
	public void testTriggerBannerLoadEvent() throws InterruptedException {
		BannerViewCache bannerViewCache = new BannerViewCache();
		final BannerView bannerView = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		String bannerAdId = bannerViewCache.addBannerView(bannerView);
		final Semaphore _loadedSemaphore = new Semaphore(0);
		bannerView.setListener(new BannerView.Listener() {
			@Override
			public void onBannerLoaded(BannerView bannerAdView) {
				assertEquals(bannerView, bannerAdView);
				_loadedSemaphore.release();
			}
		});
		bannerViewCache.triggerBannerLoadEvent(bannerAdId);
		_loadedSemaphore.acquire();
	}

	@Test
	public void testTriggerBannerClickEvent() throws InterruptedException {
		BannerViewCache bannerViewCache = new BannerViewCache();
		final BannerView bannerView = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		String bannerAdId = bannerViewCache.addBannerView(bannerView);
		final Semaphore _clickSemaphore = new Semaphore(0);
		bannerView.setListener(new BannerView.Listener() {
			@Override
			public void onBannerClick(BannerView bannerAdView) {
				assertEquals(bannerView, bannerAdView);
				_clickSemaphore.release();
			}
		});
		bannerViewCache.triggerBannerClickEvent(bannerAdId);
		_clickSemaphore.acquire();
	}

	@Test
	public void testTriggerBannerErrorEvent() throws InterruptedException {
		BannerViewCache bannerViewCache = new BannerViewCache();
		final BannerView bannerView = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		final BannerErrorInfo bannerErrorInfo = new BannerErrorInfo("test error", BannerErrorCode.NATIVE_ERROR);
		String bannerAdId = bannerViewCache.addBannerView(bannerView);
		final Semaphore _errorSemaphore = new Semaphore(0);
		bannerView.setListener(new BannerView.Listener() {
			@Override
			public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo _bannerErrorInfo) {
				assertEquals(bannerView, bannerAdView);
				assertEquals(bannerErrorInfo, _bannerErrorInfo);
				_errorSemaphore.release();
			}
		});
		bannerViewCache.triggerBannerErrorEvent(bannerAdId, bannerErrorInfo);
		_errorSemaphore.acquire();
	}

	@Test
	public void testTriggerBannerLeftApplicationEvent() throws InterruptedException {
		BannerViewCache bannerViewCache = new BannerViewCache();
		final BannerView bannerView = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		String bannerAdId = bannerViewCache.addBannerView(bannerView);
		final Semaphore _leftApplicationSemaphore = new Semaphore(0);
		bannerView.setListener(new BannerView.Listener() {
			@Override
			public void onBannerLeftApplication(BannerView bannerAdView) {
				assertEquals(bannerView, bannerAdView);
				_leftApplicationSemaphore.release();
			}
		});
		bannerViewCache.triggerBannerLeftApplicationEvent(bannerAdId);
		_leftApplicationSemaphore.acquire();
	}

}
