package com.unity3d.ads.test.unit;

import android.os.ConditionVariable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.adunit.AdUnitEvent;
import com.unity3d.ads.api.Cache;
import com.unity3d.ads.cache.CacheError;
import com.unity3d.ads.cache.CacheEvent;
import com.unity3d.ads.cache.CacheThread;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.request.WebRequestEvent;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.ads.video.VideoPlayerEvent;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;
import com.unity3d.ads.webview.bridge.CallbackStatus;
import com.unity3d.ads.webview.bridge.Invocation;
import com.unity3d.ads.webview.bridge.WebViewCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CacheTest {
	private static final String NON_CACHED_FILE_ID = "not_cached";
	private static final String CACHED_FILE_ID = "cached";

	private static final String REMOTE_IMG = TestUtilities.getTestServerAddress() + "/google.png";
	private static final long REMOTE_IMG_SIZE = 13504;
	private static final String REMOTE_IMG_FILE_ID = "google.png";

	private static final String REMOTE_VIDEO = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
	private static final long REMOTE_VIDEO_SIZE = 134924;
	private static final String REMOTE_VIDEO_FILE_ID = "blue_test_trailer.mp4";

	private static final String FLAG_FILE_NOT_FOUND = "fileNotFound";
	private static final String FLAG_JSON_EXCEPTION_RECEIVED = "jsonExceptionReceived";

	@Before
	public void setup() throws IOException {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
		assertNotEquals("Cache directory has not been properly initialized", null, SdkProperties.getCacheDirectory());

		// Write test file to cache directory
		try {
			File f = new File(SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + CACHED_FILE_ID);
			FileOutputStream fos = new FileOutputStream(f, true);
			fos.write("test".getBytes());
			fos.flush();
			fos.close();
		} catch(IOException e) {
			throw new IOException("testGetFileUrlFileFound unable to write test file", e.getCause());
		}

		File img = new File(SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + REMOTE_IMG_FILE_ID);
		img.delete();

		File video = new File(SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + REMOTE_VIDEO_FILE_ID);
		video.delete();
	}

	@Test
	public void testGetFilePathFileNotFound() {
		MockWebViewApp webapp = new MockWebViewApp() {
			@Override
			public void callbackError(Enum error, Object... params) {
				if(error == CacheError.FILE_NOT_FOUND && params.length == 0) {
					setFlag(FLAG_FILE_NOT_FOUND);
				}
			}
		};

		WebViewApp.setCurrentApp(webapp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("1234", invocation.getId());
		Cache.getFilePath(NON_CACHED_FILE_ID, callback);
		invocation.sendInvocationCallback();

		assertTrue("Test Cache.getFilePath non-cached file found in cache", webapp.getFlag(FLAG_FILE_NOT_FOUND));
	}

	@Test
	public void testGetFilePathFileFound() {
		MockWebViewApp webapp = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				if(params.length == 1 && params[0] instanceof String) {
					String returnedName = (String)params[0];
					String expectedName = SdkProperties.getCacheDirectory() + "/" + SdkProperties.getCacheFilePrefix() + CACHED_FILE_ID;

					if(expectedName.equals(returnedName)) {
						setFlag("fileUrlMatches");
					}
				}
			}
		};

		WebViewApp.setCurrentApp(webapp);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("1234", invocation.getId());
		Cache.getFilePath(CACHED_FILE_ID, callback);
		invocation.sendInvocationCallback();

		assertEquals("Test Cache.getFilePath cached file not found", webapp.getFlag("fileUrlMatches"), true);
	}

	@Test
	public void testDownload() throws InterruptedException {
		final ConditionVariable cacheCv = new ConditionVariable();


		MockWebViewApp webapp = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				if(params.length == 0) {
					setFlag("callbackReceived");
				}
			}

			@Override
			public void eventCache(CacheEvent eventId, Object... params) {
				logCacheEvent("testDownload", eventId, params);
				switch(eventId) {
					case DOWNLOAD_STARTED:
						// params: url
						if(REMOTE_IMG.equals(params[0])) {
							setFlag("startEventReceived");
						}
						break;

					case DOWNLOAD_END:
						// params: url, bytes, total bytes, duration, response code, headers
						if(REMOTE_IMG.equals((String)params[0]) && (long)params[1] == REMOTE_IMG_SIZE && (long)params[2] == REMOTE_IMG_SIZE && (long)params[3] >= 0 && (int)params[4] == 200) {
							setFlag("endEventReceived");

							JSONArray headers = (JSONArray) params[5];
							for(int i = 0; i < headers.length(); i++) {
								try {
									JSONArray headerField = (JSONArray)headers.get(i);

									if("Content-Length".equals(headerField.getString(0)) && Integer.valueOf(headerField.getString(1)) == REMOTE_IMG_SIZE) {
										setFlag("endEventHeadersReceived");
									}
								} catch(JSONException e) {
									// do nothing, continue to next field
								}
							}
						}

						cacheCv.open();
						break;

					// This test should never receive other cache events, treat them as errors
					default:
						setFlag("unexpectedEventReceived");
						cacheCv.open();
						break;
				}
			}
		};
		WebViewApp.setCurrentApp(webapp);

		CacheThread.setProgressInterval(0);
		CacheThread.setConnectTimeout(30000);
		CacheThread.setReadTimeout(30000);

		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("1234", invocation.getId());
		Cache.download(REMOTE_IMG, REMOTE_IMG_FILE_ID, new JSONArray(), false, callback);
		invocation.sendInvocationCallback();

		boolean success = cacheCv.block(30000);
		assertTrue("Condition Variable was not opened", success);
		assertTrue("Test Cache.download image download callback ok not received", webapp.getFlag("callbackReceived"));
		assertTrue("Test Cache.download image download start event not received", webapp.getFlag("startEventReceived"));
		assertTrue("Test Cache.download image download end event not received", webapp.getFlag("endEventReceived"));
		assertTrue("Test Cache.download image download end event headers not received", webapp.getFlag("endEventHeadersReceived"));
		assertFalse("Test Cache.download image download unexpected events received", webapp.getFlag("unexpectedEventReceived"));

		MockWebViewApp webapp2 = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				try {
					JSONObject result = (JSONObject) params[0];

					if(result.has("id")) {
						setFlag("idFound");
					}

					if(result.has("found") && result.getBoolean("found") == true) {
						setFlag("fileFound");
					}

					if(result.has("size") && result.getLong("size") == REMOTE_IMG_SIZE) {
						setFlag("fileSizeMatches");
					}

					if(result.has("mtime") && result.getLong("mtime") > 0 && result.getLong("mtime") < System.currentTimeMillis()) {
						setFlag("fileTimestampOk");
					}
				} catch(JSONException e) {
					setFlag(FLAG_JSON_EXCEPTION_RECEIVED);
				}
			}
		};

		WebViewApp.setCurrentApp(webapp2);
		Invocation invocation2 = new Invocation();
		WebViewCallback callback2 = new WebViewCallback("1234", invocation2.getId());
		Cache.getFileInfo(REMOTE_IMG_FILE_ID, callback2);
		invocation2.sendInvocationCallback();

		assertTrue("Test Cache.download getFileInfo file not found", webapp2.getFlag("fileFound"));
		assertTrue("Test Cache.download getFileInfo file size does not match", webapp2.getFlag("fileSizeMatches"));
		assertTrue("Test Cache.download getFileInfo file timestamp not ok", webapp2.getFlag("fileTimestampOk"));
		assertFalse("Test Cache.download JSON exception received", webapp2.getFlag(FLAG_JSON_EXCEPTION_RECEIVED));

		MockWebViewApp webapp3 = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				if(params.length == 0) {
					setFlag("deleteOk");
				}
			}
		};
		WebViewApp.setCurrentApp(webapp3);

		Invocation invocation3 = new Invocation();
		WebViewCallback callback3 = new WebViewCallback("1234", invocation3.getId());
		Cache.deleteFile(REMOTE_IMG_FILE_ID, callback3);
		invocation3.sendInvocationCallback();

		assertTrue("Test Cache.download delete not successful", webapp3.getFlag("deleteOk"));

		MockWebViewApp webapp4 = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				try {
					JSONObject result = (JSONObject) params[0];

					if(result.has("found") && result.getBoolean("found") == false) {
						setFlag(FLAG_FILE_NOT_FOUND);
					}
				} catch(JSONException e) {
					setFlag(FLAG_JSON_EXCEPTION_RECEIVED);
					DeviceLog.exception("JSON exception", e);
				}
			}
		};

		WebViewApp.setCurrentApp(webapp4);
		Invocation invocation4 = new Invocation();
		WebViewCallback callback4 = new WebViewCallback("1234", invocation4.getId());
		Cache.getFileInfo(REMOTE_IMG_FILE_ID, callback4);
		invocation4.sendInvocationCallback();

		assertTrue("Test Cache.download file still found after delete", webapp4.getFlag(FLAG_FILE_NOT_FOUND));
		assertFalse("Test Cache.download JSON exception after delete", webapp4.getFlag(FLAG_JSON_EXCEPTION_RECEIVED));
	}

	@Test
	public void testTimeoutSettings() {
		MockWebViewApp webapp = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				if(params.length == 0) {
					setFlag("callbackReceived");
				}
			}
		};
		WebViewApp.setCurrentApp(webapp);

		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("1234", invocation.getId());
		Cache.setTimeouts(1234, 5678, callback);
		invocation.sendInvocationCallback();

		assertTrue("Test Cache.setTimeouts callback ok not received", webapp.getFlag("callbackReceived"));

		MockWebViewApp webapp2 = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				if(params.length == 2) {
					setFlag("callbackReceived");

					if(((Integer) params[0]).intValue() == 1234 && ((Integer) params[1]).intValue() == 5678) {
						setFlag("valuesMatch");
					}
				}
			}
		};

		WebViewApp.setCurrentApp(webapp2);

		Invocation invocation2 = new Invocation();
		WebViewCallback callback2 = new WebViewCallback("1234", invocation2.getId());
		Cache.getTimeouts(callback2);
		invocation2.sendInvocationCallback();

		assertTrue("Test Cache.getTimeouts callback ok not received", webapp2.getFlag("callbackReceived"));
		assertTrue("Test Cache.getTimeouts values do not match values given to Cache.setTimeouts", webapp2.getFlag("valuesMatch"));
	}

	private static long freeSpace;
	private static long totalSpace;

	@Test
	public void testFileSystemProperties() {
		freeSpace = 0;
		totalSpace = 0;

		MockWebViewApp webapp = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				if(params.length == 1) {
					setFlag("callbackReceived");
					freeSpace = (long) params[0];
				}
			}
		};
		WebViewApp.setCurrentApp(webapp);

		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("1234", invocation.getId());
		Cache.getFreeSpace(callback);
		invocation.sendInvocationCallback();

		assertTrue("Test Cache.getFreeSpace callback not ok", webapp.getFlag("callbackReceived"));
		assertTrue("Test Cache.getFreeSpace is not greater than zero", freeSpace > 0);

		MockWebViewApp webapp2 = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				if(params.length == 1) {
					setFlag("callbackReceived");
					totalSpace = (long) params[0];
				}
			}
		};
		WebViewApp.setCurrentApp(webapp2);

		Invocation invocation2 = new Invocation();
		WebViewCallback callback2 = new WebViewCallback("1234", invocation2.getId());
		Cache.getTotalSpace(callback2);
		invocation2.sendInvocationCallback();

		assertTrue("Test Cache.getTotalSpace callback not ok", webapp2.getFlag("callbackReceived"));
		assertTrue("Test Cache.getTotalSpace not greater than zero", totalSpace > 0);
		assertTrue("Test Cache.getTotalSpace not greater than Cache.getFreeSpace", totalSpace > freeSpace);
	}

	private static long downloadPosition;

	@Test
	public void testStopResumeDownload() throws Exception {
		final long minDownloadBytes = 12345;
		final ConditionVariable cacheCv = new ConditionVariable();
		final ConditionVariable cacheCv2 = new ConditionVariable();
		downloadPosition = 0;

		final MockWebViewApp webapp = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				if(params.length == 0 && !getFlag("cancelInvoked")) {
					setFlag("callbackDownloadReceived");
				} else {
					setFlag("callbackCancelReceived");
				}
			}

			@Override
			public void eventCache(CacheEvent eventId, Object... params) {
				logCacheEvent("testStopResumeDownload first part", eventId, params);
				switch(eventId) {
					case DOWNLOAD_STARTED:
						// params: url, position, total, responseCode, responseHeaders
						DeviceLog.debug("REMOTE_VIDEO_SIZE: " + (Long)params[2] + ", " + REMOTE_VIDEO_SIZE);
						DeviceLog.debug("REMOTE_VIDEO: " + REMOTE_VIDEO + ", " + params[0]);
						DeviceLog.debug("RESPONSE_CODE: " + (Integer)params[3]);
						DeviceLog.debug("POSITION: " + (Long)params[1]);
						if(REMOTE_VIDEO.equals(params[0]) && (Long)params[1] == 0 && (Long)params[2] == REMOTE_VIDEO_SIZE && (Integer)params[3] == 200) {
							setFlag("startEventReceived");
						}
						break;

					case DOWNLOAD_PROGRESS:
						if(REMOTE_VIDEO.equals(params[0]) && (Long)params[1] > 1) {
							setFlag("cancelInvoked");
							Invocation invocation = new Invocation();
							WebViewCallback callback = new WebViewCallback("1234", invocation.getId());
							Cache.stop(callback);
							invocation.sendInvocationCallback();
						}
						break;

					case DOWNLOAD_END:
						setFlag("endEventReceived");
						cacheCv.open();
						break;

					case DOWNLOAD_STOPPED:
						// params: url, total bytes
						if(REMOTE_VIDEO.equals(params[0]) && (Long)params[1] > 1) {
							setFlag("stopEventReceived");
							downloadPosition = (long)params[1];
							cacheCv.open();
						}
						break;

					// This test should never receive other cache events, treat them as errors
					default:
						setFlag("unexpectedEventReceived");
						cacheCv.open();
						break;
				}
			}
		};
		WebViewApp.setCurrentApp(webapp);

		CacheThread.setConnectTimeout(30000);
		CacheThread.setReadTimeout(30000);

		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("1234", invocation.getId());
		CacheThread.setProgressInterval(2);
		Cache.download(REMOTE_VIDEO, REMOTE_VIDEO_FILE_ID, new JSONArray(), false, callback);
		invocation.sendInvocationCallback();

		boolean success = cacheCv.block(30000);
		assertTrue("Cache stop download test: condition variable was not opened", success);
		assertTrue("Cache stop download test: callback download ok not received", webapp.getFlag("callbackDownloadReceived"));
		assertTrue("Cache stop download test: start event not received", webapp.getFlag("startEventReceived"));
		assertTrue("Cache stop download test: callback cancel ok not received", webapp.getFlag("callbackCancelReceived"));
		assertFalse("Cache stop download test: end event received instead of stop event", webapp.getFlag("endEventReceived"));
		assertTrue("Cache stop download test: stop event not received", webapp.getFlag("stopEventReceived"));
		assertFalse("Cache stop download test: unexpected events received", webapp.getFlag("unexpectedEventReceived"));

		CacheThread.setProgressInterval(0);

		final MockWebViewApp webapp2 = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				if(params.length == 0) {
					setFlag("callbackDownloadReceived");
				}
			}

			@Override
			public void eventCache(CacheEvent eventId, Object... params) {
				logCacheEvent("testStopResumeDownload second part", eventId, params);
				long remainingBytes = REMOTE_VIDEO_SIZE - downloadPosition;

				switch(eventId) {
					case DOWNLOAD_STARTED:
						// params: url, pos
						// ition, total, responseCode, responseHeaders

						DeviceLog.debug("REMAINING_VIDEO_SIZE: " + (Long)params[2] + ", " + REMOTE_VIDEO_SIZE);
						DeviceLog.debug("REMOTE_VIDEO: " + REMOTE_VIDEO + ", " + params[0]);
						DeviceLog.debug("RESPONSE_CODE: " + (Integer)params[3]);
						DeviceLog.debug("POSITION: " + (Long)params[1]);

						if(REMOTE_VIDEO.equals(params[0]) && downloadPosition == (Long)params[1] && REMOTE_VIDEO_SIZE == (Long)params[2] && (Integer)params[3] == 206) {
							setFlag("startEventReceived");
						}
						break;

					case DOWNLOAD_END:
						// params: url, byteCount, totalBytes, duration, responseCode, responseHeaders
						if(REMOTE_VIDEO.equals(params[0]) && remainingBytes == (Long)params[1] && remainingBytes == (Long)params[2] && (Long)params[3] > 0 && (Integer)params[4] == 206) {
							setFlag("endEventReceived");
							cacheCv2.open();
						}
						break;

					// This test should never receive other cache events, treat them as errors
					default:
						setFlag("unexpectedEventReceived");
						cacheCv2.open();
						break;
				}
			}
		};
		WebViewApp.setCurrentApp(webapp2);

		Invocation invocation2 = new Invocation();
		WebViewCallback callback2 = new WebViewCallback("1234", invocation2.getId());
		Cache.download(REMOTE_VIDEO, REMOTE_VIDEO_FILE_ID, new JSONArray("[[\"Range\", \"bytes=" + downloadPosition + "-\"]]"), true, callback2);
		invocation2.sendInvocationCallback();

		boolean success2 = cacheCv2.block(30000);
		assertTrue("Cache resume download test: condition variable was not opened", success2);
		assertTrue("Cache resume download test: callback download ok not received", webapp2.getFlag("callbackDownloadReceived"));
		assertTrue("Cache resume download test: start event not received", webapp2.getFlag("startEventReceived"));
		assertTrue("Cache resume download test: end event not received", webapp2.getFlag("endEventReceived"));
		assertFalse("Cache resume download test: unexpected event received", webapp2.getFlag("unexpectedEventReceived"));

		MockWebViewApp webapp3 = new MockWebViewApp() {
			@Override
			public void callbackOk(Object... params) {
				try {
					JSONObject result = (JSONObject) params[0];

					if(result.has("id") && REMOTE_VIDEO_FILE_ID.equals(result.getString("id"))) {
						setFlag("idFound");
					}

					if(result.has("found") && result.getBoolean("found") == true) {
						setFlag("fileFound");
					}

					if(result.has("size") && result.getLong("size") == REMOTE_VIDEO_SIZE) {
						setFlag("fileSizeMatches");
					}

					if(result.has("mtime") && result.getLong("mtime") > 0 && result.getLong("mtime") < System.currentTimeMillis()) {
						setFlag("fileTimestampOk");
					}
				} catch(JSONException e) {
					setFlag(FLAG_JSON_EXCEPTION_RECEIVED);
				}
			}
		};
		WebViewApp.setCurrentApp(webapp3);

		Invocation invocation3 = new Invocation();
		WebViewCallback callback3 = new WebViewCallback("1234", invocation3.getId());
		Cache.getFileInfo(REMOTE_VIDEO_FILE_ID, callback3);
		invocation3.sendInvocationCallback();

		assertTrue("Cache resume download test getFileInfo: file not found", webapp3.getFlag("fileFound"));
		assertTrue("Cache resume download test getFileInfo: file size does not match", webapp3.getFlag("fileSizeMatches"));
		assertTrue("Cache resume download test getFileInfo: file timestamp not ok", webapp3.getFlag("fileTimestampOk"));
		assertFalse("Cache resume download test getFileInfo: JSON exception received", webapp3.getFlag(FLAG_JSON_EXCEPTION_RECEIVED));
	}

	public class MockWebViewApp extends WebViewApp {
		HashSet<String> results;

		@Override
		public boolean invokeCallback(Invocation invocation) {
			for(ArrayList<Object> response : invocation.getResponses()) {
				Enum error = (Enum)response.get(1);
				Object[] params = (Object[])response.get(2);
				if(params.length == 0) {
					throw new RuntimeException("Callback id not in params");
				}

				ArrayList<Object> parsedParams = new ArrayList<>();
				parsedParams.addAll(Arrays.asList(params));
				parsedParams.remove(0);

				CallbackStatus status = (CallbackStatus)response.get(0);
				if(status == CallbackStatus.OK) {
					callbackOk(parsedParams.toArray());
					break;
				} else if(status == CallbackStatus.ERROR) {
					callbackError(error, parsedParams.toArray());
					break;
				} else {
					throw new IllegalArgumentException("Callback status not OK or ERROR");
				}
			}

			return true;
		}

		public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
			switch ((WebViewEventCategory)eventCategory) {
				case ADUNIT:
					eventAdUnit((AdUnitEvent)eventId, params);
					return true;

				case VIDEOPLAYER:
					eventVideoPlayer((VideoPlayerEvent)eventId, params);
					return true;

				case REQUEST:
					eventUrl((WebRequestEvent)eventId, params);
					return true;

				case CACHE:
					eventCache((CacheEvent)eventId, params);
					return true;

				default:
					throw new IllegalArgumentException("Unknown event category");
			}
		}

		public void callbackOk(Object... params) {
			throw new UnsupportedOperationException("Unhandled callbackOk");
		}

		public void callbackError(Enum error, Object... params) {
			throw new UnsupportedOperationException("Unhandled callbackError");
		}

		public void eventAdUnit(AdUnitEvent eventId, Object... params) {
			throw new UnsupportedOperationException("Unhandled eventAdUnit");
		}

		public void eventVideoPlayer(VideoPlayerEvent eventId, Object... params) {
			throw new UnsupportedOperationException("Unhandled eventVideoPlayer");
		}

		public void eventUrl(WebRequestEvent eventId, Object... params) {
			throw new UnsupportedOperationException("Unhandled eventUrl");
		}

		public void eventCache(CacheEvent eventId, Object... params) {
			throw new UnsupportedOperationException("Unhandled eventCache");
		}

		public void setFlag(String key) {
			if(results == null) results = new HashSet<>();

			results.add(key);
		}

		public boolean getFlag(String key) {
			if(results == null) return false;

			return results.contains(key);
		}
	}

	private void logCacheEvent(String testName, CacheEvent eventId, Object... params) {
		switch(eventId) {
			case DOWNLOAD_STARTED:
				DeviceLog.debug(testName + ": cache event: DOWNLOAD_STARTED " + (String)params[0] + " " + (Long)params[1] + " " + (Long)params[2] + " " + (Integer)params[3]);
				break;

			case DOWNLOAD_PROGRESS:
				DeviceLog.debug(testName + ": cache event: DOWNLOAD_PROGRESS " + (String)params[0] + " " + (Long)params[1] + " " + (Long)params[2]);
				break;

			case DOWNLOAD_END:
				DeviceLog.debug(testName + ": cache event: DOWNLOAD_END " + (String)params[0] + " " + (Long)params[1] + " " + (Long)params[2] + " " + (Long)params[3] + " " + (Integer)params[4]);
				break;

			case DOWNLOAD_STOPPED:
				DeviceLog.debug(testName + ": cache event: DOWNLOAD_STOPPED " + (String)params[0] + " " + (Long)params[1] + " " + (Long)params[2] + " " + (Long)params[3] + " " + (Integer)params[4]);
				break;

			case DOWNLOAD_ERROR:
				DeviceLog.debug(testName + ": cache event: DOWNLOAD_ERROR " + (CacheError)params[0] + " " + (String)params[1]);
				break;
		}
	}
}