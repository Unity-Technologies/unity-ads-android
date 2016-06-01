package com.unity3d.ads.test.unit;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.RequiresDevice;
import android.support.test.filters.SdkSuppress;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.view.ViewGroup;

import com.unity3d.ads.adunit.AdUnitActivity;
import com.unity3d.ads.api.VideoPlayer;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.ads.video.VideoPlayerError;
import com.unity3d.ads.video.VideoPlayerEvent;
import com.unity3d.ads.video.VideoPlayerView;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;
import com.unity3d.ads.webview.bridge.CallbackStatus;
import com.unity3d.ads.webview.bridge.Invocation;
import com.unity3d.ads.webview.bridge.WebViewCallback;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class VideoViewTest extends ActivityInstrumentationTestCase2<AdUnitActivity> {
	private class MyCustomRule<A extends AdUnitActivity> extends ActivityTestRule<A> {
		public MyCustomRule(Class<A> activityClass, boolean initialTouchMode, boolean launchActivity) {
			super(activityClass, initialTouchMode, launchActivity);
		}

		@Override
		protected void afterActivityFinished() {
			super.afterActivityFinished();
		}
	}

	public VideoViewTest() {
		super(AdUnitActivity.class);
	}

	@Rule
	public MyCustomRule<AdUnitActivity> testRule = new MyCustomRule<>(AdUnitActivity.class, false, false);

	@Before
	public void setUp() throws Exception {
		super.setUp();
		injectInstrumentation(InstrumentationRegistry.getInstrumentation());
	}

	@After
	public void tearDown() throws Exception {
		super.tearDown();
	}

	private class MockWebViewApp extends WebViewApp {
		public CallbackStatus CALLBACK_STATUS = null;
		public Enum CALLBACK_ERROR = null;
		public Object[] CALLBACK_PARAMS = null;
		public VideoPlayerView VIDEOPLAYER_VIEW = null;
		public Enum EVENT_CATEGORY = null;
		public Enum EVENT_ID = null;
		public List<Integer> INFO_EVENTS = null;
		public boolean EVENT_TRIGGERED = false;
		public Object[] EVENT_PARAMS = null;
		public ConditionVariable CONDITION_VARIABLE = null;

		@Override
		public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
			EVENT_CATEGORY = eventCategory;
			EVENT_ID = eventId;
			EVENT_PARAMS = params;

			return true;
		}

		@Override
		public boolean invokeCallback(Invocation invocation) {
			for (ArrayList<Object> response : invocation.getResponses()) {
				CallbackStatus status = (CallbackStatus) response.get(0);
				Enum error = (Enum) response.get(1);
				Object[] params = (Object[]) response.get(2);

				ArrayList<Object> paramList = new ArrayList<>();
				paramList.addAll(Arrays.asList(params));
				paramList.add(1, status.name());

				if (error != null) {
					paramList.add(2, error.name());
				}

				CALLBACK_ERROR = error;
				CALLBACK_PARAMS = params;
				CALLBACK_STATUS = status;

				break;
			}

			return true;
		}

		@Override
		public boolean invokeMethod(String className, String methodName, Method callback, Object... params) {
			return true;
		}
	}

	@Test
	@RequiresDevice
	public void testConstruct () throws Exception {
		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				assertNotNull("VideoPlayerView should not be null after constructing", vp);
				cv.open();
			}
		});

		boolean success = cv.block(30000);
		assertTrue("Condition variable was not opened properly: VideoPlayer was not created", success);
	}

	@Test
	@RequiresDevice
	public void testPrepare () throws Exception {
		final Activity activity = waitForActivityStart();
		assertNotNull("Started activity should not be null!", activity);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				DeviceLog.debug("Event: " + eventCategory.name() + ", " + eventId.name());

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					if (CONDITION_VARIABLE != null)
						CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		final Handler handler = new Handler(Looper.getMainLooper());

		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				DeviceLog.debug("URL_GIVEN: " + validUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(validUrl, 1f);
					}
				});
			}
		});
		MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		boolean success = cv.block(30000);

		assertTrue("Condition Variable was not opened: PREPARE or PREPARE ERROR event was not received", success);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Ignore
	@Test
	@RequiresDevice
	public void testPrepareNonExistingUrl () throws Exception {
		final Activity activity = waitForActivityStart();

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					if (CONDITION_VARIABLE != null)
						CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		final String invalidUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer-invalid.mp4";
		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				DeviceLog.debug("URL_GIVEN: " + invalidUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(invalidUrl, 1f);
					}
				});
			}
		});

		boolean success = cv.block(30000);

		assertTrue("Condition Variable was not opened: PREPARE or PREPARE ERROR event was not received", success);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	public void testPrepareAndPlay () throws Exception {
		final Activity activity = waitForActivityStart();

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.COMPLETED)) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;

		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				mockWebViewApp.VIDEOPLAYER_VIEW = vp;
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				DeviceLog.debug("URL_GIVEN: " + validUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(validUrl, 1f);
					}
				});
			}
		});

		boolean success = cv.block(30000);

		assertTrue("Condition Variable was not opened: COMPLETED or PREPARE ERROR event was not received", success);
		assertEquals("Event category should be videoplayer category", WebViewEventCategory.VIDEOPLAYER.name(), mockWebViewApp.EVENT_CATEGORY.name());
		assertEquals("Event ID should be completed", VideoPlayerEvent.COMPLETED, mockWebViewApp.EVENT_ID);
		assertEquals("The video url and the url received from the completed event should be the same", validUrl, mockWebViewApp.EVENT_PARAMS[0]);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	public void testPrepareAndPlayNonExistingUrl () throws Exception {
		final Activity activity = waitForActivityStart();

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.GENERIC_ERROR)) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final String invalidUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer-invalid.mp4";
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;

		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				mockWebViewApp.VIDEOPLAYER_VIEW = vp;
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				DeviceLog.debug("URL_GIVEN: " + invalidUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(invalidUrl, 1f);
					}
				});
			}
		});

		boolean success = cv.block(30000);

		assertTrue("Condition Variable was not opened: VIDEOPLAYER GENERIC ERROR or PREPARE ERROR was not received", success);
		assertEquals("Event category should be videoplayer category", WebViewEventCategory.VIDEOPLAYER.name(), mockWebViewApp.EVENT_CATEGORY.name());
		assertEquals("Event ID should be generic error", VideoPlayerEvent.GENERIC_ERROR, mockWebViewApp.EVENT_ID);
		assertEquals("The video url and the url received from the completed event should be the same", invalidUrl, mockWebViewApp.EVENT_PARAMS[2]);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}


	@Test
	@RequiresDevice
	public void testPreparePlaySeekToPause () throws Exception {
		final Activity activity = waitForActivityStart();

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (!EVENT_TRIGGERED && EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PROGRESS)) {
					EVENT_TRIGGERED = true;
					Handler handler = new Handler(Looper.getMainLooper());
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							VIDEOPLAYER_VIEW.seekTo(4080);
							VIDEOPLAYER_VIEW.pause();
							CONDITION_VARIABLE.open();
						}
					}, 2000);
				}

				return true;
			}
		});

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;

		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				mockWebViewApp.VIDEOPLAYER_VIEW = vp;
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				DeviceLog.debug("URL_GIVEN: " + validUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(validUrl, 1f);
					}
				});
			}
		});

		boolean success = cv.block(30000);

		assertTrue("Condition Variable was not opened: PROGRESS or PREPARE ERROR event was not received", success);
		assertTrue("Current position should be over 300ms", mockWebViewApp.VIDEOPLAYER_VIEW.getCurrentPosition() > 300);
		int diff = Math.abs(4080 - mockWebViewApp.VIDEOPLAYER_VIEW.getCurrentPosition());
		DeviceLog.debug("Difference: " + diff);
		assertTrue("Difference between expected position and actual position should be less than 300ms", diff < 300);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	public void testPreparePlayStop () throws Exception {
		final Activity activity = waitForActivityStart();

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
					Handler handler = new Handler(Looper.getMainLooper());
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							VIDEOPLAYER_VIEW.stop();
							CONDITION_VARIABLE.open();
						}
					}, 3000);
				}

				return true;
			}
		});

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;

		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				mockWebViewApp.VIDEOPLAYER_VIEW = vp;
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				DeviceLog.debug("URL_GIVEN: " + validUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(validUrl, 1f);
					}
				});
			}
		});

		boolean success = cv.block(30000);

		assertTrue("Condition Variable was not opened: PREPARED or PREPARE ERROR event was not received", success);
		assertFalse("Videoplayer shoudn't be in isPlaying state", mockWebViewApp.VIDEOPLAYER_VIEW.isPlaying());
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	public void testPreparePlaySetVolumePause () throws Exception {
		final Activity activity = waitForActivityStart();

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (!EVENT_TRIGGERED && EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PROGRESS)) {
					EVENT_TRIGGERED = true;
					Handler handler = new Handler(Looper.getMainLooper());
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							VIDEOPLAYER_VIEW.setVolume(0.666f);
							VIDEOPLAYER_VIEW.pause();
							CONDITION_VARIABLE.open();
						}
					}, 3000);
				}

				return true;
			}
		});

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;

		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				mockWebViewApp.VIDEOPLAYER_VIEW = vp;
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				DeviceLog.debug("URL_GIVEN: " + validUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(validUrl, 1f);
					}
				});
			}
		});

		boolean success = cv.block(30000);

		assertTrue("Condition Variable was not opened: PREPARED or PREPARE ERROR event was not received", success);
		assertFalse("Videoplayer shouldn't be in isPlaying state", mockWebViewApp.VIDEOPLAYER_VIEW.isPlaying());
		assertEquals("getVolume should return the same value as what was set as volume", 0.666f, mockWebViewApp.VIDEOPLAYER_VIEW.getVolume());
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	private ArrayList<Long> EVENT_POSITIONS = new ArrayList<>();

	@Test
	@RequiresDevice
	public void testSetProgressInterval () throws Exception {
		final Activity activity = waitForActivityStart();

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (eventId.equals(VideoPlayerEvent.PROGRESS)) {
					EVENT_POSITIONS.add(System.currentTimeMillis());
				}

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				else if (!EVENT_TRIGGERED && EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PROGRESS)) {
					EVENT_TRIGGERED = true;
					Handler handler = new Handler(Looper.getMainLooper());
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							VIDEOPLAYER_VIEW.pause();
							CONDITION_VARIABLE.open();
						}
					}, 3000);
				}

				return true;
			}
		});

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;

		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				mockWebViewApp.VIDEOPLAYER_VIEW = vp;
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				vp.setProgressEventInterval(300);
				DeviceLog.debug("URL_GIVEN: " + validUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(validUrl, 1f);
					}
				});
			}
		});

		boolean success = cv.block(30000);
		assertTrue("Condition Variable was not opened: VIDEO PROGRESS or PREPARE ERROR event was not received", success);

		for (int idx = 0; idx < EVENT_POSITIONS.size(); idx++) {
			if (idx + 1 < EVENT_POSITIONS.size()) {
				long interval = Math.abs(300 - (EVENT_POSITIONS.get(idx + 1) - EVENT_POSITIONS.get(idx)));
				DeviceLog.debug("Interval is: " + interval);
				assertFalse("Interval of the events weren't as accurate as expected (threshold of 70ms, was: " + interval + ")", interval > 70);
			}
		}

		assertFalse("Videoplayer shouldn't be in isPlaying state", mockWebViewApp.VIDEOPLAYER_VIEW.isPlaying());
		assertEquals("getProgressInterval should return the same value as what was set", 300, mockWebViewApp.VIDEOPLAYER_VIEW.getProgressEventInterval());
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}


	@Test
	@RequiresDevice
	public void testPreparePlayPause () throws Exception {
		final Activity activity = waitForActivityStart();

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				} else if (!EVENT_TRIGGERED && EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PROGRESS)) {
					EVENT_TRIGGERED = true;
					Handler handler = new Handler(Looper.getMainLooper());
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							VIDEOPLAYER_VIEW.pause();
							CONDITION_VARIABLE.open();
						}
					}, 3000);
				}

				return true;
			}
		});

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;

		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				mockWebViewApp.VIDEOPLAYER_VIEW = vp;
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				DeviceLog.debug("URL_GIVEN: " + validUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(validUrl, 1f);
					}
				});
			}
		});

		boolean success = cv.block(30000);

		assertTrue("Condition Variable was not opened: VIDEO PROGRESS or PREPARE ERROR event was not received", success);
		assertTrue("Current position should be over 300ms", mockWebViewApp.VIDEOPLAYER_VIEW.getCurrentPosition() > 300);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	@SdkSuppress(minSdkVersion = 17)
	public void testInfoListener () throws Exception {
		final Activity activity = waitForActivityStart();

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.INFO)) {
					INFO_EVENTS = new ArrayList<>();
					INFO_EVENTS.add((Integer) params[0]);
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.COMPLETED)) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;

		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				mockWebViewApp.VIDEOPLAYER_VIEW = vp;
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				DeviceLog.debug("URL_GIVEN: " + validUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(validUrl, 1f);
					}
				});
			}
		});

		boolean success = cv.block(30000);

		assertTrue("Condition Variable was not opened: COMPLETED or PREPARE ERROR event was not received", success);
		assertEquals("Event category should be videoplayer category", WebViewEventCategory.VIDEOPLAYER.name(), mockWebViewApp.EVENT_CATEGORY.name());
		assertEquals("Event ID should be completed", VideoPlayerEvent.COMPLETED, mockWebViewApp.EVENT_ID);
		assertEquals("The video url and the url received from the completed event should be the same", validUrl, mockWebViewApp.EVENT_PARAMS[0]);
		assertNotNull("Info events should not be NULL", mockWebViewApp.INFO_EVENTS);
		assertTrue("There should be at least one INFO event received", mockWebViewApp.INFO_EVENTS.size() > 0);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	@SdkSuppress(minSdkVersion = 17)
	public void testDisableInfoListener () throws Exception {
		final Activity activity = waitForActivityStart();

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.INFO)) {
					INFO_EVENTS = new ArrayList<>();
					INFO_EVENTS.add((Integer) params[0]);
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.COMPLETED)) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;

		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				vp.setInfoListenerEnabled(false);
				mockWebViewApp.VIDEOPLAYER_VIEW = vp;
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				DeviceLog.debug("URL_GIVEN: " + validUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(validUrl, 1f);
					}
				});
			}
		});

		boolean success = cv.block(30000);

		assertTrue("Condition Variable was not opened: COMPLETED or PREPARE ERROR event was not received", success);
		assertEquals("Event category should be videoplayer category", WebViewEventCategory.VIDEOPLAYER.name(), mockWebViewApp.EVENT_CATEGORY.name());
		assertEquals("Event ID should be completed", VideoPlayerEvent.COMPLETED, mockWebViewApp.EVENT_ID);
		assertEquals("The video url and the url received from the completed event should be the same", validUrl, mockWebViewApp.EVENT_PARAMS[0]);
		assertNull("No info-events shoul've been received", mockWebViewApp.INFO_EVENTS);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	@SdkSuppress(minSdkVersion = 17)
	public void testEnableInfoListener () throws Exception {
		final Activity activity = waitForActivityStart();

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (EVENT_CATEGORY.equals(WebViewEventCategory.VIDEOPLAYER) && EVENT_ID.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.INFO)) {
					INFO_EVENTS = new ArrayList<>();
					INFO_EVENTS.add((Integer) params[0]);
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.COMPLETED)) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;

		final Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				final VideoPlayerView vp = new VideoPlayerView(getInstrumentation().getTargetContext());
				vp.setInfoListenerEnabled(false);
				vp.setInfoListenerEnabled(true);
				mockWebViewApp.VIDEOPLAYER_VIEW = vp;
				activity.addContentView(vp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				DeviceLog.debug("URL_GIVEN: " + validUrl);
				handler.post(new Runnable() {
					@Override
					public void run() {
						vp.prepare(validUrl, 1f);
					}
				});
			}
		});

		boolean success = cv.block(30000);

		assertTrue("Condition Variable was not opened: COMPLETED or PREPARE ERROR event was not received", success);
		assertEquals("Event category should be videoplayer category", WebViewEventCategory.VIDEOPLAYER.name(), mockWebViewApp.EVENT_CATEGORY.name());
		assertEquals("Event ID should be completed", VideoPlayerEvent.COMPLETED, mockWebViewApp.EVENT_ID);
		assertEquals("The video url and the url received from the completed event should be the same", validUrl, mockWebViewApp.EVENT_PARAMS[0]);
		assertNotNull("Info events should not be NULL", mockWebViewApp.INFO_EVENTS);
		assertTrue("There should be at least one INFO event received", mockWebViewApp.INFO_EVENTS.size() > 0);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	public void testInfoListenerTooLowApiLevel () throws Exception {
		if (Build.VERSION.SDK_INT < 17) {
			WebViewApp.setCurrentApp(new MockWebViewApp() {
				@Override
				public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
					super.sendEvent(eventCategory, eventId, params);
					return true;
				}

				@Override
				public boolean invokeCallback(Invocation invocation) {

					for (ArrayList<Object> response : invocation.getResponses()) {
						CallbackStatus status = (CallbackStatus) response.get(0);
						Enum error = (Enum) response.get(1);

						if (status.equals(CallbackStatus.ERROR) && error.equals(VideoPlayerError.API_LEVEL_ERROR)) {
							CONDITION_VARIABLE.open();
						}

						break;
					}

					return true;
				}
			});

			final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();

			ConditionVariable cv = new ConditionVariable();
			mockWebViewApp.CONDITION_VARIABLE = cv;

			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				@Override
				public void run() {
					Invocation i = new Invocation();
					VideoPlayer.setInfoListenerEnabled(true, new WebViewCallback("test", i.getId()));
					i.sendInvocationCallback();
				}
			});

			boolean success = cv.block(30000);
			assertTrue("Condition Variable was not opened: ERROR event was not received", success);
		}
		else {
			DeviceLog.debug("Skipping test \"testInfoListenerTooLowApiLevel\", API level too high: " + Build.VERSION.SDK_INT);
		}
	}

	private boolean waitForActivityFinish (final Activity activity) {
		final ConditionVariable cv = new ConditionVariable();
		new Thread(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new MockWebViewApp() {
					private boolean allowEvents = true;
					@Override
					public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
						if (eventId.name().startsWith("ON_") && allowEvents) {
							DeviceLog.debug(eventId.name());

							if ("ON_DESTROY".equals(eventId.name())) {
								allowEvents = false;
								cv.open();
							}
						}

						return true;
					}
				});

				activity.finish();
			}
		}).start();
		return cv.block(30000);
	}

	private Activity waitForActivityStart () {
		final ConditionVariable cv = new ConditionVariable();
		WebViewApp.setCurrentApp(new MockWebViewApp() {
			private boolean allowEvents = true;
			private boolean launched = false;

			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				if (eventId.name().startsWith("ON_") && allowEvents) {
					DeviceLog.debug(eventId.name());

					if ("LAUNCHED".equals(eventId.name())) {
						launched = true;
					}

					if ("ON_RESUME".equals(eventId.name())) {
						allowEvents = false;

						if (launched) {
							DeviceLog.debug("Activity launch already came through, opening CV");
							cv.open();
						}
					}
				}

				return true;
			}
		});


		new Thread(new Runnable() {
			@Override
			public void run() {
				testRule.launchActivity(new Intent());
				WebViewApp.getCurrentApp().sendEvent(ExtraEvents.LAUNCHED, ExtraEvents.LAUNCHED);
				cv.open();
			}
		}).start();

		boolean success = cv.block(30000);
		return testRule.getActivity();
	}

	private enum ExtraEvents { LAUNCHED }
}