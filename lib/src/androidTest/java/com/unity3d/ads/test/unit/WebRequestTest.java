package com.unity3d.ads.test.unit;

import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.request.IResolveHostListener;
import com.unity3d.ads.request.IWebRequestListener;
import com.unity3d.ads.request.ResolveHostError;
import com.unity3d.ads.request.WebRequest;
import com.unity3d.ads.request.WebRequestResultReceiver;
import com.unity3d.ads.request.WebRequestThread;
import com.unity3d.ads.test.TestUtilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class WebRequestTest {
	private static boolean SUCCESS = false;
	private static String RESPONSE = null;
	private static String ERROR = null;
	private static int RESPONSE_CODE = -1;

	private static String ADDRESS = null;
	private static String HOST = null;

	private static String validUrl = TestUtilities.getTestServerAddress();
	private static String invalidUrl = "http://localhost-invalid:8000";

	private static int connectTimeout = 30000;
	private static int readTimeout = 30000;

	@Before
	public void prepareTests () {
		SUCCESS = false;
		RESPONSE = null;
		RESPONSE_CODE = -1;
		ERROR = null;
		ADDRESS = null;
		HOST = null;
	}

	@Test
	public void testGetRequest () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		WebRequestThread.request(validUrl, WebRequest.RequestType.GET, null, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
				SUCCESS = true;
				RESPONSE_CODE = responseCode;
				RESPONSE = response;
				cv.open();
			}

			@Override
			public void onFailed(String url, String error) {
				SUCCESS = false;
				ERROR = error;
				cv.open();
			}
		});

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);
		assertEquals("The request should have succeeded but didn't. SUCCESS=false", true, SUCCESS);
		assertEquals("Status code of the request was assumed to be 200, but it wasn't", 200, RESPONSE_CODE);
		assertNull("Error should be null", ERROR);
		assertEquals("Response was expected to be 'OK'", "OK", RESPONSE);
	}

	@Test
	public void testPostRequest () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		WebRequestThread.request(validUrl, WebRequest.RequestType.POST, null, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
				SUCCESS = true;
				RESPONSE_CODE = responseCode;
				RESPONSE = response;
				cv.open();
			}

			@Override
			public void onFailed(String url, String error) {
				SUCCESS = false;
				ERROR = error;
				cv.open();
			}
		});

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);
		assertEquals("The request should have succeeded but didn't. SUCCESS=false", true, SUCCESS);
		assertEquals("Status code of the request was assumed to be 200, but it wasn't", 200, RESPONSE_CODE);
		assertEquals("Response was expected to be 'OK'", "OK", RESPONSE);
	}

	@Test
	public void testEmptyGetUrl () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		WebRequestThread.request("", WebRequest.RequestType.GET, null, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
				SUCCESS = true;
				RESPONSE_CODE = responseCode;
				RESPONSE = response;
				cv.open();
			}

			@Override
			public void onFailed(String url, String error) {
				SUCCESS = false;
				ERROR = error;
				cv.open();
			}
		});

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);
		assertEquals("The request should have failed but didn't. SUCCESS=true", false, SUCCESS);
		assertEquals("Status code of the request was assumed to be -1, but it wasn't", -1, RESPONSE_CODE);
		assertEquals("Error message was different than expected", "Request is NULL or too short", ERROR);
	}

	@Test
	public void testEmptyPostUrl () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		WebRequestThread.request("", WebRequest.RequestType.POST, null, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
				SUCCESS = true;
				RESPONSE_CODE = responseCode;
				RESPONSE = response;
				cv.open();
			}

			@Override
			public void onFailed(String url, String error) {
				SUCCESS = false;
				ERROR = error;
				cv.open();
			}
		});

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);
		assertEquals("The request should have failed but didn't. SUCCESS=true", false, SUCCESS);
		assertEquals("Status code of the request was assumed to be -1, but it wasn't", -1, RESPONSE_CODE);
		assertEquals("Error message was different than expected", "Request is NULL or too short", ERROR);
	}

	@Test
	public void testNullGetUrl () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		WebRequestThread.request(null, WebRequest.RequestType.GET, null, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
				SUCCESS = true;
				RESPONSE_CODE = responseCode;
				RESPONSE = response;
				cv.open();
			}

			@Override
			public void onFailed(String url, String error) {
				SUCCESS = false;
				ERROR = error;
				cv.open();
			}
		});

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);
		assertEquals("The request should have failed but didn't. SUCCESS=true", false, SUCCESS);
		assertEquals("Status code of the request was assumed to be -1, but it wasn't", -1, RESPONSE_CODE);
		assertEquals("Error message was different than expected", "Request is NULL or too short", ERROR);
	}

	@Test
	public void testNullPostUrl () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		WebRequestThread.request(null, WebRequest.RequestType.POST, null, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
				SUCCESS = true;
				RESPONSE_CODE = responseCode;
				RESPONSE = response;
				cv.open();
			}

			@Override
			public void onFailed(String url, String error) {
				SUCCESS = false;
				ERROR = error;
				cv.open();
			}
		});

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);
		assertEquals("The request should have failed but didn't. SUCCESS=true", false, SUCCESS);
		assertEquals("Status code of the request was assumed to be -1, but it wasn't", -1, RESPONSE_CODE);
		assertEquals("Error message was different than expected", "Request is NULL or too short", ERROR);
	}

	@Test
	public void testInvalidGetUrl () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		WebRequestThread.request(invalidUrl, WebRequest.RequestType.GET, null, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
				SUCCESS = true;
				RESPONSE_CODE = responseCode;
				RESPONSE = response;
				cv.open();
			}

			@Override
			public void onFailed(String url, String error) {
				SUCCESS = false;
				ERROR = error;
				cv.open();
			}
		});

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);
		assertEquals("The request should have failed but didn't. SUCCESS=true", false, SUCCESS);
		assertEquals("Status code of the request was assumed to be -1, but it wasn't", -1, RESPONSE_CODE);
		assertNotNull("There should be a error message", ERROR);
		assertFalse("Error message should contain something", ERROR.isEmpty());
	}

	@Test
	public void testInvalidPostUrl () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		WebRequestThread.request(invalidUrl, WebRequest.RequestType.POST, null, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
				SUCCESS = true;
				RESPONSE_CODE = responseCode;
				RESPONSE = response;
				cv.open();
			}

			@Override
			public void onFailed(String url, String error) {
				SUCCESS = false;
				ERROR = error;
				cv.open();
			}
		});

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);
		assertEquals("The request should have failed but didn't. SUCCESS=true", false, SUCCESS);
		assertEquals("Status code of the request was assumed to be -1, but it wasn't", -1, RESPONSE_CODE);
		assertNotNull("There should be a error message", ERROR);
		assertFalse("Error message should contain something", ERROR.isEmpty());
	}

	@Test
	public void testGetRequestInvalidRequestMessage () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		IWebRequestListener listener = new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
				SUCCESS = true;
				RESPONSE_CODE = responseCode;
				RESPONSE = response;
				cv.open();
			}

			@Override
			public void onFailed(String url, String error) {
				SUCCESS = false;
				ERROR = error;
				cv.open();
			}
		};

		Handler handler = new Handler(Looper.getMainLooper());
		WebRequestThread.request(2, validUrl, WebRequest.RequestType.GET, null, null, connectTimeout, readTimeout, listener, new WebRequestResultReceiver(handler, listener));

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);
		assertEquals("The request should not have succeeded but did. SUCCESS=true", false, SUCCESS);
		assertEquals("Status code of the request was assumed to be -1, but it wasn't", -1, RESPONSE_CODE);
		assertTrue("Error message was different than expected", ERROR.startsWith("Invalid Thread Message"));
	}

	@Test
	public void testGetRequestInvalidResultMessage () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		IWebRequestListener listener = new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
				SUCCESS = true;
				RESPONSE_CODE = responseCode;
				RESPONSE = response;
				cv.open();
			}

			@Override
			public void onFailed(String url, String error) {
				SUCCESS = false;
				ERROR = error;
				cv.open();
			}
		};

		Handler handler = new Handler(Looper.getMainLooper());
		WebRequestThread.request(2, validUrl, WebRequest.RequestType.GET, null, null, connectTimeout, readTimeout, listener, new MockWebRequestResultReceiver(handler, listener));

		boolean success = cv.block(30000);
		assertTrue("ConditionVariable was not opened", success);
		assertEquals("The request should not have succeeded but did. SUCCESS=true", false, SUCCESS);
		assertEquals("Status code of the request was assumed to be -1, but it wasn't", -1, RESPONSE_CODE);
		assertTrue("Error message was different than expected", ERROR.startsWith("Invalid resultCode"));
	}

	@Test
	public void testCancelRequests () throws Exception {
		WebRequestThread.request(validUrl, WebRequest.RequestType.GET, null, connectTimeout, readTimeout, new IWebRequestListener() {
			@Override
			public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
				SUCCESS = true;
				RESPONSE_CODE = responseCode;
				RESPONSE = response;
			}

			@Override
			public void onFailed(String url, String error) {
				SUCCESS = false;
				ERROR = error;
			}
		});

		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				WebRequestThread.cancel();
			}
		}, 10);

		final ConditionVariable cv = new ConditionVariable();
		boolean success = cv.block(500);

		assertEquals("Shouldn't have received a responseCode", -1, RESPONSE_CODE);
		assertNull("Shouldn't have received a repsonse", RESPONSE);
		assertFalse("Shouldn't have received success", SUCCESS);
		assertNull("Shouldn't have received and error", ERROR);
	}

	@Test
	public void testResolveHost () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		boolean resolveSuccess = WebRequestThread.resolve("google-public-dns-a.google.com", new IResolveHostListener() {
			@Override
			public void onResolve(String host, String address) {
				HOST = host;
				ADDRESS = address;
				cv.open();
			}

			@Override
			public void onFailed(String host, ResolveHostError error, String errorMessage) {
				cv.open();
			}
		});

		boolean cvSuccess = cv.block(30000);

		assertTrue("Resolve should have succeeded", resolveSuccess);
		assertTrue("Condition variable was not opened (Resolving host took too long)", cvSuccess);
		assertEquals("Host should still be: google-public-dns-a.google.com", "google-public-dns-a.google.com", HOST);
		assertEquals("Host Address not what was expected", "8.8.8.8", ADDRESS);
	}


	public class MockWebRequestResultReceiver extends WebRequestResultReceiver {
		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			resultCode = 12345;
			super.onReceiveResult(resultCode, resultData);
		}

		public MockWebRequestResultReceiver(Handler handler, IWebRequestListener listener) {
			super(handler, listener);
		}
	}
}
