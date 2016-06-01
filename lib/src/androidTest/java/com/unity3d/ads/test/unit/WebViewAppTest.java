package com.unity3d.ads.test.unit;

import android.content.Context;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.configuration.Configuration;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.ads.webview.WebView;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.bridge.CallbackStatus;
import com.unity3d.ads.webview.bridge.Invocation;
import com.unity3d.ads.webview.bridge.NativeCallback;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class WebViewAppTest {
	public static void nativeCallbackMethod () {
	}

	@BeforeClass
	public static void prepareTests () throws Exception {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
	}

	@Test
	public void testCreate () throws Exception {
		WebViewApp.setCurrentApp(null);
		final Configuration conf = new Configuration(TestUtilities.getTestServerAddress());

		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
			}
		}, 100);

		WebViewApp.create(conf);
		assertNotNull("After creating WebApp, the current WebApp should not be null", WebViewApp.getCurrentApp());
	}

	@Test
	public void testAddCallback () throws NoSuchMethodException {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();

		Method m = getClass().getMethod("nativeCallbackMethod");
		NativeCallback localNativeCallback = new NativeCallback(m);

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean success = cv.block(10000);

		WebViewApp.getCurrentApp().addCallback(localNativeCallback);
		NativeCallback remoteNativeCallback = WebViewApp.getCurrentApp().getCallback(localNativeCallback.getId());

		assertTrue("ConditionVariable was not opened successfully", success);
		assertNotNull("The WebApp stored callback should not be NULL", remoteNativeCallback);
		assertEquals("The local and the WebApp stored callback should be the same object", localNativeCallback, remoteNativeCallback);
		assertEquals("The local and the WebApp stored callback should have the same ID", localNativeCallback.getId(), remoteNativeCallback.getId());
	}

	@Test
	public void testRemoveCallback () throws NoSuchMethodException {
		WebViewApp.setCurrentApp(null);
		Method m = getClass().getMethod("nativeCallbackMethod");
		NativeCallback localNativeCallback = new NativeCallback(m);
		final ConditionVariable cv = new ConditionVariable();

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean success = cv.block(10000);

		assertTrue("ConditionVariable was not opened successfully", success);
		WebViewApp.getCurrentApp().addCallback(localNativeCallback);
		NativeCallback remoteNativeCallback = WebViewApp.getCurrentApp().getCallback(localNativeCallback.getId());
		assertNotNull("The WebApp stored callback should not be NULL", remoteNativeCallback);
		WebViewApp.getCurrentApp().removeCallback(localNativeCallback);
		remoteNativeCallback = WebViewApp.getCurrentApp().getCallback(localNativeCallback.getId());
		assertNull("The WebApp stored callback should be NULL because it was removed", remoteNativeCallback);
	}

	@Test
	public void testSetWebView () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebView webView = new WebView(InstrumentationRegistry.getContext());
				WebViewApp.getCurrentApp().setWebView(webView);
				assertEquals("Local and WebApps WebView should be the same object", webView, WebViewApp.getCurrentApp().getWebView());
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean success = cv.block(10000);

		assertTrue("ConditionVariable was not opened successfully", success);
		assertNotNull("Current WebApps WebView should not be null because it was set", WebViewApp.getCurrentApp().getWebView());
	}

	@Test
	public void testSetConfiguration () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean success = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", success);

		final Configuration conf = new Configuration(TestUtilities.getTestServerAddress());
		WebViewApp.getCurrentApp().setConfiguration(conf);

		assertNotNull("Current WebApp configuration should not be null", WebViewApp.getCurrentApp().getConfiguration());
		assertEquals("Local configuration and current WebApp configuration should be the same object", conf, WebViewApp.getCurrentApp().getConfiguration());
	}

	@Test
	public void testSetWebAppLoaded () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean success = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", success);
		assertFalse("WebApp should not be loaded. It was just created", WebViewApp.getCurrentApp().isWebAppLoaded());
		WebViewApp.getCurrentApp().setWebAppLoaded(true);
		assertTrue("WebApp should now be \"loaded\". We set the status to true", WebViewApp.getCurrentApp().isWebAppLoaded());
	}

	@Test
	public void testSendEventShouldFail () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean cvsuccess = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", cvsuccess);
		boolean success = WebViewApp.getCurrentApp().sendEvent(MockEventCategory.TEST_CATEGORY_1, MockEvent.TEST_EVENT_1);
		assertFalse("sendEvent -method should've returned false", success);
		assertFalse("WebView invokeJavascript should've not been invoked but was (webviewapp is not loaded so no call should have occured)", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_INVOKED);
		assertNull("The invoked JavaScript string should be null (webviewapp is not loaded so no call should have occured)", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_CALL);
	}

	@Test
	public void testSendEventShouldSucceed () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean cvsuccess = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", cvsuccess);
		boolean success = WebViewApp.getCurrentApp().sendEvent(MockEventCategory.TEST_CATEGORY_1, MockEvent.TEST_EVENT_1);
		assertTrue("sendEvent should have succeeded", success);
		assertTrue("WebView invokeJavascript should've been invoked but was not", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_INVOKED);
		assertNotNull("The invoked JavaScript string should not be null", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_CALL);
	}

	@Test
	public void testSendEventWithParamsShouldSucceed () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean cvsuccess = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", cvsuccess);
		boolean success = WebViewApp.getCurrentApp().sendEvent(MockEventCategory.TEST_CATEGORY_1, MockEvent.TEST_EVENT_1, "Test", 12345, true);
		assertTrue("sendEvent should have succeeded", success);
		assertTrue("WebView invokeJavascript should've been invoked but was not", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_INVOKED);
		assertNotNull("The invoked JavaScript string should not be null", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_CALL);
	}

	@Test
	public void testInvokeMethodShouldFailWebAppNotLoaded () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppLoaded(false);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean cvsuccess = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", cvsuccess);
		Method m = getClass().getMethod("testNativeCallbackMethod");
		boolean success = WebViewApp.getCurrentApp().invokeMethod("TestClass", "testMethod", m);
		assertFalse("invokeMethod -method should've returned false", success);
		assertFalse("WebView invokeJavascript should've not been invoked but was (webviewapp is not loaded so no call should have occured)", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_INVOKED);
		assertNull("The invoked JavaScript string should be null (webviewapp is not loaded so no call should have occured)", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_CALL);
	}

	@Test
	public void testInvokeMethodShouldSucceedMethodNull () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean cvsuccess = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", cvsuccess);
		Method m = null;
		boolean success = WebViewApp.getCurrentApp().invokeMethod("TestClass", "testMethod", m);
		assertTrue("invokeMethod -method should've returned true", success);
		assertTrue("WebView invokeJavascript should've succeeded but didn't", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_INVOKED);
		assertNotNull("The invoked JavaScript string should not be null.", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_CALL);
	}

	@Test
	public void testInvokeMethodShouldSucceed () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean cvsuccess = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", cvsuccess);
		Method m = getClass().getMethod("testNativeCallbackMethod");
		boolean success = WebViewApp.getCurrentApp().invokeMethod("TestClass", "testMethod", m);
		assertTrue("invokeMethod -method should've returned true", success);
		assertTrue("WebView invokeJavascript should've succeeded but didn't", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_INVOKED);
		assertNotNull("The invoked JavaScript string should not be null.", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_CALL);
	}

	@Test
	public void testInvokeMethodWithParamsShouldSucceed () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean cvsuccess = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", cvsuccess);
		Method m = getClass().getMethod("testNativeCallbackMethod");
		boolean success = WebViewApp.getCurrentApp().invokeMethod("TestClass", "testMethod", m, "Test", 12345, true);
		assertTrue("invokeMethod -method should've returned true", success);
		assertTrue("WebView invokeJavascript should've succeeded but didn't", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_INVOKED);
		assertNotNull("The invoked JavaScript string should not be null.", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_CALL);
	}

	@Test
	public void testInvokeCallbackShouldFailWebAppNotLoaded () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean cvsuccess = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", cvsuccess);
		Invocation invocation = new Invocation();
		invocation.setInvocationResponse(CallbackStatus.OK, null, "Test", 12345, true);
		boolean success = WebViewApp.getCurrentApp().invokeCallback(invocation);
		assertFalse("invokeCallback -method should've returned false (webapp not loaded)", success);
		assertFalse("WebView invokeJavascript should've not been invoked but was (webviewapp is not loaded so no call should have occured)", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_INVOKED);
		assertNull("The invoked JavaScript string should be null (webviewapp is not loaded so no call should have occured)", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_CALL);
	}

	@Test
	public void testInvokeCallbackShouldSucceed () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean cvsuccess = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", cvsuccess);
		Invocation invocation = new Invocation();
		invocation.setInvocationResponse(CallbackStatus.OK, null, "Test", 12345, true);
		boolean success = WebViewApp.getCurrentApp().invokeCallback(invocation);
		assertTrue("invokeCallback -method should've returned true", success);
		assertTrue("WebView invokeJavascript should've been invoked but was not", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_INVOKED);
		assertNotNull("The invoked JavaScript string should not be null", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_CALL);
	}

	@Test
	public void testInvokeCallbackWithErrorShouldSucceed () throws Exception {
		WebViewApp.setCurrentApp(null);
		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp());
				WebViewApp.getCurrentApp().setWebView(new MockWebView(InstrumentationRegistry.getContext()));
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				cv.open();
			}
		});

		boolean cvsuccess = cv.block(10000);
		assertTrue("ConditionVariable was not opened successfully", cvsuccess);
		Invocation invocation = new Invocation();
		invocation.setInvocationResponse(CallbackStatus.OK, MockError.TEST_ERROR_1, "Test", 12345, true);
		boolean success = WebViewApp.getCurrentApp().invokeCallback(invocation);
		assertTrue("invokeCallback -method should've returned true", success);
		assertTrue("WebView invokeJavascript should've been invoked but was not", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_INVOKED);
		assertNotNull("The invoked JavaScript string should not be null", ((MockWebView) WebViewApp.getCurrentApp().getWebView()).JS_CALL);
	}

	public static void testNativeCallbackMethod () {
	}

	private enum MockEventCategory {
		TEST_CATEGORY_1,
		TEST_CATEGORY_2
	}

	private enum MockEvent {
		TEST_EVENT_1,
		TEST_EVENT_2
	}

	private enum MockError {
		TEST_ERROR_1,
		TEST_ERROR_2
	}

	private class MockWebView extends WebView {
		public boolean JS_INVOKED = false;
		public String JS_CALL = null;

		public MockWebView(Context context) {
			super(context);
		}

		@Override
		public void loadUrl(String url) {
		}

		@Override
		public void invokeJavascript(String data) {
			JS_INVOKED = true;
			JS_CALL = data;
		}
	}
}
