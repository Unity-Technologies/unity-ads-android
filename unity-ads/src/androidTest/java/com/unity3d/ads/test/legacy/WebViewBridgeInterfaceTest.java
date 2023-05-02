package com.unity3d.ads.test.legacy;

import android.net.Uri;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.webkit.ValueCallback;
import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import androidx.webkit.JavaScriptReplyProxy;
import androidx.webkit.WebMessageCompat;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.IExperiments;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.services.core.webview.WebView;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.CallbackStatus;
import com.unity3d.services.core.webview.bridge.NativeCallback;
import com.unity3d.services.core.webview.bridge.WebViewBridgeInterface;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import kotlinx.coroutines.Dispatchers;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

@RunWith(MockitoJUnitRunner.class)
public class WebViewBridgeInterfaceTest {

	@Mock
	private JavaScriptReplyProxy javaScriptProxy;

	private static boolean nativeCallbackInvoked = false;
	private static CallbackStatus nativeCallbackStatus = null;
	private static String nativeCallbackValue = null;

	private static Class[] apiTestClassList = {
			WebViewBridgeInterfaceTest.WebViewBridgeTestApi.class
	};

	public static class WebViewBridgeTestApi {
		private static boolean invoked = false;
		private static String value = null;
		private static WebViewCallback callback = null;
		private static int callbackCount = 0;

		@WebViewExposed
			public static void apiTestMethod (String value, WebViewCallback callback) {
			invoked = true;
			WebViewBridgeTestApi.value = value;
			WebViewBridgeTestApi.callback = callback;
			callbackCount++;

			callback.invoke(value);
		}

		@WebViewExposed
		public static void apiTestMethodNoParams (WebViewCallback callback) {
			invoked = true;
			value = null;
			WebViewBridgeTestApi.callback = callback;
			callbackCount++;

			callback.invoke();
		}
	}

	@BeforeClass
	public static void prepareTests () {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
	}

