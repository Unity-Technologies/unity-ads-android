package com.unity3d.ads.test.unit;

import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.request.IWebRequestListener;
import com.unity3d.ads.request.WebRequest;
import com.unity3d.ads.request.WebRequestThread;
import com.unity3d.ads.test.TestUtilities;

import org.junit.After;
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
public class WebRequestThreadPoolTest {
    private static boolean SUCCESS = false;
    private static String RESPONSE = null;
    private static String ERROR = null;
    private static int RESPONSE_CODE = -1;

    private static boolean SUCCESS_2 = false;
    private static String RESPONSE_2 = null;
    private static String ERROR_2 = null;
    private static int RESPONSE_CODE_2 = -1;

    private static int RESPONSE_COUNT = 0;

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

        SUCCESS_2 = false;
        RESPONSE_2 = null;
        RESPONSE_CODE_2 = -1;
        ERROR_2 = null;

        RESPONSE_COUNT = 0;

        WebRequestThread.setConcurrentRequestCount(2);
    }

    @After
    public void afterTest () {
        WebRequestThread.reset();
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
        assertNull("Error should be null", ERROR);
        assertEquals("The request should have succeeded but didn't. SUCCESS=false", true, SUCCESS);
        assertEquals("Status code of the request was assumed to be 200, but it wasn't", 200, RESPONSE_CODE);
        assertEquals("Response was expected to be 'OK'", "OK", RESPONSE);
    }

    @Test
    public void testGetRequestMultipleRequests () throws Exception {
        final ConditionVariable cv1 = new ConditionVariable();
        WebRequestThread.request(validUrl, WebRequest.RequestType.GET, null, connectTimeout, readTimeout, new IWebRequestListener() {
            @Override
            public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
                SUCCESS = true;
                RESPONSE_CODE = responseCode;
                RESPONSE = response;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                cv1.open();
            }

            @Override
            public void onFailed(String url, String error) {
                SUCCESS = false;
                ERROR = error;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                cv1.open();
            }
        });

        final ConditionVariable cv2 = new ConditionVariable();
        WebRequestThread.request(validUrl, WebRequest.RequestType.POST, null, connectTimeout, readTimeout, new IWebRequestListener() {
            @Override
            public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
                SUCCESS_2 = true;
                RESPONSE_CODE_2 = responseCode;
                RESPONSE_2 = response;
                cv2.open();
            }

            @Override
            public void onFailed(String url, String error) {
                SUCCESS_2 = false;
                ERROR_2 = error;
                cv2.open();
            }
        });

        final ConditionVariable cv3 = new ConditionVariable();
        WebRequestThread.request(validUrl, WebRequest.RequestType.GET, null, connectTimeout, readTimeout, new IWebRequestListener() {
            @Override
            public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
                cv3.open();
            }

            @Override
            public void onFailed(String url, String error) {
                cv3.open();
            }
        });

        boolean success1 = cv1.block(30000);
        boolean success2 = cv2.block(1);
        boolean success3 = cv3.block(30000);

        assertTrue("ConditionVariable for first request was not opened", success1);
        assertTrue("ConditionVariable for second request was not opened", success2);
        assertTrue("ConditionVariable for third request was not opened", success3);

        assertEquals("The request should have succeeded but didn't. SUCCESS=false", true, SUCCESS);
        assertEquals("Status code of the request was assumed to be 200, but it wasn't", 200, RESPONSE_CODE);
        assertNull("Error should be null", ERROR);
        assertEquals("Response was expected to be 'OK'", "OK", RESPONSE);

        assertEquals("The request should have succeeded but didn't. SUCCESS=false", true, SUCCESS_2);
        assertEquals("Status code of the request was assumed to be 200, but it wasn't", 200, RESPONSE_CODE_2);
        assertNull("Error should be null", ERROR_2);
        assertEquals("Response was expected to be 'OK'", "OK", RESPONSE_2);
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
    public void testCancelRequests () throws Exception {
        WebRequestThread.request(validUrl, WebRequest.RequestType.GET, null, connectTimeout, readTimeout, new IWebRequestListener() {
            @Override
            public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
                SUCCESS = true;
                RESPONSE_CODE = responseCode;
                RESPONSE = response;
                RESPONSE_COUNT++;
            }

            @Override
            public void onFailed(String url, String error) {
                SUCCESS = false;
                ERROR = error;
            }
        });

        WebRequestThread.request(validUrl, WebRequest.RequestType.GET, null, connectTimeout, readTimeout, new IWebRequestListener() {
            @Override
            public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
                SUCCESS = true;
                RESPONSE_CODE = responseCode;
                RESPONSE = response;
                RESPONSE_COUNT++;
            }

            @Override
            public void onFailed(String url, String error) {
                SUCCESS = false;
                ERROR = error;
               }
        });

        WebRequestThread.request(validUrl, WebRequest.RequestType.GET, null, connectTimeout, readTimeout, new IWebRequestListener() {
            @Override
            public void onComplete(String url, String response, int responseCode, Map<String, List<String>> headers) {
                SUCCESS = true;
                RESPONSE_CODE = responseCode;
                RESPONSE = response;
                RESPONSE_COUNT++;
            }

            @Override
            public void onFailed(String url, String error) {
                SUCCESS = false;
                ERROR = error;
            }
        });

        WebRequestThread.cancel();

        final ConditionVariable cv = new ConditionVariable();
        boolean success = cv.block(500);

        assertEquals("response count should be zero", 0, RESPONSE_COUNT);
        assertEquals("Shouldn't have received a responseCode", -1, RESPONSE_CODE);
        assertNull("Shouldn't have received a response", RESPONSE);
        assertFalse("Shouldn't have received success", SUCCESS);
        assertNull("Shouldn't have received and error", ERROR);
    }
}
