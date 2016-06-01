package com.unity3d.ads.test.unit;

import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.configuration.Configuration;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.bridge.CallbackStatus;
import com.unity3d.ads.webview.bridge.NativeCallback;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RunWith(AndroidJUnit4.class)
public class NativeCallbackTest {
	private static boolean INVOKED = false;
	private static CallbackStatus STATUS = null;
	private static String VALUE = null;

	public static void invalidResponseMethod () {
		INVOKED = true;
	}

	public static void validResponseMethod (CallbackStatus status, String value) {
		INVOKED = true;
		STATUS = status;
		VALUE = value;
	}

	@BeforeClass
	public static void prepareTests () throws Exception {
		final Configuration conf = new Configuration(TestUtilities.getTestServerAddress());
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebAppLoaded(true);
				WebViewApp.getCurrentApp().setWebAppInitialized(true);
			}
		}, 100);

		WebViewApp.create(conf);
	}

	@Before
	public void resetTests () throws Exception {
		INVOKED = false;
		STATUS = null;
		VALUE = null;
	}

	@Test (expected = NullPointerException.class)
	public void testNullMethod () {
		NativeCallback cb = new NativeCallback(null);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testInvalidMethodOK () throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Class<NativeCallbackTest> cls = NativeCallbackTest.class;
		Method responseMethod;
		responseMethod = cls.getMethod("invalidResponseMethod");
		NativeCallback cb = new NativeCallback(responseMethod);
		assertNotNull(cb.getId());
		cb.invoke("OK");
		assertFalse("Callback should not have been invoked", INVOKED);
		assertNull("Status should still be NULL", STATUS);
		assertNull("Value should still be NULL", VALUE);
	}

	@Test
	public void testValidMethodOK () throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Class<NativeCallbackTest> cls = NativeCallbackTest.class;
		Method responseMethod;
		responseMethod = cls.getMethod("validResponseMethod", CallbackStatus.class, String.class);
		NativeCallback cb = new NativeCallback(responseMethod);
		assertNotNull(cb.getId());
		cb.invoke("OK", "We are okay");
		assertTrue("Callback should have been invoked", INVOKED);
		assertEquals("Callback status should be OK", CallbackStatus.OK, STATUS);
		assertEquals("Callback value should be the same as was sent", "We are okay", VALUE);
	}

	@Test
	public void testValidMethodERROR () throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Class<NativeCallbackTest> cls = NativeCallbackTest.class;
		Method responseMethod;
		responseMethod = cls.getMethod("validResponseMethod", CallbackStatus.class, String.class);
		NativeCallback cb = new NativeCallback(responseMethod);
		assertNotNull(cb.getId());
		cb.invoke("ERROR", "We are broken");
		assertTrue("Callback should have been invoked", INVOKED);
		assertEquals("Callback status should be ERROR", CallbackStatus.ERROR, STATUS);
		assertEquals("Callback value should be the same as was sent", "We are broken", VALUE);
	}
}