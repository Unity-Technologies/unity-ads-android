package com.unity3d.ads.test.unit;

import android.app.Activity;
import android.content.Intent;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import com.unity3d.ads.adunit.AdUnitActivity;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.video.VideoPlayerView;
import com.unity3d.ads.webview.WebView;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.bridge.CallbackStatus;
import com.unity3d.ads.webview.bridge.Invocation;
import android.support.test.rule.ActivityTestRule;

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
public class AdUnitActivityTestBaseClass extends ActivityInstrumentationTestCase2<AdUnitActivity> {
	public AdUnitActivityTestBaseClass() {
		super(AdUnitActivity.class);
	}

	protected class MyCustomRule<A extends AdUnitActivity> extends ActivityTestRule<A> {
		public MyCustomRule(Class<A> activityClass, boolean initialTouchMode, boolean launchActivity) {
			super(activityClass, initialTouchMode, launchActivity);
		}

		@Override
		protected void afterActivityFinished() {
			super.afterActivityFinished();
		}
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

	protected class MockWebViewApp extends WebViewApp {
		public CallbackStatus CALLBACK_STATUS = null;
		public Enum CALLBACK_ERROR = null;
		public Object[] CALLBACK_PARAMS = null;
		public VideoPlayerView VIDEOPLAYER_VIEW = null;
		public List<Integer> INFO_EVENTS = null;
		public boolean EVENT_TRIGGERED = false;
		public Object[] EVENT_PARAMS = null;
		public int EVENT_COUNT = 0;
		public ConditionVariable CONDITION_VARIABLE = null;

		public ArrayList<Enum> EVENT_CATEGORIES = new ArrayList<>();
		public ArrayList<Enum> EVENTS = new ArrayList<>();

		@Override
		public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
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

	protected boolean waitForActivityFinish (final Activity activity) {
		final ConditionVariable cv = new ConditionVariable();
		new Thread(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new MockWebViewApp() {
					private boolean allowEvents = true;
					@Override
					public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
						if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
							return true;
						}
						if (allowEvents) {
							EVENT_CATEGORIES.add(eventCategory);
							EVENTS.add(eventId);
							EVENT_PARAMS = params;
							EVENT_COUNT++;

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

	protected Activity waitForActivityStart (final Intent intent) {
		final ConditionVariable cv = new ConditionVariable();
		WebViewApp.setCurrentApp(new MockWebViewApp() {
			private boolean allowEvents = true;
			private boolean launched = false;

			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				if ("ON_FOCUS_GAINED".equals(eventId.name()) || "ON_FOCUS_LOST".equals(eventId.name())) {
					return true;
				}
				if (allowEvents) {

					DeviceLog.debug(eventId.name());

					if ("LAUNCHED".equals(eventId.name())) {
						launched = true;
					}
					else {
						EVENT_CATEGORIES.add(eventCategory);
						EVENTS.add(eventId);
						EVENT_PARAMS = params;
						EVENT_COUNT++;
					}

					if ("ON_RESUME".equals(eventId.name())) {
						//allowEvents = false;

						if (launched) {
							DeviceLog.debug("Activity launch already came through, opening CV");
							cv.open();
						}
					}
				}

				return true;
			}
		});

		final ConditionVariable webViewCV = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebView(new WebView(InstrumentationRegistry.getTargetContext()));
				webViewCV.open();
			}
		});

		new Thread(new Runnable() {
			@Override
			public void run() {
				Intent tmpIntent = intent;
				if (tmpIntent == null) tmpIntent = new Intent();
				testRule.launchActivity(tmpIntent);
				WebViewApp.getCurrentApp().sendEvent(ExtraEvents.LAUNCHED, ExtraEvents.LAUNCHED);
				cv.open();
			}
		}).start();

		boolean success = cv.block(30000);
		return testRule.getActivity();
	}

	private enum ExtraEvents { LAUNCHED }

	protected String printEvents (ArrayList<Enum> events) {
		String retString = "";

		if (events != null) {
			for (Enum event : events) {
				retString += event.name() + ", ";
			}
		}

		retString = retString.substring(0, retString.length() - 1);
		return retString;
	}
}

