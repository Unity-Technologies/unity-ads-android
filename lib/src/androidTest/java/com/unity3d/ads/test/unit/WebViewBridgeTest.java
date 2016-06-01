package com.unity3d.ads.test.unit;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.configuration.Configuration;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.bridge.CallbackStatus;
import com.unity3d.ads.webview.bridge.NativeCallback;
import com.unity3d.ads.webview.bridge.WebViewBridge;
import com.unity3d.ads.webview.bridge.WebViewBridgeError;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RunWith(AndroidJUnit4.class)
public class WebViewBridgeTest {

	public Enum webViewCallbackError = null;
	public String webViewCallbackClassname = null;
	public String webViewCallbackMethodname = null;
	public Object[] webViewCallbackParameters = null;

	private static String nativeCallbackValue = null;
	private static boolean nativeCallbackFinished = false;
	private static CallbackStatus nativeCallbackStatus = null;


	private static Class[] apiTestClassList = {
			WebViewBridgeTest.WebViewBridgeTestApi.class
	};

	public static class WebViewBridgeTestApi {
		private static boolean invoked = false;
		private static String value = null;
		private static WebViewCallback callback = null;

		@WebViewExposed
		public static void apiTestMethod (String value, WebViewCallback callback) {
			invoked = true;
			WebViewBridgeTestApi.value = value;
			WebViewBridgeTestApi.callback = callback;
		}

		@WebViewExposed
		public static void apiTestMethodNoParams (WebViewCallback callback) {
			invoked = true;
			value = null;
			WebViewBridgeTestApi.callback = callback;
		}
	}

	@BeforeClass
	public static void prepareTests () {
	}

