package com.unity3d.ads.test.unit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.ConditionVariable;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.lifecycle.LifecycleEvent;
import com.unity3d.ads.lifecycle.LifecycleListener;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.webview.WebViewApp;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import static org.junit.Assert.*;

@TargetApi(14)
@RunWith(AndroidJUnit4.class)
public class LifecycleListenerTest {
	private static ArrayList<Enum> EVENTS = new ArrayList<>();
	private static ArrayList<Object[]> EVENT_PARAMS = new ArrayList<>();
	private static int EVENT_COUNT = 0;

	@Rule
	public final ActivityTestRule<LifecycleListenerTestBaseActivity> _baseActivityRule = new ActivityTestRule<>(LifecycleListenerTestBaseActivity.class);
	public final ActivityTestRule<LifecycleListenerTestActivity> _testActivityRule = new ActivityTestRule<>(LifecycleListenerTestActivity.class);

	@Before
	public void setup () {
		ClientProperties.setActivity(_baseActivityRule.getActivity());
	}

	@After
	public void teardown () {
		ClientProperties.setActivity(null);

		EVENTS = new ArrayList<>();
		EVENT_PARAMS = new ArrayList<>();
		EVENT_COUNT = 0;
	}

	@Test
	public void testLifecycleEvents () {
		ArrayList<String> eventList = new ArrayList<>();
		eventList.add("onActivityCreated");
		eventList.add("onActivityStarted");
		eventList.add("onActivityResumed");
		eventList.add("onActivityPaused");
		eventList.add("onActivityStopped");
		eventList.add("onActivityDestroyed");

		LifecycleListener listener = new LifecycleListener(eventList);
		ClientProperties.getActivity().getApplication().registerActivityLifecycleCallbacks(listener);
		Activity activity = waitForActivityStart(_testActivityRule);
		DeviceLog.debug(activity.getClass().getName());
		waitForActivityFinish(activity);
		ClientProperties.getActivity().getApplication().unregisterActivityLifecycleCallbacks(listener);

		assertEquals("Event count not what was expected", 6, EVENT_COUNT);

		assertEquals("Expected first event from the AdUnitActivity to be CREATED: " + printEvents(EVENTS), LifecycleEvent.CREATED, EVENTS.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be STARTED: " + printEvents(EVENTS), LifecycleEvent.STARTED, EVENTS.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be RESUMED: " + printEvents(EVENTS), LifecycleEvent.RESUMED, EVENTS.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be PAUSED: " + printEvents(EVENTS), LifecycleEvent.PAUSED, EVENTS.get(EVENTS.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be STOPPED: " + printEvents(EVENTS), LifecycleEvent.STOPPED, EVENTS.get(EVENTS.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be DESTROYED: " + printEvents(EVENTS), LifecycleEvent.DESTROYED, EVENTS.get(EVENTS.size() - 1));

		for (Object[] params : EVENT_PARAMS) {
			assertEquals("The event first parameter not what was expected", "com.unity3d.ads.test.unit.LifecycleListenerTestActivity", params[0]);
		}

	}

	protected boolean waitForActivityFinish (final Activity activity) {
		final ConditionVariable cv = new ConditionVariable();
		new Thread(new Runnable() {
			@Override
			public void run() {
				WebViewApp.setCurrentApp(new WebViewApp() {
					private boolean allowEvents = true;
					@Override
					public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
						if (allowEvents && params[0].equals("com.unity3d.ads.test.unit.LifecycleListenerTestActivity")) {
							EVENTS.add(eventId);
							EVENT_PARAMS.add(params);
							EVENT_COUNT++;

							DeviceLog.debug(eventId.name() + " " + params[0]);

							if ("DESTROYED".equals(eventId.name())) {
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

	protected Activity waitForActivityStart (final ActivityTestRule rule) {
		final ConditionVariable cv = new ConditionVariable();
		WebViewApp.setCurrentApp(new WebViewApp() {
			private boolean allowEvents = false;

			@Override
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				if ("CREATED".equals(eventId.name())) {
					allowEvents = true;
				}

				if (allowEvents && params[0].equals("com.unity3d.ads.test.unit.LifecycleListenerTestActivity")) {
					DeviceLog.debug(eventId.name() + " " + params[0]);

					EVENTS.add(eventId);
					EVENT_PARAMS.add(params);
					EVENT_COUNT++;
				}

				return true;
			}
		});

		new Thread(new Runnable() {
			@Override
			public void run() {
				rule.launchActivity(new Intent());
				cv.open();
			}
		}).start();

		boolean success = cv.block(30000);
		return rule.getActivity();
	}

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
