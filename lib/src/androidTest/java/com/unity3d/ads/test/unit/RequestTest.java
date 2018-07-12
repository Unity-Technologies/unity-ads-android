package com.unity3d.ads.test.unit;

import android.os.ConditionVariable;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.api.Request;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.request.WebRequestError;
import com.unity3d.ads.request.WebRequestEvent;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;
import com.unity3d.ads.webview.bridge.CallbackStatus;
import com.unity3d.ads.webview.bridge.Invocation;
import com.unity3d.ads.webview.bridge.WebViewCallback;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class RequestTest {
	private static CallbackStatus CALLBACK_STATUS = null;
	private static Enum CALLBACK_ERROR = null;
	private static Object[] CALLBACK_PARAMS = null;
	private static String CALLBACK_ID = null;

	private static Object[] EVENT_PARAMS = null;
	private static Enum EVENT_CATEGORY = null;
	private static Enum EVENT_ID = null;

	private static String validUrl = TestUtilities.getTestServerAddress();
	private static String invalidUrl = "http://localhost-invalid:8000";

	private static int connectTimeout = 10000;
	private static int readTimeout = 10000;

	@BeforeClass
	public static void prepareTests () {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
	}

	@Before
	public void resetTests () {
		CALLBACK_STATUS = null;
		CALLBACK_ERROR = null;
		CALLBACK_PARAMS = null;
		CALLBACK_ID = null;
		EVENT_PARAMS = null;
		EVENT_CATEGORY = null;
		EVENT_ID = null;
	}

	@Test
	public void testValidGetWithNullHeaders () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}
		};

		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_Valid_Get_No_Headers", invocation.getId());
		Request.get("1", validUrl, null, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();
		boolean success = cv.block(30000);

		assertTrue("ConditionVariable was not opened", success);
		assertEquals("Callback status was not OK, successfull callback was expected", CallbackStatus.OK, CALLBACK_STATUS);
		assertNull("Callback error was not NULL, successfull callback was expected", CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);
		assertEquals("Request has changed for some reason", validUrl, EVENT_PARAMS[1]);
		assertEquals("Response code was not OK (200)", 200, EVENT_PARAMS[3]);
		assertEquals("Event ID was incorrect", WebRequestEvent.COMPLETE, EVENT_ID);
		assertEquals("Event Category was incorrect", WebViewEventCategory.REQUEST, EVENT_CATEGORY);
	}

	// TODO: This test doesn't work with Android 6.0.
	@Test
	public void testValidPostWithNullHeaders () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}
		};

		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_Valid_Post_No_Headers", invocation.getId());
		Request.post("1", validUrl, null, null, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();

		boolean success = cv.block(30000);

		assertTrue("ConditionVariable was not opened", success);
		assertEquals("Callback status was not OK, successfull callback was expected", CallbackStatus.OK, CALLBACK_STATUS);
		assertNull("Callback error was not NULL, successfull callback was expected", CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);

		assertEquals("Request has changed for some reason", validUrl, EVENT_PARAMS[1]);
		assertEquals("Response code was not OK (200)", 200, EVENT_PARAMS[3]);
		assertEquals("Event ID was incorrect", WebRequestEvent.COMPLETE, EVENT_ID);
		assertEquals("Event Category was incorrect", WebViewEventCategory.REQUEST, EVENT_CATEGORY);
	}

	@Test
	public void testValidPostWithNullHeadersAndPostBody () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}
		};

		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_Valid_Post_No_Headers_Post_Body", invocation.getId());
		Request.post("1", validUrl, "Testing Testing", null, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();

		boolean success = cv.block(30000);

		assertTrue("ConditionVariable was not opened", success);
		assertEquals("Callback status was not OK, successfull callback was expected", CallbackStatus.OK, CALLBACK_STATUS);
		assertNull("Callback error was not NULL, successfull callback was expected", CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);

		assertEquals("Request has changed for some reason", validUrl, EVENT_PARAMS[1]);
		assertEquals("Response code was not OK (200)", 200, EVENT_PARAMS[3]);
		assertEquals("Event ID was incorrect", WebRequestEvent.COMPLETE, EVENT_ID);
		assertEquals("Event Category was incorrect", WebViewEventCategory.REQUEST, EVENT_CATEGORY);
	}

	@Test
	public void testValidPostWithValidHeadersAndPostBody () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}
		};

		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_Valid_Post_No_Headers_Post_Body", invocation.getId());
		JSONArray headers = new JSONArray("[[\"test-header\", \"test\"]]");
		Request.post("1", validUrl, "Testing Testing", headers, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);

		JSONArray receivedHeaders = (JSONArray)EVENT_PARAMS[4];
		boolean testHeaderExists = false;
		for (int i = 0; i < receivedHeaders.length(); i++) {
			if (((JSONArray)receivedHeaders.get(i)).get(0).equals("test-header") && ((JSONArray)receivedHeaders.get(i)).get(1).equals("test")) {
				testHeaderExists = true;
			}
		}

		assertTrue("There should be headers in the connection result", receivedHeaders.length() > 0);
		assertEquals("Callback status was not OK, successfull callback was expected", CallbackStatus.OK, CALLBACK_STATUS);
		assertNull("Callback error was not NULL, successfull callback was expected", CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);
		assertTrue("Header \"Test-Header\" not found or its content was not \"test\"", testHeaderExists);
		assertEquals("Request has changed for some reason", validUrl, EVENT_PARAMS[1]);
		assertEquals("Response code was not OK (200)", 200, EVENT_PARAMS[3]);
		assertEquals("Event ID was incorrect", WebRequestEvent.COMPLETE, EVENT_ID);
		assertEquals("Event Category was incorrect", WebViewEventCategory.REQUEST, EVENT_CATEGORY);
	}

	@Test
	public void testInvalidUrlGetWithNullHeaders () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}
		};

		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_InvalidUrl_Get_No_Headers", invocation.getId());
		Request.get("1", invalidUrl, null, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();

		boolean success = cv.block(30000);

		assertTrue("ConditionVariable was not opened", success);
		assertEquals("Callback status was not OK, successfull callback was expected", CallbackStatus.OK, CALLBACK_STATUS);
		assertNull("Callback error was not NULL, successfull callback was expected", CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);
		assertEquals("Request has changed for some reason", invalidUrl, EVENT_PARAMS[1]);
		assertEquals("Expected three (3) params in event parameters", 3, EVENT_PARAMS.length);
		assertEquals("Event ID was incorrect", WebRequestEvent.FAILED, EVENT_ID);
		assertEquals("Event Category was incorrect", WebViewEventCategory.REQUEST, EVENT_CATEGORY);
	}

	@Test
	public void testInvalidUrlPostWithNullHeaders () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}
		};

		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_InvalidUrl_Post_No_Headers", invocation.getId());
		Request.post("1", invalidUrl, null, null, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();

		boolean success = cv.block(30000);

		assertTrue("ConditionVariable was not opened", success);
		assertEquals("Callback status was not OK, successfull callback was expected", CallbackStatus.OK, CALLBACK_STATUS);
		assertNull("Callback error was not NULL, successfull callback was expected", CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);
		assertEquals("Request has changed for some reason", invalidUrl, EVENT_PARAMS[1]);
		assertEquals("Expected two (3) params in event parameters", 3, EVENT_PARAMS.length);
		assertEquals("Event ID was incorrect", WebRequestEvent.FAILED, EVENT_ID);
		assertEquals("Event Category was incorrect", WebViewEventCategory.REQUEST, EVENT_CATEGORY);
	}

	@Test
	public void testValidGetWithHeader () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}
		};

		JSONArray headers = new JSONArray("[[\"test-header\", \"test\"]]");

		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_Valid_Get_With_Header", invocation.getId());
		Request.get("1", validUrl, headers, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);

		JSONArray receivedHeaders = (JSONArray)EVENT_PARAMS[4];
		boolean testHeaderExists = false;
		for (int i = 0; i < receivedHeaders.length(); i++) {
			if (((JSONArray)receivedHeaders.get(i)).get(0).equals("test-header") && ((JSONArray)receivedHeaders.get(i)).get(1).equals("test")) {
				testHeaderExists = true;
			}
		}

		assertTrue("There should be headers in the connection result", receivedHeaders.length() > 0);
		assertEquals("Callback status was not OK, successfull callback was expected", CallbackStatus.OK, CALLBACK_STATUS);
		assertNull("Callback error was not NULL, successfull callback was expected", CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);
		assertTrue("Header \"Test-Header\" not found or its content was not \"test\"", testHeaderExists);
		assertEquals("Request has changed for some reason", validUrl, EVENT_PARAMS[1]);
		assertEquals("Response code was not OK (200)", 200, EVENT_PARAMS[3]);
		assertEquals("Event ID was incorrect", WebRequestEvent.COMPLETE, EVENT_ID);
		assertEquals("Event Category was incorrect", WebViewEventCategory.REQUEST, EVENT_CATEGORY);
	}

	@Test
	public void testValidPostWithHeader () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}
		};

		JSONArray headers = new JSONArray("[[\"test-header\", \"test\"]]");
		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_Valid_Get_With_Header", invocation.getId());
		Request.post("1", validUrl, null, headers, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);

		JSONArray receivedHeaders = (JSONArray)EVENT_PARAMS[4];
		boolean testHeaderExists = false;
		for (int i = 0; i < receivedHeaders.length(); i++) {
			if (((JSONArray)receivedHeaders.get(i)).get(0).equals("test-header") && ((JSONArray)receivedHeaders.get(i)).get(1).equals("test")) {
				testHeaderExists = true;
			}
		}

		assertTrue("There should be headers in the connection result", receivedHeaders.length() > 0);
		assertEquals("Callback status was not OK, successfull callback was expected", CallbackStatus.OK, CALLBACK_STATUS);
		assertNull("Callback error was not NULL, successfull callback was expected", CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);
		assertTrue("Header \"Test-Header\" not found or its content was not \"test\"", testHeaderExists);
		assertEquals("Request has changed for some reason", validUrl, EVENT_PARAMS[1]);
		assertEquals("Response code was not OK (200)", 200, EVENT_PARAMS[3]);
		assertEquals("Event ID was incorrect", WebRequestEvent.COMPLETE, EVENT_ID);
		assertEquals("Event Category was incorrect", WebViewEventCategory.REQUEST, EVENT_CATEGORY);
	}

	@SdkSuppress(minSdkVersion = 16)
	@Test
	public void testValidGetWithInvalidHeader () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}
		};

		JSONArray headers = new JSONArray("[[\"C,o-n#n#e*c*t*io*n\", \"close\"]]");
		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_Valid_Get_With_Invalid_Header", invocation.getId());
		Request.get("1", validUrl, headers, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();

		boolean success = cv.block(30000);

		assertTrue("ConditionVariable was not opened", success);
		assertEquals("Callback status was not OK, successfull callback was expected", CallbackStatus.OK, CALLBACK_STATUS);
		assertNull("Callback error was not NULL, successfull callback was expected", CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);
		assertEquals("Request has changed for some reason", validUrl, EVENT_PARAMS[1]);
		assertEquals("Expected three (3) params in event parameters", 3, EVENT_PARAMS.length);
		assertEquals("Event Category was incorrect", WebViewEventCategory.REQUEST, EVENT_CATEGORY);
		assertEquals("Event ID was incorrect", WebRequestEvent.FAILED, EVENT_ID);
	}

	@SdkSuppress(minSdkVersion = 16)
	@Test
	public void testValidPostWithInvalidHeader () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}
		};

		JSONArray headers = new JSONArray("[[\"C,o-n#n#e*c*t*io*n\", \"close\"]]");
		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_Valid_Post_With_Invalid_Header", invocation.getId());
		Request.post("1", validUrl, null, headers, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();

		boolean success = cv.block(30000);

		assertTrue("ConditionVariable was not opened", success);
		assertEquals("Callback status was not OK, successfull callback was expected", CallbackStatus.OK, CALLBACK_STATUS);
		assertNull("Callback error was not NULL, successfull callback was expected", CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);
		assertEquals("Request has changed for some reason", validUrl, EVENT_PARAMS[1]);
		assertEquals("Expected three (3) params in event parameters", 3, EVENT_PARAMS.length);
		assertEquals("Event Category was incorrect", WebViewEventCategory.REQUEST, EVENT_CATEGORY);
		assertEquals("Event ID was incorrect", WebRequestEvent.FAILED, EVENT_ID);
	}

	@Test
	public void testValidGetWithMalformedHeaderMap () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}

			@Override
			public boolean invokeCallback(Invocation invocation) {
				super.invokeCallback(invocation);
				cv.open();

				return true;
			}
		};

		JSONArray headers = new JSONArray("[\"close\"]]");
		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_Valid_Get_With_Malformed_Header_Map", invocation.getId());
		Request.get("1", validUrl, headers, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();

		boolean success = cv.block(30000);

		assertTrue("ConditionVariable was not opened", success);
		assertEquals("Callback status was not ERROR, error callback was expected", CallbackStatus.ERROR, CALLBACK_STATUS);
		assertEquals("Callback error was not what was expected", WebRequestError.MAPPING_HEADERS_FAILED, CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);
	}

	@Test
	public void testValidPostWithMalformedHeaderMap () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		MockWebViewApp webViewApp = new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				EVENT_CATEGORY = eventCategory;
				EVENT_ID = eventId;
				EVENT_PARAMS = params;
				cv.open();

				return true;
			}

			@Override
			public boolean invokeCallback(Invocation invocation) {
				super.invokeCallback(invocation);
				cv.open();

				return true;
			}
		};

		JSONArray headers = new JSONArray("[\"close\"]]");
		WebViewApp.setCurrentApp(webViewApp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("Test_Valid_Post_With_Malformed_Header_Map", invocation.getId());
		Request.post("1", validUrl, null, headers, connectTimeout, readTimeout, callback);
		invocation.sendInvocationCallback();

		boolean success = cv.block(30000);

		assertTrue("ConditionVariable was not opened", success);
		assertEquals("Callback status was not ERROR, error callback was expected", CallbackStatus.ERROR, CALLBACK_STATUS);
		assertEquals("Callback error was not what was expected", WebRequestError.MAPPING_HEADERS_FAILED, CALLBACK_ERROR);
		assertEquals("Callback params list length was expected to be 2 (callback id and request id in params)", 2, CALLBACK_PARAMS.length);
		assertEquals("Callback first param was expected to be Callback ID", callback.getCallbackId(), CALLBACK_ID);
	}

	public class MockWebViewApp extends WebViewApp {
		public MockWebViewApp () {
			super();
		}

		@Override
		public boolean invokeCallback(Invocation invocation) {

			for (ArrayList<Object> response : invocation.getResponses()) {
				CallbackStatus status = (CallbackStatus)response.get(0);
				Enum error = (Enum)response.get(1);
				Object[] params = (Object[])response.get(2);

				ArrayList<Object> paramList = new ArrayList<>();
				paramList.addAll(Arrays.asList(params));
				paramList.add(1, status.name());

				CALLBACK_STATUS = status;
				CALLBACK_ERROR = error;
				CALLBACK_PARAMS = params;
				CALLBACK_ID = (String)params[0];
			}

			return true;
		}
	}
}
