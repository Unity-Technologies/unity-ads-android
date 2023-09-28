package com.unity3d.ads.test.instrumentation.services.banners;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.test.instrumentation.InstrumentationTestActivity;
import com.unity3d.services.ads.operation.load.ILoadModule;
import com.unity3d.services.ads.operation.load.ILoadOperation;
import com.unity3d.services.ads.operation.load.LoadBannerModule;
import com.unity3d.services.ads.operation.load.LoadOperationState;
import com.unity3d.services.banners.BannerErrorCode;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.BannerViewCache;

import com.unity3d.services.core.request.metrics.SDKMetricsSender;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.lang.reflect.Field;
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
	public void testTriggerBannerLoadEvent() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
		BannerViewCache bannerViewCache = new BannerViewCache();
		final BannerView bannerView = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		String bannerAdId = bannerViewCache.addBannerView(bannerView);
		final Semaphore _loadedSemaphore = new Semaphore(0);

		ILoadModule loadBannerModule = new LoadBannerModule(Mockito.mock(SDKMetricsSender.class));
		ILoadModule spy = Mockito.spy(loadBannerModule);

		Field instance = LoadBannerModule.class.getDeclaredField("_instance");
		instance.setAccessible(true);
		instance.set(LoadBannerModule.class, spy);

		bannerView.setListener(new BannerView.Listener() {
			@Override
			public void onBannerLoaded(BannerView bannerAdView) {
				assertEquals(bannerView, bannerAdView);
				Mockito.verify(spy).onUnityAdsAdLoaded(bannerAdId);
				_loadedSemaphore.release();
			}
		});

		bannerViewCache.triggerBannerLoadEvent(bannerAdId);
		_loadedSemaphore.acquire();
	}

	@Test
	public void testTriggerBannerShowEvent() throws InterruptedException {
		BannerViewCache bannerViewCache = new BannerViewCache();
		final BannerView bannerView = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		String bannerAdId = bannerViewCache.addBannerView(bannerView);
		final Semaphore _shownSemaphore = new Semaphore(0);
		bannerView.setListener(new BannerView.Listener() {
			@Override
			public void onBannerShown(BannerView bannerAdView) {
				assertEquals(bannerView, bannerAdView);
				_shownSemaphore.release();
			}
		});
		bannerViewCache.triggerBannerShowEvent(bannerAdId);
		_shownSemaphore.acquire();
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
	public void testTriggerBannerErrorEventWithNativeError() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
		BannerViewCache bannerViewCache = new BannerViewCache();
		final BannerView bannerView = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		final BannerErrorInfo bannerErrorInfo = new BannerErrorInfo("test error", BannerErrorCode.NATIVE_ERROR);
		String bannerAdId = bannerViewCache.addBannerView(bannerView);

		LoadOperationState loadOperationState = Mockito.mock(LoadOperationState.class);
		Mockito.when(loadOperationState.isBanner()).thenReturn(true);
		Mockito.when(loadOperationState.duration()).thenReturn(Long.MAX_VALUE);
		loadOperationState.placementId = "test";

		ILoadOperation loadOperation = Mockito.mock(ILoadOperation.class);
		Mockito.when(loadOperation.getLoadOperationState()).thenReturn(loadOperationState);

		ILoadModule loadBannerModule = new LoadBannerModule(Mockito.mock(SDKMetricsSender.class));
		ILoadModule spy = Mockito.spy(loadBannerModule);
		Mockito.when(spy.get(Mockito.anyString())).thenReturn(loadOperation);
		Mockito.doAnswer(invocation -> {
			Mockito.doAnswer(invocation1 -> {
				LoadOperationState state = invocation.getArgument(1);
				state.onUnityAdsFailedToLoad(UnityAds.UnityAdsLoadError.INVALID_ARGUMENT, "test error");
				return null;
			}).when(loadOperation).onUnityAdsFailedToLoad(Mockito.anyString(), Mockito.any(), Mockito.any());
			return null;
		}).when(spy).executeAdOperation(Mockito.any(), Mockito.any());

		Field instance = LoadBannerModule.class.getDeclaredField("_instance");
		instance.setAccessible(true);
		instance.set(LoadBannerModule.class, spy);

		final Semaphore _errorSemaphore = new Semaphore(0);
		bannerView.setListener(new BannerView.Listener() {
			@Override
			public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo _bannerErrorInfo) {
				assertEquals(bannerView, bannerAdView);
				assertEquals(BannerErrorCode.NATIVE_ERROR, _bannerErrorInfo.errorCode);
				Mockito.verify(spy).onUnityAdsFailedToLoad(bannerAdId, UnityAds.UnityAdsLoadError.INVALID_ARGUMENT, "test error");
				_errorSemaphore.release();
			}
		});
		bannerView.load();
		bannerViewCache.triggerBannerErrorEvent(bannerAdId, bannerErrorInfo);
		_errorSemaphore.acquire();
	}

	@Test
	public void testTriggerBannerErrorEventWithWebViewError() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
		BannerViewCache bannerViewCache = new BannerViewCache();
		final BannerView bannerView = new BannerView(_activityRule.getActivity(), "test", new UnityBannerSize(320, 50));
		final BannerErrorInfo bannerErrorInfo = new BannerErrorInfo("test error", BannerErrorCode.WEBVIEW_ERROR);
		String bannerAdId = bannerViewCache.addBannerView(bannerView);

		LoadOperationState loadOperationState = Mockito.mock(LoadOperationState.class);
		Mockito.when(loadOperationState.isBanner()).thenReturn(true);
		Mockito.when(loadOperationState.duration()).thenReturn(Long.MAX_VALUE);
		loadOperationState.placementId = "test";

		ILoadOperation loadOperation = Mockito.mock(ILoadOperation.class);
		Mockito.when(loadOperation.getLoadOperationState()).thenReturn(loadOperationState);

		ILoadModule loadBannerModule = new LoadBannerModule(Mockito.mock(SDKMetricsSender.class));
		ILoadModule spy = Mockito.spy(loadBannerModule);
		Mockito.when(spy.get(Mockito.anyString())).thenReturn(loadOperation);
		Mockito.doAnswer(invocation -> {
			Mockito.doAnswer(invocation1 -> {
				LoadOperationState state = invocation.getArgument(1);
				state.onUnityAdsFailedToLoad(UnityAds.UnityAdsLoadError.INTERNAL_ERROR, "test error");
				return null;
			}).when(loadOperation).onUnityAdsFailedToLoad(Mockito.anyString(), Mockito.any(), Mockito.any());
			return null;
		}).when(spy).executeAdOperation(Mockito.any(), Mockito.any());

		Field instance = LoadBannerModule.class.getDeclaredField("_instance");
		instance.setAccessible(true);
		instance.set(LoadBannerModule.class, spy);

		final Semaphore _errorSemaphore = new Semaphore(0);
		bannerView.setListener(new BannerView.Listener() {
			@Override
			public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo _bannerErrorInfo) {
				assertEquals(bannerView, bannerAdView);
				assertEquals(BannerErrorCode.WEBVIEW_ERROR, _bannerErrorInfo.errorCode);
				Mockito.verify(spy).onUnityAdsFailedToLoad(bannerAdId, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, "test error");
				_errorSemaphore.release();
			}
		});
		bannerView.load();
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
