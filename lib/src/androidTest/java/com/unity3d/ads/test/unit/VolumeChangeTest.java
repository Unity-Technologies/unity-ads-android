package com.unity3d.ads.test.unit;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.support.test.filters.RequiresDevice;
import android.support.test.filters.SdkSuppress;
import android.view.ViewGroup;

import com.unity3d.ads.api.DeviceInfo;
import com.unity3d.ads.device.IVolumeChangeListener;
import com.unity3d.ads.device.VolumeChange;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.ads.video.VideoPlayerEvent;
import com.unity3d.ads.video.VideoPlayerView;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

import org.junit.Before;
import org.junit.Test;

public class VolumeChangeTest extends AdUnitActivityTestBaseClass {
	@Before
	public void setup() {
		WebViewApp.setCurrentApp(null);
	}

	@Test
	@RequiresDevice
	@SdkSuppress(minSdkVersion = 21)
	public void testVolumeChange() throws Exception {
		final Activity activity = waitForActivityStart(null);

		ClientProperties.setApplicationContext(getInstrumentation().getTargetContext());

		AudioManager am = (AudioManager)ClientProperties.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			private boolean allowEvents = true;
			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				if (allowEvents) {
					EVENT_CATEGORIES.add(eventCategory);
					EVENTS.add(eventId);
					EVENT_COUNT++;

					DeviceLog.debug(eventId.name());

					if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PREPARED)) {
						VIDEOPLAYER_VIEW.play();
						CONDITION_VARIABLE.open();
					}
					if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.PROGRESS)) {
						if (EVENT_COUNT < 5) {
							CONDITION_VARIABLE.open();
						}
					}
					if (eventCategory.equals(WebViewEventCategory.DEVICEINFO) && eventId.equals(DeviceInfo.DeviceInfoEvent.VOLUME_CHANGED)) {
						EVENT_PARAMS = params;
						CONDITION_VARIABLE.open();
					}
					if (eventCategory.equals(WebViewEventCategory.VIDEOPLAYER) && eventId.equals(VideoPlayerEvent.COMPLETED)) {
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
				((MockWebViewApp) WebViewApp.getCurrentApp()).VIDEOPLAYER_VIEW = new VideoPlayerView(getInstrumentation().getTargetContext());
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

		cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		success = cv.block(3000);

		IVolumeChangeListener vcl = new IVolumeChangeListener() {
			@Override
			public void onVolumeChanged(int volume) {
				DeviceLog.debug("VOLUME_CHANGED: " + volume);
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.DEVICEINFO, DeviceInfo.DeviceInfoEvent.VOLUME_CHANGED, volume);
			}

			@Override
			public int getStreamType() {
				return AudioManager.STREAM_MUSIC;
			}
		};

		VolumeChange.registerListener(vcl);

		am.setStreamVolume(AudioManager.STREAM_MUSIC, 1, 0);

		cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		success = cv.block(3000);

		assertNotNull("WebViewApp NULL", mockWebViewApp);
		assertNotNull("EVENT_PARAMS NULL (params never received?)", mockWebViewApp.EVENT_PARAMS);
		assertNotNull("First EVENT_PARAMS NULL", mockWebViewApp.EVENT_PARAMS[0]);
		assertEquals("Volume not what was expected", 1, (int)mockWebViewApp.EVENT_PARAMS[0]);

		VolumeChange.unregisterListener(vcl);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, 3, 0);

		VolumeChange.registerListener(vcl);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, 2, 0);

		cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		success = cv.block(3000);

		assertEquals("Volume not what was expected", 2, (int)mockWebViewApp.EVENT_PARAMS[0]);

		cv = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = cv;
		success = cv.block(30000);

		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}
}
