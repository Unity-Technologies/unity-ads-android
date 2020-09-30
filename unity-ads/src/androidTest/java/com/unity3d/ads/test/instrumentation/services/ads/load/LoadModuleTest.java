package com.unity3d.ads.test.instrumentation.services.ads.load;

import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.services.ads.load.LoadModule;
import com.unity3d.services.core.configuration.IInitializationListener;
import com.unity3d.services.core.configuration.IInitializationNotificationCenter;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.CallbackStatus;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(AndroidJUnit4.class)
public class LoadModuleTest {

	private InitializationNotificationCenter initializationNotificationCenter;
	private LoadModule loadModule;
	private IInitializationNotificationCenter notificationCenter;

	private IInitializationListener listener;

	@Before
	public void before() {
		notificationCenter = mock(IInitializationNotificationCenter.class);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				listener = invocation.getArgument(0);
				return null;
			}})
			.when(notificationCenter).addListener(any(IInitializationListener.class));

		loadModule = new LoadModule(notificationCenter);
	}

	@Test
	public void testSubscription() {
		verify(notificationCenter, times(1)).addListener(loadModule);
		verify(notificationCenter, times(1)).addListener(any(IInitializationListener.class));
	}

	@Test
	public void testLoadAfterInitialized() throws InterruptedException, InvocationTargetException, IllegalAccessException, JSONException {
		MockedWebViewApp mockedWebViewApp = new MockedWebViewApp(loadModule);
		WebViewApp.setCurrentApp(mockedWebViewApp.webViewApp);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);

		MockedLoadListener mockedLoadListener = new MockedLoadListener();

		loadModule.load("test", mockedLoadListener.listener);

		mockedWebViewApp.await();
		mockedWebViewApp.simulateLoadCall("test");

		mockedLoadListener.await();

		verify(mockedLoadListener.listener, Mockito.times(1)).onUnityAdsAdLoaded("test");
		verifyNoMoreInteractions(mockedLoadListener.listener);
	}

	@Test
	public void testLoadAfterInitialized_WithWebViewTimeout() throws InterruptedException, InvocationTargetException, IllegalAccessException, JSONException {
		MockedWebViewApp mockedWebViewApp = new MockedWebViewApp(loadModule);
		WebViewApp.setCurrentApp(mockedWebViewApp.webViewApp);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);

		MockedLoadListener mockedLoadListener = new MockedLoadListener();

		loadModule.load("test", mockedLoadListener.listener);

		mockedWebViewApp.await();
		mockedLoadListener.await();

		verify(mockedLoadListener.listener, Mockito.times(1)).onUnityAdsFailedToLoad("test");
		verifyNoMoreInteractions(mockedLoadListener.listener);
	}

	@Test
	public void testLoadAfterInitialized_ListenerCleanup() throws InterruptedException, InvocationTargetException, IllegalAccessException, JSONException {
		MockedWebViewApp mockedWebViewApp = new MockedWebViewApp(loadModule);
		WebViewApp.setCurrentApp(mockedWebViewApp.webViewApp);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);

		MockedLoadListener mockedLoadListener = new MockedLoadListener();

		loadModule.load("test", mockedLoadListener.listener);

		mockedWebViewApp.await();
		mockedWebViewApp.simulateLoadCall("test");
		mockedWebViewApp.simulateLoadCall("test");

		mockedLoadListener.await();

		verify(mockedLoadListener.listener, Mockito.times(1)).onUnityAdsAdLoaded("test");
		verifyNoMoreInteractions(mockedLoadListener.listener);
	}

	@Test
	public void testLoadAfterInitialized_WithFailedWebViewCall() throws InterruptedException, InvocationTargetException, IllegalAccessException, JSONException {
		MockedWebViewApp mockedWebViewApp = new MockedWebViewApp(loadModule);
		WebViewApp.setCurrentApp(mockedWebViewApp.webViewApp);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);

		MockedLoadListener mockedLoadListener = new MockedLoadListener();

		loadModule.load("test", mockedLoadListener.listener);

		mockedWebViewApp.await();
		mockedWebViewApp.failLoadCall("test");

		mockedLoadListener.await();

		verify(mockedLoadListener.listener, Mockito.times(1)).onUnityAdsFailedToLoad("test");
		verifyNoMoreInteractions(mockedLoadListener.listener);
	}

	@Test
	public void testLoadAfterInitialized_WithHardcodedTimeout() throws InterruptedException, InvocationTargetException, IllegalAccessException, JSONException {
		MockedWebViewApp mockedWebViewApp = new MockedWebViewApp(loadModule);
		WebViewApp.setCurrentApp(mockedWebViewApp.webViewApp);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);

		MockedLoadListener mockedLoadListener = new MockedLoadListener();

		loadModule.load("test", mockedLoadListener.listener);

		mockedWebViewApp.await();
		mockedWebViewApp.simulateLoadCallTimeout("test");

		mockedLoadListener.await();

		verify(mockedLoadListener.listener, Mockito.times(1)).onUnityAdsFailedToLoad("test");
		verifyNoMoreInteractions(mockedLoadListener.listener);
	}

	@Test
	public void testTwiceLoadAfterInitialized() throws InterruptedException, JSONException, InvocationTargetException, IllegalAccessException {
		MockedWebViewApp mockedWebViewApp = new MockedWebViewApp(loadModule);
		WebViewApp.setCurrentApp(mockedWebViewApp.webViewApp);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);

		MockedLoadListener mockedLoadListener1 = new MockedLoadListener();
		MockedLoadListener mockedLoadListener2 = new MockedLoadListener();

		loadModule.load("test", mockedLoadListener1.listener);
		loadModule.load("test_2", mockedLoadListener2.listener);

		mockedWebViewApp.await();
		mockedWebViewApp.simulateLoadCall("test");

		mockedWebViewApp.await();
		mockedWebViewApp.simulateLoadCall("test_2");

		mockedLoadListener1.await();
		mockedLoadListener2.await();

		verify(mockedLoadListener1.listener, Mockito.times(1)).onUnityAdsAdLoaded("test");
		verifyNoMoreInteractions(mockedLoadListener1.listener);

		verify(mockedLoadListener2.listener, Mockito.times(1)).onUnityAdsAdLoaded("test_2");
		verifyNoMoreInteractions(mockedLoadListener2.listener);
	}

	@Test
	public void testLoadWithNullPlacement() throws InterruptedException {
		MockedWebViewApp mockedWebViewApp = new MockedWebViewApp(loadModule);
		WebViewApp.setCurrentApp(mockedWebViewApp.webViewApp);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);

		MockedLoadListener mockedLoadListener = new MockedLoadListener();

		loadModule.load(null, mockedLoadListener.listener);

		mockedLoadListener.await();

		verify(mockedLoadListener.listener, Mockito.times(1)).onUnityAdsFailedToLoad(null);
		verifyNoMoreInteractions(mockedLoadListener.listener);
	}

	@Test
	public void testLoadWithEmptyPlacement() throws InterruptedException {
		MockedWebViewApp mockedWebViewApp = new MockedWebViewApp(loadModule);
		WebViewApp.setCurrentApp(mockedWebViewApp.webViewApp);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);

		MockedLoadListener mockedLoadListener = new MockedLoadListener();

		loadModule.load("", mockedLoadListener.listener);

		mockedLoadListener.await();

		verify(mockedLoadListener.listener, Mockito.times(1)).onUnityAdsFailedToLoad("");
		verifyNoMoreInteractions(mockedLoadListener.listener);
	}

	@Test
	public void testLoadBeforeInitializedAndBatchInvocation() throws InterruptedException, IllegalAccessException, JSONException, InvocationTargetException {
		MockedWebViewApp mockedWebViewApp = new MockedWebViewApp(loadModule);
		WebViewApp.setCurrentApp(mockedWebViewApp.webViewApp);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);

		MockedLoadListener mockedLoadListener1 = new MockedLoadListener();
		MockedLoadListener mockedLoadListener2 = new MockedLoadListener();

		loadModule.load("test", mockedLoadListener1.listener);
		loadModule.load("test_2", mockedLoadListener2.listener);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);
		loadModule.onSdkInitialized();

		mockedWebViewApp.await();
		mockedWebViewApp.simulateLoadCall("test");

		mockedWebViewApp.await();
		mockedWebViewApp.simulateLoadCall("test_2");

		mockedLoadListener1.await();
		mockedLoadListener2.await();

		verify(mockedLoadListener1.listener, Mockito.times(1)).onUnityAdsAdLoaded("test");
		verifyNoMoreInteractions(mockedLoadListener1.listener);

		verify(mockedLoadListener2.listener, Mockito.times(1)).onUnityAdsAdLoaded("test_2");
		verifyNoMoreInteractions(mockedLoadListener2.listener);
	}

	@Test
	public void testLoadBeforeInitializedAndBatchInvocation_InitFailed() throws InterruptedException, IllegalAccessException, JSONException, InvocationTargetException {
		MockedWebViewApp mockedWebViewApp = new MockedWebViewApp(loadModule);
		WebViewApp.setCurrentApp(mockedWebViewApp.webViewApp);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);

		MockedLoadListener mockedLoadListener1 = new MockedLoadListener();
		MockedLoadListener mockedLoadListener2 = new MockedLoadListener();

		loadModule.load("test", mockedLoadListener1.listener);
		loadModule.load("test_2", mockedLoadListener2.listener);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_FAILED);
		loadModule.onSdkInitializationFailed("", 0);

		mockedLoadListener1.await();
		mockedLoadListener2.await();

		verify(mockedLoadListener1.listener, Mockito.times(1)).onUnityAdsFailedToLoad("test");
		verifyNoMoreInteractions(mockedLoadListener1.listener);

		verify(mockedLoadListener2.listener, Mockito.times(1)).onUnityAdsFailedToLoad("test_2");
		verifyNoMoreInteractions(mockedLoadListener2.listener);
	}

	@Test
	public void testLoadBeforeInitializedSamePlacement() throws InterruptedException, IllegalAccessException, JSONException, InvocationTargetException {
		MockedWebViewApp mockedWebViewApp = new MockedWebViewApp(loadModule);
		WebViewApp.setCurrentApp(mockedWebViewApp.webViewApp);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);

		MockedLoadListener mockedLoadListener1 = new MockedLoadListener();
		MockedLoadListener mockedLoadListener2 = new MockedLoadListener();

		loadModule.load("test", mockedLoadListener1.listener);
		loadModule.load("test", mockedLoadListener2.listener);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);
		loadModule.onSdkInitialized();

		mockedWebViewApp.await();
		mockedWebViewApp.simulateLoadCall("test");

		mockedWebViewApp.await();
		mockedWebViewApp.simulateLoadCall("test");

		mockedLoadListener1.await();
		mockedLoadListener2.await();

		verify(mockedLoadListener1.listener, Mockito.times(1)).onUnityAdsAdLoaded("test");
		verifyNoMoreInteractions(mockedLoadListener1.listener);

		verify(mockedLoadListener2.listener, Mockito.times(1)).onUnityAdsAdLoaded("test");
		verifyNoMoreInteractions(mockedLoadListener2.listener);
	}

	class MockedLoadListener {
		public CountDownLatch latch;
		public IUnityAdsLoadListener listener;

		public MockedLoadListener() {
			latch = new CountDownLatch(1);
			listener = mock(IUnityAdsLoadListener.class);

			doAnswer(new Answer() {
           		public Object answer(InvocationOnMock invocation) {
				latch.countDown();
				return null;
			}}).when(listener).onUnityAdsAdLoaded(anyString());
			doAnswer(new Answer() {
				public Object answer(InvocationOnMock invocation) {
					latch.countDown();
					return null;
				}}).when(listener).onUnityAdsFailedToLoad(anyString());
		}

		public void await() throws InterruptedException {
			latch.await(35, TimeUnit.SECONDS);
		}
	}

	class MockedWebViewApp {
		private final LoadModule loadModule;
		public CountDownLatch latch;
		public WebViewApp webViewApp;

		public Method loadCallback;
		public JSONObject options;

		public MockedWebViewApp(LoadModule loadModule) {
			webViewApp = mock(WebViewApp.class);
			latch = new CountDownLatch(1);
			this.loadModule = loadModule;

			doAnswer(new Answer() {
				public Object answer(InvocationOnMock invocation) {
					loadCallback = invocation.getArgument(2);
					options = invocation.getArgument(3);
					latch.countDown();
					return null;
				}}).when(webViewApp).invokeMethod(eq("webview"), eq("load"), any(Method.class), any());
		}

		public void await() throws InterruptedException {
			latch.await(35, TimeUnit.SECONDS);
			latch = new CountDownLatch(1);
		}

		public void failLoadCall(String placementId) throws InvocationTargetException, IllegalAccessException, JSONException {
			loadCallback.invoke(null, CallbackStatus.ERROR);
		}

		public void simulateLoadCall(String placementId) throws InvocationTargetException, IllegalAccessException, JSONException {
			loadCallback.invoke(null, CallbackStatus.OK);
			loadModule.sendAdLoaded(placementId, options.getString("listenerId"));
		}

		public void simulateLoadCallTimeout(String placementId) throws InvocationTargetException, IllegalAccessException, JSONException {
			loadCallback.invoke(null, CallbackStatus.OK);
		}
	}

}
