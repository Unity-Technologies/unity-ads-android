package com.unity3d.ads.test.legacy;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.webkit.ValueCallback;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.services.core.webview.WebView;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.Invocation;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class InvocationTest {

	private static Class[] apiTestClassList = {
			InvocationTest.BatchInvocationTestApi.class
	};

	public static class BatchInvocationTestApi {
		public static boolean INVOKED = false;
		public static String VALUE = null;
		public static WebViewCallback CALLBACK = null;
		public static int INVOCATION_COUNT = 0;
		public static boolean JAVASCRIPT_INVOKED = false;

		@WebViewExposed
		public static void apiTestMethod (String value, WebViewCallback callback) {
			DeviceLog.entered();
			INVOKED = true;
			VALUE = value;
			CALLBACK = callback;
			INVOCATION_COUNT++;
			callback.invoke(value);
		}

		@WebViewExposed
		public static void apiTestMethodNoParams (WebViewCallback callback) {
			DeviceLog.entered();
			INVOKED = true;
			VALUE = null;
			CALLBACK = callback;
			INVOCATION_COUNT++;
			callback.invoke();
		}
	}

	@BeforeClass
	public static void prepareTests () {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
	}

	@Before
	public void beforeTest () {
		BatchInvocationTestApi.INVOKED = false;
		BatchInvocationTestApi.VALUE = null;
		BatchInvocationTestApi.CALLBACK = null;
		BatchInvocationTestApi.INVOCATION_COUNT = 0;
		BatchInvocationTestApi.JAVASCRIPT_INVOKED = false;

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
		}, 300);

		WebViewApp.create(config);
	}

	@Test
	public void testBasicBatchInvocation () {
		Invocation batch = new Invocation();

		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebView(new WebView(ClientProperties.getApplicationContext(), false) {
					@Override
					public void evaluateJavascript(String data, ValueCallback<String> callback) {
						BatchInvocationTestApi.JAVASCRIPT_INVOKED = true;
					}
				});
				cv.open();
			}
		}, 100);

		boolean success = cv.block(30000);
		assertTrue("Condition Variable was not opened", success);
		batch.addInvocation("com.unity3d.ads.test.legacy.InvocationTest$BatchInvocationTestApi", "apiTestMethod", new Object[]{"test1"}, new WebViewCallback("CALLBACK_01", batch.getId()));
		batch.addInvocation("com.unity3d.ads.test.legacy.InvocationTest$BatchInvocationTestApi", "apiTestMethod", new Object[]{"test2"}, new WebViewCallback("CALLBACK_02", batch.getId()));
		batch.addInvocation("com.unity3d.ads.test.legacy.InvocationTest$BatchInvocationTestApi", "apiTestMethod", new Object[]{"test3"}, new WebViewCallback("CALLBACK_03", batch.getId()));
		batch.addInvocation("com.unity3d.ads.test.legacy.InvocationTest$BatchInvocationTestApi", "apiTestMethod", new Object[]{"test4"}, new WebViewCallback("CALLBACK_04", batch.getId()));

		batch.nextInvocation();
		batch.nextInvocation();
		batch.nextInvocation();
		batch.nextInvocation();
		batch.sendInvocationCallback();

		assertTrue("Invocation should have happened", BatchInvocationTestApi.INVOKED);
		assertTrue("Javascript invocation should have happened", BatchInvocationTestApi.JAVASCRIPT_INVOKED);
		assertEquals("Invocation count was different than expected", 4, BatchInvocationTestApi.INVOCATION_COUNT);
		assertEquals("Invocation response value was different than expected", "test4", BatchInvocationTestApi.VALUE);
	}

	@Test
	public void testBatchInvocationOneInvalidMethod () {
		Invocation batch = new Invocation();

		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebView(new WebView(ClientProperties.getApplicationContext(), false) {
					@Override
					public void evaluateJavascript(String data, ValueCallback<String> callback) {
						BatchInvocationTestApi.JAVASCRIPT_INVOKED = true;
					}
				});
				cv.open();
			}
		}, 100);

		boolean success = cv.block(30000);
		assertTrue("Condition Variable was not opened", success);

		batch.addInvocation("com.unity3d.ads.test.legacy.InvocationTest$BatchInvocationTestApi", "apiTestMethodNonExistent", new Object[]{"test1"}, new WebViewCallback("CALLBACK_01", batch.getId()));
		batch.addInvocation("com.unity3d.ads.test.legacy.InvocationTest$BatchInvocationTestApi", "apiTestMethod", new Object[]{"test2"}, new WebViewCallback("CALLBACK_02", batch.getId()));
		batch.addInvocation("com.unity3d.ads.test.legacy.InvocationTest$BatchInvocationTestApi", "apiTestMethod", new Object[]{"test3"}, new WebViewCallback("CALLBACK_03", batch.getId()));
		batch.addInvocation("com.unity3d.ads.test.legacy.InvocationTest$BatchInvocationTestApi", "apiTestMethod", new Object[]{"test4"}, new WebViewCallback("CALLBACK_04", batch.getId()));

		batch.nextInvocation();
		batch.nextInvocation();
		batch.nextInvocation();
		batch.nextInvocation();
		batch.sendInvocationCallback();

		assertTrue("Invocation should have happened", BatchInvocationTestApi.INVOKED);
		assertTrue("Javascript invocation should have happened", BatchInvocationTestApi.JAVASCRIPT_INVOKED);
		assertEquals("Successfull invocation count was different than expected", 3, BatchInvocationTestApi.INVOCATION_COUNT);
		assertEquals("Invocation response value should have been set", BatchInvocationTestApi.VALUE, "test4");
	}

	@Test
	public void testWebViewBatchCallbackWriteAndReadParcel () {
		Invocation batch = new Invocation();
		WebViewCallback callback = new WebViewCallback("CALLBACK_01", batch.getId());
		Parcel parcel = Parcel.obtain();
		callback.writeToParcel(parcel, 0);
		parcel.setDataPosition(0);
		WebViewCallback callbackFromParcel = WebViewCallback.CREATOR.createFromParcel(parcel);
		WebViewCallback[] arrayWebViewCallback = WebViewCallback.CREATOR.newArray(10);

		assertEquals("InvocationID should be the same. ", callback.getInvocationId(), callbackFromParcel.getInvocationId());
		assertEquals("CallbackID should be the same. ", callback.getCallbackId(), callbackFromParcel.getCallbackId());
		assertEquals("The array should contain 10 elements. ", 10, arrayWebViewCallback.length);
		assertEquals("Describe contents value was wrong. ", 45678, callback.describeContents());
	}
}