	@Before
	public void beforeTest () {
		WebViewBridgeTestApi.invoked = false;
		WebViewBridgeTestApi.value = null;
		WebViewBridgeTestApi.callback = null;
		nativeCallbackValue = null;
		nativeCallbackFinished = false;
		nativeCallbackStatus = null;
		webViewCallbackError = null;
		webViewCallbackClassname = null;
		webViewCallbackMethodname = null;
		webViewCallbackParameters = null;

		final Configuration config = new Configuration(TestUtilities.getTestServerAddress()) {
			@Override
			public Class[] getWebAppApiClassList() {
				return apiTestClassList;
			}
		};

		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
			}
		}, 100);

		WebViewApp.create(config);
	}

	@After
	public void afterTest () {
		WebViewBridgeTestApi.invoked = false;
		WebViewBridgeTestApi.value = null;
		WebViewBridgeTestApi.callback = null;
		nativeCallbackValue = null;
		nativeCallbackFinished = false;
		nativeCallbackStatus = null;
		webViewCallbackError = null;
		webViewCallbackClassname = null;
		webViewCallbackMethodname = null;
		webViewCallbackParameters = null;
	}

	// THESE SHOULD THROW AN EXCEPTION

	@Test (expected = NullPointerException.class)
	public void testAllNull () throws Exception {
		WebViewBridge.handleInvocation(null, null, null, null);
	}

	@Test (expected = NullPointerException.class)
	public void testClassNameSetOthersNull () throws Exception {
		WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", null, null, null);
	}

	@Test (expected = NullPointerException.class)
	public void testClassNameMethodSetOthersNull () throws Exception {
		WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", "apiTestMethod", null, null);
	}

	@Test (expected = NullPointerException.class)
	public void testOthersSetParametersNull () throws Exception {
		WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", "apiTestMethod", null, new MockWebViewCallback("APICALLBACK_01", 1));
	}

	@Test
	public void testOthersSetMethodNull () throws Exception {
		boolean gotException = false;

		try {
			WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", null, new Object[]{"test"}, new MockWebViewCallback("APICALLBACK_01", 1));
		}
		catch (NoSuchMethodException e) {
			assertEquals("Should have received exception of type NoSuchMethodException", NoSuchMethodException.class, e.getClass());
			gotException = true;
		}

		assertTrue("Should have received exception", gotException);
		assertEquals("WebViewCallback should have received error WebViewBridgeError.METHOD_NOT_FOUND", WebViewBridgeError.METHOD_NOT_FOUND, webViewCallbackError);
		assertEquals("Class name was not the same than originally was called", "com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", webViewCallbackClassname);
		assertNull("Method name was supposed to be null", webViewCallbackMethodname);
	}

	@Test
	public void testOthersSetClassNameNull () throws Exception {
		boolean gotException = false;

		try {
			WebViewBridge.handleInvocation(null, "apiTestMethod", new Object[]{"test"}, new MockWebViewCallback("APICALLBACK_01", 1));
		}
		catch (NoSuchMethodException e) {
			assertEquals("Should have received exception of type NoSuchMethodException", NoSuchMethodException.class, e.getClass());
			gotException = true;
		}

		assertTrue("Should have received exception", gotException);
		assertEquals("WebViewCallback should have received error WebViewBridgeError.METHOD_NOT_FOUND", WebViewBridgeError.METHOD_NOT_FOUND, webViewCallbackError);
		assertNull("Class name was supposed to be null", webViewCallbackClassname);
		assertEquals("Method name was not the same than originally was called", "apiTestMethod", webViewCallbackMethodname);
	}

	@Test
	public void testAllSetClassNameWrong () throws Exception {
		boolean gotException = false;

		try {
			WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApina", "apiTestMethod", new Object[]{"test"}, new MockWebViewCallback("APICALLBACK_01", 1));
		}
		catch (NoSuchMethodException e) {
			assertEquals("Should have received exception of type NoSuchMethodException", NoSuchMethodException.class, e.getClass());
			gotException = true;
		}

		assertTrue("Should have received exception", gotException);
		assertEquals("WebViewCallback should have received error WebViewBridgeError.METHOD_NOT_FOUND", WebViewBridgeError.METHOD_NOT_FOUND, webViewCallbackError);
		assertEquals("Class name was not the same than originally was called", "com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApina", webViewCallbackClassname);
		assertEquals("Method name was not the same than originally was called", "apiTestMethod", webViewCallbackMethodname);
	}

	@Test
	public void testAllSetMethodWrong () throws Exception {
		boolean gotException = false;

		try {
			WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", "apinaTestMethod", new Object[]{"test"}, new MockWebViewCallback("APICALLBACK_01", 1));
		}
		catch (NoSuchMethodException e) {
			assertEquals("Should have received exception of type NoSuchMethodException", NoSuchMethodException.class, e.getClass());
			gotException = true;
		}

		assertTrue("Should have received exception", gotException);
		assertEquals("WebViewCallback should have received error WebViewBridgeError.METHOD_NOT_FOUND", WebViewBridgeError.METHOD_NOT_FOUND, webViewCallbackError);
		assertEquals("Class name was not the same than originally was called", "com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", webViewCallbackClassname);
		assertEquals("Method name was not the same than originally was called", "apinaTestMethod", webViewCallbackMethodname);
	}

	@Test (expected = NullPointerException.class)
	public void testAllSetParametersWrong () throws Exception {
		WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", "apiTestMethod", new Object[]{1, "test"}, new MockWebViewCallback("APICALLBACK_01", 1));
	}

	@Test
	public void testAllSetClassNameEmpty () throws Exception {
		boolean gotException = false;

		try {
			WebViewBridge.handleInvocation("", "apiTestMethod", new Object[]{"test"}, new MockWebViewCallback("APICALLBACK_01", 1));
		}
		catch (NoSuchMethodException e) {
			assertEquals("Should have received exception of type NoSuchMethodException", NoSuchMethodException.class, e.getClass());
			gotException = true;
		}

		assertTrue("Should have received exception", gotException);
		assertEquals("WebViewCallback should have received error WebViewBridgeError.METHOD_NOT_FOUND", WebViewBridgeError.METHOD_NOT_FOUND, webViewCallbackError);
		assertEquals("Class name was not the same than originally was called", "", webViewCallbackClassname);
		assertEquals("Method name was not the same than originally was called", "apiTestMethod", webViewCallbackMethodname);
	}

	@Test
	public void testAllSetMethodEmpty () throws Exception {
		boolean gotException = false;

		try {
			WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", "", new Object[]{"test"}, new MockWebViewCallback("APICALLBACK_01", 1));
		}
		catch (NoSuchMethodException e) {
			assertEquals("Should have received exception of type NoSuchMethodException", NoSuchMethodException.class, e.getClass());
			gotException = true;
		}

		assertTrue("Should have received exception", gotException);
		assertEquals("WebViewCallback should have received error WebViewBridgeError.METHOD_NOT_FOUND", WebViewBridgeError.METHOD_NOT_FOUND, webViewCallbackError);
		assertEquals("Class name was not the same than originally was called", "com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", webViewCallbackClassname);
		assertEquals("Method name was not the same than originally was called", "", webViewCallbackMethodname);
	}

	@Test (expected = NullPointerException.class)
	public void testOthersSetCallbackNull () throws Exception {
		WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", "apiTestMethod", new Object[]{"test"}, null);
	}

	public class MockWebViewCallback extends WebViewCallback {

		public MockWebViewCallback(String callbackId, int invocationId) {
			super(callbackId, invocationId);
		}

		public MockWebViewCallback(Parcel in) {
			super(in);
		}

		public final Parcelable.Creator<MockWebViewCallback> CREATOR  = new Parcelable.Creator<MockWebViewCallback>() {
			public MockWebViewCallback createFromParcel(Parcel in) {
				return new MockWebViewCallback(in);
			}

			public MockWebViewCallback[] newArray(int size) {
				return new MockWebViewCallback[size];
			}
		};

		@Override
		public void invoke(Object... params) {
			// Do nothing
		}

		@Override
		public void error(Enum error, Object... params) {
			webViewCallbackError = error;
			webViewCallbackClassname = (String)params[0];
			webViewCallbackMethodname = (String)params[1];
			webViewCallbackParameters = (Object[])params[2];
		}
	}

	@Test
	public void testWrongMethodName () {
		boolean gotException = false;

		try {
			WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", "wrongMethodName", new Object[]{"test"}, new MockWebViewCallback("APICALLBACK_01", 1));
		}
		catch (Exception e) {
			assertEquals("Should have received exception of type NoSuchMethodException", NoSuchMethodException.class, e.getClass());
			gotException = true;
		}

		assertTrue("Should have received exception", gotException);
		assertEquals("WebViewCallback should have received error WebViewBridgeError.METHOD_NOT_FOUND", WebViewBridgeError.METHOD_NOT_FOUND, webViewCallbackError);
		assertEquals("Class name was not the same than originally was called", "com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", webViewCallbackClassname);
		assertEquals("Method name was not the same than originally was called", "wrongMethodName", webViewCallbackMethodname);
	}


	// THESE SHOULD WORK

	@Test
	public void testAllSetMethodNoParams () throws Exception {
		WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", "apiTestMethodNoParams", new Object[]{}, new MockWebViewCallback("APICALLBACK_01", 1));
		assertEquals(true, WebViewBridgeTestApi.invoked);
		assertNull(WebViewBridgeTestApi.value);
		assertNotNull(WebViewBridgeTestApi.callback);
		assertEquals("APICALLBACK_01", WebViewBridgeTestApi.callback.getCallbackId());
	}

	@Test
	public void testOthersSetParametersNullMethodNoParams () throws Exception {
		WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", "apiTestMethodNoParams", null, new MockWebViewCallback("APICALLBACK_01", 1));
		assertEquals(true, WebViewBridgeTestApi.invoked);
		assertNull(WebViewBridgeTestApi.value);
		assertNotNull(WebViewBridgeTestApi.callback);
		assertEquals("APICALLBACK_01", WebViewBridgeTestApi.callback.getCallbackId());
	}

	@Test
	public void testAllSetCorrectly () throws Exception {
		WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", "apiTestMethod", new Object[]{"test"}, new MockWebViewCallback("APICALLBACK_01", 1));
		assertEquals(true, WebViewBridgeTestApi.invoked);
		assertEquals("test", WebViewBridgeTestApi.value);
		assertNotNull(WebViewBridgeTestApi.callback);
		assertEquals("APICALLBACK_01", WebViewBridgeTestApi.callback.getCallbackId());
	}

	@Test
	public void testAllSetCallbackEmpty () throws Exception {
		WebViewBridge.handleInvocation("com.unity3d.ads.test.unit.WebViewBridgeTest$WebViewBridgeTestApi", "apiTestMethod", new Object[]{"test"}, new MockWebViewCallback("", 1));
		assertEquals(true, WebViewBridgeTestApi.invoked);
		assertEquals("test", WebViewBridgeTestApi.value);
		assertNotNull(WebViewBridgeTestApi.callback);
		assertEquals("", WebViewBridgeTestApi.callback.getCallbackId());
	}

	public static void nativeCallbackMethod (CallbackStatus status, String value) {
		nativeCallbackValue = value;
		nativeCallbackFinished = true;
		nativeCallbackStatus = status;
	}

	public static void nativeCallbackMethod (CallbackStatus status) {
		nativeCallbackFinished = true;
		nativeCallbackStatus = status;
	}

	@Test
	public void testHandleCallbackSingleParam () throws Exception {
		Method ncbm = WebViewBridgeTest.class.getMethod("nativeCallbackMethod", CallbackStatus.class, String.class);
		NativeCallback ncb = new NativeCallback(ncbm);
		WebViewApp.getCurrentApp().addCallback(ncb);
		Object[] params = new Object[]{"Test"};
		WebViewBridge.handleCallback(ncb.getId(), CallbackStatus.OK.toString(), params);
		assertEquals("Native Callback status was expected to be OK", CallbackStatus.OK, nativeCallbackStatus);
		assertTrue("Native Callback should have finished", nativeCallbackFinished);
		assertEquals("Native callback should have received a string as a parameter with value \"Test\"", "Test", nativeCallbackValue);
	}

	@Test
	public void testHandleCallbackNoParam () throws Exception {
		Method ncbm = WebViewBridgeTest.class.getMethod("nativeCallbackMethod", CallbackStatus.class);
		NativeCallback ncb = new NativeCallback(ncbm);
		WebViewApp.getCurrentApp().addCallback(ncb);
		Object[] params = null;
		WebViewBridge.handleCallback(ncb.getId(), CallbackStatus.OK.toString(), params);
		assertEquals("Native Callback status was expected to be OK", CallbackStatus.OK, nativeCallbackStatus);
		assertTrue("Native Callback should have finished", nativeCallbackFinished);
		assertNull("Native callback should have not received a value \"Test\"", nativeCallbackValue);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testHandleCallbackInvalidCallbackStatus () throws Exception {
		Method ncbm = WebViewBridgeTest.class.getMethod("nativeCallbackMethod", CallbackStatus.class, String.class);
		NativeCallback ncb = new NativeCallback(ncbm);
		WebViewApp.getCurrentApp().addCallback(ncb);
		Object[] params = new Object[]{"Test"};
		WebViewBridge.handleCallback(ncb.getId(), "INVALID_STATUS", params);
	}

	@Test (expected = NullPointerException.class)
	public void testHandleCallbackNullMethod () {
		Method ncbm = null;
		NativeCallback ncb = new NativeCallback(ncbm);
	}
}
