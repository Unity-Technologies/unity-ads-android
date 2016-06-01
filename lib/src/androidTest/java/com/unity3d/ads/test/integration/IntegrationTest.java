package com.unity3d.ads.test.integration;

import android.os.Handler;
import android.os.Looper;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.configuration.Configuration;
import com.unity3d.ads.configuration.InitializeThread;
import com.unity3d.ads.metadata.MetaData;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class IntegrationTest {

	private static final String _gameId = "14851";
	private static final boolean _testMode = true;
	private static final boolean _debugMode = true;

	private static final String _testPlacementId = "defaultVideoAndPictureZone";

	private static final Semaphore _readySemaphore = new Semaphore(0);
	private static final Semaphore _startSemaphore = new Semaphore(0);
	private static final Semaphore _completedSemaphore = new Semaphore(0);
	private static final Semaphore _finishSemaphore = new Semaphore(0);

	private static final Handler _handler = new Handler(Looper.getMainLooper());

	@Rule
	public final ActivityTestRule<IntegrationTestActivity> _activityRule = new ActivityTestRule<>(IntegrationTestActivity.class);

	@Test
	public void integrationTest() throws InterruptedException {
		assertTrue("This device is not supported", UnityAds.isSupported());
		assertFalse("UnityAds has already been initialized", UnityAds.isInitialized());

		assertFalse("Default placement is ready before initialization", UnityAds.isReady());
		assertFalse("Test placement is ready before initialization", UnityAds.isReady(_testPlacementId));

		assertEquals("Default placement state should be not available before initialization", UnityAds.PlacementState.NOT_AVAILABLE, UnityAds.getPlacementState());
		assertEquals("Test placement state should be not available before initialization", UnityAds.PlacementState.NOT_AVAILABLE, UnityAds.getPlacementState(_testPlacementId));

		final IUnityAdsListener listener = new IUnityAdsListener() {
			@Override
			public void onUnityAdsReady(String placementId) {
				if (placementId.equals(_testPlacementId)) {
					_readySemaphore.release();
				}
			}

			@Override
			public void onUnityAdsStart(String placementId) {
				if (placementId.equals(_testPlacementId)) {
					_startSemaphore.release();
				}
			}

			@Override
			public void onUnityAdsFinish(String placementId, final UnityAds.FinishState result) {
				if (placementId.equals(_testPlacementId)) {
					_handler.post(new Runnable() {
						@Override
						public void run() {
							assertEquals("Test placement finish state result should be completed", UnityAds.FinishState.COMPLETED, result);
						}
					});
					_finishSemaphore.release();
				}
			}

			@Override
			public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {

			}
		};

		MetaData integrationTestMetaData = new MetaData(_activityRule.getActivity().getApplicationContext());
		integrationTestMetaData.set("integration_test", true);
		integrationTestMetaData.commit();

		UnityAds.setDebugMode(_debugMode);

		ClientProperties.setGameId(_gameId);
		ClientProperties.setListener(listener);
		ClientProperties.setApplicationContext(_activityRule.getActivity().getApplicationContext());
		SdkProperties.setTestMode(_testMode);

		Configuration configuration = new Configuration();
		final Class[] apiClassList = {
			com.unity3d.ads.api.AdUnit.class,
			com.unity3d.ads.api.Cache.class,
			com.unity3d.ads.api.Connectivity.class,
			com.unity3d.ads.api.DeviceInfo.class,
			com.unity3d.ads.api.Listener.class,
			com.unity3d.ads.api.Storage.class,
			com.unity3d.ads.api.Sdk.class,
			com.unity3d.ads.api.Request.class,
			com.unity3d.ads.api.Resolve.class,
			com.unity3d.ads.api.VideoPlayer.class,
			com.unity3d.ads.api.Placement.class,
			com.unity3d.ads.api.Intent.class,
			com.unity3d.ads.test.integration.IntegrationTest.class,
		};

		configuration.setWebAppApiClassList(apiClassList);
		InitializeThread.initialize(configuration);

		if (!_readySemaphore.tryAcquire(1, TimeUnit.MINUTES)) {
			fail("onUnityAdsReady did not arrive");
		}

		assertTrue("UnityAds is not initialized after onUnityAdsReady callback", UnityAds.isInitialized());

		assertTrue("Default placement is not ready after onUnityAdsReady callback", UnityAds.isReady());
		assertTrue("Test placement is not ready after onUnityAdsReady callback", UnityAds.isReady(_testPlacementId));

		assertEquals("Default placement state should be ready after onUnityAdsReady callback", UnityAds.PlacementState.READY, UnityAds.getPlacementState());
		assertEquals("Test placement state should be ready after onUnityAdsReady callback", UnityAds.PlacementState.READY, UnityAds.getPlacementState(_testPlacementId));

		UnityAds.show(_activityRule.getActivity(), _testPlacementId);
		if (!_startSemaphore.tryAcquire(1, TimeUnit.MINUTES)) {
			fail("onUnityAdsStart did not arrive");
		}

		if(!_completedSemaphore.tryAcquire(1, TimeUnit.MINUTES)) {
			fail("onVideoCompleted did not arrive");
		}

		WebViewApp.getCurrentApp().getWebView().invokeJavascript("document.querySelector('.btn-close').click()");

		if(!_finishSemaphore.tryAcquire(1, TimeUnit.MINUTES)) {
			fail("onUnityAdsFinish did not arrive");
		}
	}

	@WebViewExposed
	public static void onVideoCompleted(String placementId, WebViewCallback callback) {
		if(placementId.equals(_testPlacementId)) {
			_completedSemaphore.release();
		}
		callback.invoke();
	}
}
