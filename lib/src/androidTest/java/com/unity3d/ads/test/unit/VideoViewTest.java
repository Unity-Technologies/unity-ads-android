package com.unity3d.ads.test.unit;

import android.app.Activity;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.support.test.filters.RequiresDevice;
import android.support.test.filters.SdkSuppress;
import android.support.test.runner.AndroidJUnit4;
import android.view.ViewGroup;

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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class VideoViewTest extends AdUnitActivityTestBaseClass {

	public VideoViewTest() {
		super();
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
		final Activity activity = waitForActivityStart(null);
		assertNotNull("Started activity should not be null!", activity);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				DeviceLog.debug("Event: " + eventCategory.name() + ", " + eventId.name());

				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
					if (CONDITION_VARIABLE != null)
						CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		final Handler handler = new Handler(Looper.getMainLooper());

		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable prepareCV = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = prepareCV;
		DeviceLog.debug("URL_GIVEN: " + validUrl);
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.prepare(validUrl, 1f, 0);
		success = prepareCV.block(30000);

		assertTrue("Condition Variable was not opened: PREPARE or PREPARE ERROR event was not received", success);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	public void testPrepareAndPlay () throws Exception {
		final Activity activity = waitForActivityStart(null);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			private boolean allowEvents = true;
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);
				if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
					return true;
				}
				if (allowEvents) {
					EVENT_CATEGORIES.add(eventCategory);
					EVENTS.add(eventId);
					EVENT_PARAMS = params;
					EVENT_COUNT++;

					DeviceLog.debug(eventId.name());

					if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
						VIDEOPLAYER_VIEW.play();
					}
					if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.COMPLETED)) {
						allowEvents = false;
						CONDITION_VARIABLE.open();
					}
				}

				return true;
			}
		});

		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		final Handler handler = new Handler(Looper.getMainLooper());

		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		DeviceLog.debug("URL_GIVEN: " + validUrl);
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.prepare(validUrl, 1f, 0);
		success = cv.block(30000);

		assertTrue("Condition Variable was not opened: COMPLETED or PREPARE ERROR event was not received", success);
		assertEquals("Event category should be videoplayer category", WebViewEventCategory.VIDEOPLAYER, mockWebViewApp.EVENT_CATEGORIES.get(0));
		assertEquals("Event ID should be completed", VideoPlayerEvent.COMPLETED, mockWebViewApp.EVENTS.get(mockWebViewApp.EVENTS.size() - 1));
		assertEquals("The video url and the url received from the completed event should be the same", validUrl, mockWebViewApp.EVENT_PARAMS[0]);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	public void testPrepareAndPlayNonExistingUrl () throws Exception {
		final Activity activity = waitForActivityStart(null);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			private boolean allowEvents = true;
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);
				if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
					return true;
				}
				if (allowEvents) {
					EVENT_CATEGORIES.add(eventCategory);
					EVENTS.add(eventId);
					EVENT_PARAMS = params;
					EVENT_COUNT++;

					DeviceLog.debug(eventId.name());

					if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
						VIDEOPLAYER_VIEW.play();
					}
					if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.GENERIC_ERROR)) {
						allowEvents = false;
						CONDITION_VARIABLE.open();
					}
				}

				return true;
			}
		});

		final String invalidUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer-invalid.mp4";
		final Handler handler = new Handler(Looper.getMainLooper());

		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		DeviceLog.debug("URL_GIVEN: " + invalidUrl);
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.prepare(invalidUrl, 1f, 0);
		success = cv.block(30000);

		assertTrue("Condition Variable was not opened: VIDEOPLAYER GENERIC ERROR or PREPARE ERROR was not received", success);
		assertEquals("Event category should be videoplayer category", WebViewEventCategory.VIDEOPLAYER, mockWebViewApp.EVENT_CATEGORIES.get(0));
		assertTrue("Events should contain GENERIC_ERROR", mockWebViewApp.EVENTS.contains(VideoPlayerEvent.GENERIC_ERROR));
		assertEquals("The video url and the url received from the completed event should be the same", invalidUrl, mockWebViewApp.EVENT_PARAMS[0]);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}


	@Test
	@RequiresDevice
	public void testPreparePlaySeekToPause () throws Exception {
		final Activity activity = waitForActivityStart(null);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			private boolean allowEvents = true;
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
					return true;
				}

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (!EVENT_TRIGGERED && eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PROGRESS)) {
					EVENT_TRIGGERED = true;
					VIDEOPLAYER_VIEW.seekTo(4080);
					VIDEOPLAYER_VIEW.pause();
					CONDITION_VARIABLE.open();
					allowEvents = false;
				}

				return true;
			}
		});

		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";
		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		DeviceLog.debug("URL_GIVEN: " + validUrl);
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.prepare(validUrl, 1f, 0);
		success = cv.block(30000);

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
		final Activity activity = waitForActivityStart(null);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
					return true;
				}

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (!EVENT_TRIGGERED && eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PROGRESS)) {
					if (EVENT_COUNT > 8) {
						EVENT_TRIGGERED = true;
						VIDEOPLAYER_VIEW.stop();
						CONDITION_VARIABLE.open();
					}
				}

				return true;
			}
		});

		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.prepare(validUrl, 1f, 0);
		success = cv.block(30000);

		assertTrue("Condition Variable was not opened: PREPARED or PREPARE ERROR event was not received", success);
		assertFalse("Videoplayer shoudn't be in isPlaying state", mockWebViewApp.VIDEOPLAYER_VIEW.isPlaying());
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	public void testPreparePlaySetVolumePause () throws Exception {
		final Activity activity = waitForActivityStart(null);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
					return true;
				}

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (!EVENT_TRIGGERED && eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PROGRESS)) {
					if (EVENT_COUNT > 8) {
						EVENT_TRIGGERED = true;
						VIDEOPLAYER_VIEW.setVolume(0.666f);
						VIDEOPLAYER_VIEW.pause();
						CONDITION_VARIABLE.open();
					}
				}

				return true;
			}
		});

		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.prepare(validUrl, 1f, 0);
		success = cv.block(30000);

		assertTrue("Condition Variable was not opened: PREPARED or PREPARE ERROR event was not received", success);
		assertFalse("Videoplayer shouldn't be in isPlaying state", mockWebViewApp.VIDEOPLAYER_VIEW.isPlaying());
		assertEquals("getVolume should return the same value as what was set as volume", 0.666f, mockWebViewApp.VIDEOPLAYER_VIEW.getVolume());
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	private ArrayList<Long> EVENT_POSITIONS = new ArrayList<>();

	@Test
	@RequiresDevice
	public void testSetProgressInterval () throws Exception {
		final Activity activity = waitForActivityStart(null);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
					return true;
				}

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				if (eventId.equals(VideoPlayerEvent.PROGRESS)) {
					EVENT_POSITIONS.add(System.currentTimeMillis());
				}

				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				else if (!EVENT_TRIGGERED && eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PROGRESS)) {
					if (EVENT_COUNT > 12) {
						EVENT_TRIGGERED = true;
						VIDEOPLAYER_VIEW.pause();
						CONDITION_VARIABLE.open();
					}
				}

				return true;
			}
		});

		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.setProgressEventInterval(300);
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.prepare(validUrl, 1f, 0);
		success = cv.block(30000);

		assertTrue("Condition Variable was not opened: VIDEO PROGRESS or PREPARE ERROR event was not received", success);

		int failedIntervals = 0;

		for (int idx = 0; idx < EVENT_POSITIONS.size(); idx++) {
			if (idx + 1 < EVENT_POSITIONS.size()) {
				long interval = Math.abs(300 - (EVENT_POSITIONS.get(idx + 1) - EVENT_POSITIONS.get(idx)));
				DeviceLog.debug("Interval is: " + interval);

				if (interval > 80) {
					failedIntervals++;
				}

				assertFalse("Too many intervals failed to arrive in 80ms threshold (" + failedIntervals + ")", failedIntervals > 3);
			}
		}

		assertFalse("Videoplayer shouldn't be in isPlaying state", mockWebViewApp.VIDEOPLAYER_VIEW.isPlaying());
		assertEquals("getProgressInterval should return the same value as what was set", 300, mockWebViewApp.VIDEOPLAYER_VIEW.getProgressEventInterval());
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}


	@Test
	@RequiresDevice
	public void testPreparePlayPause () throws Exception {
		final Activity activity = waitForActivityStart(null);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
					return true;
				}

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				else if (!EVENT_TRIGGERED && eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PROGRESS)) {
					if (EVENT_COUNT > 6) {
						EVENT_TRIGGERED = true;
						VIDEOPLAYER_VIEW.pause();
						CONDITION_VARIABLE.open();
					}
				}

				return true;
			}
		});

		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.prepare(validUrl, 1f, 0);
		success = cv.block(30000);

		assertTrue("Condition Variable was not opened: VIDEO PROGRESS or PREPARE ERROR event was not received", success);
		assertTrue("Current position should be over 300ms", mockWebViewApp.VIDEOPLAYER_VIEW.getCurrentPosition() > 300);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	@SdkSuppress(minSdkVersion = 17)
	public void testInfoListener () throws Exception {
		final Activity activity = waitForActivityStart(null);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
					return true;
				}

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.INFO)) {
					INFO_EVENTS = new ArrayList<>();
					INFO_EVENTS.add((Integer) params[1]);
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.COMPLETED)) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.prepare(validUrl, 1f, 0);
		success = cv.block(30000);

		assertTrue("Condition Variable was not opened: COMPLETED or PREPARE ERROR event was not received", success);
		assertEquals("Event category should be videoplayer category", WebViewEventCategory.VIDEOPLAYER, mockWebViewApp.EVENT_CATEGORIES.get(0));
		assertEquals("Event ID should be completed", VideoPlayerEvent.COMPLETED, mockWebViewApp.EVENTS.get(mockWebViewApp.EVENTS.size() - 1));
		assertEquals("The video url and the url received from the completed event should be the same", validUrl, mockWebViewApp.EVENT_PARAMS[0]);
		assertNotNull("Info events should not be NULL", mockWebViewApp.INFO_EVENTS);
		assertTrue("There should be at least one INFO event received", mockWebViewApp.INFO_EVENTS.size() > 0);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	@SdkSuppress(minSdkVersion = 17)
	public void testDisableInfoListener () throws Exception {
		final Activity activity = waitForActivityStart(null);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
					return true;
				}

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.INFO)) {
					INFO_EVENTS = new ArrayList<>();
					INFO_EVENTS.add((Integer) params[1]);
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.COMPLETED)) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.setInfoListenerEnabled(true);
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.setInfoListenerEnabled(false);
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.prepare(validUrl, 1f, 0);
		success = cv.block(30000);

		assertTrue("Condition Variable was not opened: COMPLETED or PREPARE ERROR event was not received", success);
		assertEquals("Event category should be videoplayer category", WebViewEventCategory.VIDEOPLAYER, mockWebViewApp.EVENT_CATEGORIES.get(0));
		assertEquals("Event ID should be completed", VideoPlayerEvent.COMPLETED, mockWebViewApp.EVENTS.get(mockWebViewApp.EVENTS.size() - 1));
		assertEquals("The video url and the url received from the completed event should be the same", validUrl, mockWebViewApp.EVENT_PARAMS[0]);
		assertNull("No info-events shoul've been received", mockWebViewApp.INFO_EVENTS);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}

	@Test
	@RequiresDevice
	@SdkSuppress(minSdkVersion = 17)
	public void testEnableInfoListener () throws Exception {
		final Activity activity = waitForActivityStart(null);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
					return true;
				}

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
					VIDEOPLAYER_VIEW.play();
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.INFO)) {
					INFO_EVENTS = new ArrayList<>();
					INFO_EVENTS.add((Integer) params[1]);
				}
				if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.COMPLETED)) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final String validUrl = TestUtilities.getTestServerAddress() + "/blue_test_trailer.mp4";

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
				activity.addContentView(((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		ConditionVariable cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.setInfoListenerEnabled(false);
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.setInfoListenerEnabled(true);
		((MockWebViewApp)WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW.prepare(validUrl, 1f, 0);
		success = cv.block(30000);

		assertTrue("Condition Variable was not opened: COMPLETED or PREPARE ERROR event was not received", success);
		assertEquals("Event category should be videoplayer category", WebViewEventCategory.VIDEOPLAYER, mockWebViewApp.EVENT_CATEGORIES.get(0));
		assertEquals("Event ID should be completed", VideoPlayerEvent.COMPLETED, mockWebViewApp.EVENTS.get(mockWebViewApp.EVENTS.size() - 1));
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
}