	@Before
	public void beforeTest () throws TimeoutException {
		WebViewBridgeTestApi.invoked = false;
		WebViewBridgeTestApi.value = null;
		WebViewBridgeTestApi.callback = null;
		WebViewBridgeTestApi.callbackCount = 0;
		nativeCallbackInvoked = false;
		nativeCallbackStatus = null;
		nativeCallbackValue = null;

		final Configuration config = new Configuration(TestUtilities.getTestServerAddress()) {
			@Override
			public Class[] getWebAppApiClassList() {
				return apiTestClassList;
			}
		};

		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
				WebViewApp.getCurrentApp().setWebView(new WebView(ClientProperties.getApplicationContext(), false) {
					@Override
					public void evaluateJavascript(String data, ValueCallback<String> callback) {
					}
				});

				cv.open();
			}
		}, 100);

		WebViewApp.create(config);
		boolean success = cv.block(30000);
		if (!success)
			throw new TimeoutException("ConditionVariable was not opened in preparation");
	}

	@After
	public void afterTest () {
		WebViewBridgeTestApi.invoked = false;
		WebViewBridgeTestApi.value = null;
		WebViewBridgeTestApi.callback = null;
		WebViewBridgeTestApi.callbackCount = 0;
		nativeCallbackInvoked = false;
		nativeCallbackStatus = null;
		nativeCallbackValue = null;
	}

	@Test (expected = ClassCastException.class)
	public void testHandleInvocationShouldFailParametersNull () throws JSONException {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.handleInvocation("[[\"com.unity3d.ads.test.legacy.WebViewBridgeInterfaceTest$WebViewBridgeTestApi\", \"apiTestMethodNoParams\", null, \"CALLBACK_01\"]]");
	}

	@Test(expected = ClassCastException.class)
	public void testOnHandleInvocationShouldFailParametersNull() {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.onHandleInvocation(
				WebViewApp.getCurrentApp().getWebView(),
				new WebMessageCompat("[[\"com.unity3d.ads.test.legacy.WebViewBridgeInterfaceTest$WebViewBridgeTestApi\", \"apiTestMethodNoParams\", null, \"CALLBACK_01\"]]"),
				Uri.EMPTY,
				true,
				javaScriptProxy
		);
	}

	@Test (expected = NullPointerException.class)
	public void testHandleCallbackShouldFailParametersNull () throws Exception {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.handleCallback("CALLBACK_01", "OK", null);
	}

	@Test(expected = JSONException.class)
	public void testOnHandleCallbackShouldFailParametersNull() {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.onHandleCallback(
				WebViewApp.getCurrentApp().getWebView(),
				new WebMessageCompat("{\"id\":\"CALLBACK_01\",\"status\":\"OK\"}"),
				Uri.EMPTY,
				true,
				javaScriptProxy
		);
	}

	@Test (expected = ClassCastException.class)
	public void testHandleInvocationShouldFailParametersEmpty () throws JSONException {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.handleInvocation("[[\"com.unity3d.ads.test.legacy.WebViewBridgeInterfaceTest$WebViewBridgeTestApi\", \"apiTestMethodNoParams\", \"\", \"CALLBACK_01\"]]");
	}

	@Test(expected = ClassCastException.class)
	public void testOnHandleInvocationShouldFailParametersEmpty() {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.onHandleInvocation(
				WebViewApp.getCurrentApp().getWebView(),
				new WebMessageCompat("[[\"com.unity3d.ads.test.legacy.WebViewBridgeInterfaceTest$WebViewBridgeTestApi\", \"apiTestMethodNoParams\", \"\", \"CALLBACK_01\"]]"),
				Uri.EMPTY,
				true,
				javaScriptProxy
		);
	}

	@Test (expected = JSONException.class)
	public void testHandleCallbackShouldFailParametersEmpty () throws Exception {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.handleCallback("CALLBACK_01", "OK", "");
	}

	@Test(expected = JSONException.class)
	public void testOnHandleCallbackShouldFailParametersEmpty() {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.onHandleCallback(
				WebViewApp.getCurrentApp().getWebView(),
				new WebMessageCompat("{\"id\":\"CALLBACK_01\",\"status\":\"OK\",\"parameters\":\"\"}"),
				Uri.EMPTY,
				true,
				javaScriptProxy
		);
	}

	@Test
	public void testHandleInvocationShouldSucceed () throws JSONException {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.handleInvocation("[[\"com.unity3d.ads.test.legacy.WebViewBridgeInterfaceTest$WebViewBridgeTestApi\", \"apiTestMethodNoParams\", [], \"CALLBACK_01\"]]");
		assertTrue("ApiMethod should have been invoked but wasn't", WebViewBridgeTestApi.invoked);
		assertEquals("CallbackID's didn't match", "CALLBACK_01", WebViewBridgeTestApi.callback.getCallbackId());
	}

	@Test
	public void testOnHandleInvocationShouldSucceed() {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.onHandleInvocation(
				WebViewApp.getCurrentApp().getWebView(),
				new WebMessageCompat("[[\"com.unity3d.ads.test.legacy.WebViewBridgeInterfaceTest$WebViewBridgeTestApi\", \"apiTestMethodNoParams\", [], \"CALLBACK_01\"]]"),
				Uri.EMPTY,
				true,
				javaScriptProxy
		);
		assertTrue("ApiMethod should have been invoked but wasn't", WebViewBridgeTestApi.invoked);
		assertEquals("CallbackID's didn't match", "CALLBACK_01", WebViewBridgeTestApi.callback.getCallbackId());
	}

	@Test
	public void testHandleInvocationWithParamsShouldSucceed () throws JSONException {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.handleInvocation("[[\"com.unity3d.ads.test.legacy.WebViewBridgeInterfaceTest$WebViewBridgeTestApi\", \"apiTestMethod\", [\"test\"], \"CALLBACK_01\"]]");
		assertTrue("ApiMethod should have been invoked but wasn't", WebViewBridgeTestApi.invoked);
		assertEquals("CallbackID's didn't match", "CALLBACK_01", WebViewBridgeTestApi.callback.getCallbackId());
		assertEquals("Callback value wasn't same as was originally given", "test", WebViewBridgeTestApi.value);
	}

	@Test
	public void testOnHandleInvocationWithParamsShouldSucceed() {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		webInterface.onHandleInvocation(
				WebViewApp.getCurrentApp().getWebView(),
				new WebMessageCompat("[[\"com.unity3d.ads.test.legacy.WebViewBridgeInterfaceTest$WebViewBridgeTestApi\", \"apiTestMethod\", [\"test\"], \"CALLBACK_01\"]]"),
				Uri.EMPTY,
				true,
				javaScriptProxy
		);
		assertTrue("ApiMethod should have been invoked but wasn't", WebViewBridgeTestApi.invoked);
		assertEquals("CallbackID's didn't match", "CALLBACK_01", WebViewBridgeTestApi.callback.getCallbackId());
		assertEquals("Callback value wasn't same as was originally given", "test", WebViewBridgeTestApi.value);
	}

	@Test (expected = NullPointerException.class)
	public void testHandleCallbackShouldFailCallbackNotAdded () throws Exception {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		Method m = getClass().getMethod("staticTestHandleCallback", CallbackStatus.class);
		NativeCallback callback = new NativeCallback(m);
		webInterface.handleCallback(callback.getId(), "OK", "[]");
	}

	@Test(expected = NullPointerException.class)
	public void testOnHandleCallbackShouldFailCallbackNotAdded() throws Exception {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		Method m = getClass().getMethod("staticTestHandleCallback", CallbackStatus.class);
		NativeCallback callback = new NativeCallback(m);
		webInterface.onHandleCallback(
				WebViewApp.getCurrentApp().getWebView(),
				new WebMessageCompat("{\"id\":\"" + callback.getId() + "\",\"status\":\"OK\",\"parameters\":\"[]\"}"),
				Uri.EMPTY,
				true,
				javaScriptProxy
		);
	}

	@Test (expected = NullPointerException.class)
	public void testHandleCallbackShouldFailMethodNotStatic () throws Exception {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		Method m = getClass().getMethod("instanceTestHandleCallback", CallbackStatus.class);
		NativeCallback callback = new NativeCallback(m);
		WebViewApp.getCurrentApp().addCallback(callback);
		webInterface.handleCallback(callback.getId(), "OK", "[]");
	}

	@Test(expected = NullPointerException.class)
	public void testOnHandleCallbackShouldFailMethodNotStatic() throws Exception {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		Method m = getClass().getMethod("instanceTestHandleCallback", CallbackStatus.class);
		NativeCallback callback = new NativeCallback(m);
		WebViewApp.getCurrentApp().addCallback(callback);
		webInterface.onHandleCallback(
				WebViewApp.getCurrentApp().getWebView(),
				new WebMessageCompat("{\"id\":\"" + callback.getId() + "\",\"status\":\"OK\",\"parameters\":\"[]\"}"),
				Uri.EMPTY,
				true,
				javaScriptProxy
		);
	}

	@Test
	public void testHandleCallbackShouldSucceed () throws Exception {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		Method m = getClass().getMethod("staticTestHandleCallback", CallbackStatus.class);
		NativeCallback callback = new NativeCallback(m);
		WebViewApp.getCurrentApp().addCallback(callback);
		webInterface.handleCallback(callback.getId(), "OK", "[]");
		assertTrue("NativeCallback -method should have been invoked but wasn't", nativeCallbackInvoked);
		assertEquals("NativeCallback status wasn't OK", CallbackStatus.OK, nativeCallbackStatus);
	}

	@Test
	public void testOnHandleCallbackShouldSucceed() throws Exception {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		Method m = getClass().getMethod("staticTestHandleCallback", CallbackStatus.class);
		NativeCallback callback = new NativeCallback(m);
		WebViewApp.getCurrentApp().addCallback(callback);
		webInterface.onHandleCallback(
				WebViewApp.getCurrentApp().getWebView(),
				new WebMessageCompat("{\"id\":\"" + callback.getId() + "\",\"status\":\"OK\",\"parameters\":\"[]\"}"),
				Uri.EMPTY,
				true,
				javaScriptProxy
		);
		assertTrue("NativeCallback -method should have been invoked but wasn't", nativeCallbackInvoked);
		assertEquals("NativeCallback status wasn't OK", CallbackStatus.OK, nativeCallbackStatus);
	}

	@Test
	public void testHandleCallbackWithParamsShouldSucceed () throws Exception {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		Method m = getClass().getMethod("staticTestHandleCallbackStringParam", CallbackStatus.class, String.class);
		NativeCallback callback = new NativeCallback(m);
		WebViewApp.getCurrentApp().addCallback(callback);
		webInterface.handleCallback(callback.getId(), "OK", "[\"test\"]");

		assertTrue("NativeCallback -method should have been invoked but wasn't", nativeCallbackInvoked);
		assertEquals("NativeCallback status wasn't OK", CallbackStatus.OK, nativeCallbackStatus);
		assertNotNull("Received value should not be null", nativeCallbackValue);
		assertEquals("The value received wasn't the same than was originally given", "test", nativeCallbackValue);
	}

	@Test
	public void testOnHandleCallbackWithParamsShouldSucceed() throws Exception {
		WebViewBridgeInterface webInterface = new WebViewBridgeInterface();
		Method m = getClass().getMethod("staticTestHandleCallbackStringParam", CallbackStatus.class, String.class);
		NativeCallback callback = new NativeCallback(m);
		WebViewApp.getCurrentApp().addCallback(callback);
		webInterface.onHandleCallback(
				WebViewApp.getCurrentApp().getWebView(),
				new WebMessageCompat("{\"id\":\"" + callback.getId() + "\",\"status\":\"OK\",\"parameters\":[\"test\"]}"),
				Uri.EMPTY,
				true,
				javaScriptProxy
		);

		assertTrue("NativeCallback -method should have been invoked but wasn't", nativeCallbackInvoked);
		assertEquals("NativeCallback status wasn't OK", CallbackStatus.OK, nativeCallbackStatus);
		assertNotNull("Received value should not be null", nativeCallbackValue);
		assertEquals("The value received wasn't the same than was originally given", "test", nativeCallbackValue);
	}

	public static void staticTestHandleCallback (CallbackStatus status) {
		nativeCallbackInvoked = true;
		nativeCallbackStatus = status;
	}
	public static void staticTestHandleCallbackStringParam (CallbackStatus status, String value) {
		nativeCallbackInvoked = true;
		nativeCallbackStatus = status;
		nativeCallbackValue = value;
	}
	public void instanceTestHandleCallback (CallbackStatus status) {
		nativeCallbackInvoked = true;
		nativeCallbackStatus = status;
	}
}
