package com.unity3d.ads.test.unit;

import android.app.Activity;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.adunit.AdUnitActivity;
import com.unity3d.ads.adunit.AdUnitEvent;
import com.unity3d.ads.api.AdUnit;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdUnitActivityTest extends AdUnitActivityTestBaseClass {

	private static final String VIDEO_PLAYER_VIEW = "videoplayer";
	private static final String WEB_VIEW = "webview";

	@Test
	public void testIntentSetWithViews () throws TimeoutException {
		Intent intent = new Intent();
		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);

		ArrayList<Enum> allEvents = new ArrayList<>();
		ArrayList<Enum> allEventCategories = new ArrayList<>();
		int totalEventCount = 0;

		final Activity activity = waitForActivityStart(intent);
		MockWebViewApp webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		ConditionVariable cv = new ConditionVariable();
		cv.block(300);

		assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
		assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));

		webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);

		totalEventCount += webViewApp.EVENT_COUNT;

		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), totalEventCount >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEventCategories.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEvents.size() >= 6);

		for (int idx = 0; idx < allEventCategories.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", WebViewEventCategory.ADUNIT, allEventCategories.get(idx));
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(allEvents), AdUnitEvent.ON_CREATE, allEvents.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(allEvents), AdUnitEvent.ON_START, allEvents.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(allEvents), AdUnitEvent.ON_RESUME, allEvents.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(allEvents), AdUnitEvent.ON_PAUSE, allEvents.get(allEvents.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(allEvents), AdUnitEvent.ON_STOP, allEvents.get(allEvents.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(allEvents), AdUnitEvent.ON_DESTROY, allEvents.get(allEvents.size() - 1));
	}

	@Test
	public void testIntentSetWithViewsAndOrientation () {
		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		Intent intent = new Intent();
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);
		intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, 6);

		ArrayList<Enum> allEvents = new ArrayList<>();
		ArrayList<Enum> allEventCategories = new ArrayList<>();
		int totalEventCount = 0;

		final Activity activity = waitForActivityStart(intent);
		MockWebViewApp webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		ConditionVariable cv = new ConditionVariable();
		cv.block(300);

		assertEquals("View list first value not same than expected: ", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
		assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);
		assertEquals("Current orientation was not the same that was given in intent", 6, AdUnit.getAdUnitActivity().getRequestedOrientation());
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));

		webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), totalEventCount >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEventCategories.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEvents.size() >= 6);

		for (int idx = 0; idx < allEventCategories.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", allEventCategories.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(allEvents), AdUnitEvent.ON_CREATE, allEvents.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(allEvents), AdUnitEvent.ON_START, allEvents.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(allEvents), AdUnitEvent.ON_RESUME, allEvents.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(allEvents), AdUnitEvent.ON_PAUSE, allEvents.get(allEvents.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(allEvents), AdUnitEvent.ON_STOP, allEvents.get(allEvents.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(allEvents), AdUnitEvent.ON_DESTROY, allEvents.get(allEvents.size() - 1));
	}

	@Test
	public void testIntentSetWithViewsAndOrientationSwapViews () {
		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		final String[] swapViews = new String[]{WEB_VIEW, VIDEO_PLAYER_VIEW};
		Intent intent = new Intent();
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);
		intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, 6);

		ArrayList<Enum> allEvents = new ArrayList<>();
		ArrayList<Enum> allEventCategories = new ArrayList<>();
		int totalEventCount = 0;

		final Activity activity = waitForActivityStart(intent);
		MockWebViewApp webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		ConditionVariable cv = new ConditionVariable();
		cv.block(300);

		assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
		assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);

		final ConditionVariable cvSetViews = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				((AdUnitActivity)activity).setViews(swapViews);
				cvSetViews.open();
			}
		});
		boolean success = cvSetViews.block(2000);

		assertTrue("Weird ConditionVariable problem", success);
		assertEquals("View list first value not same than expected", swapViews[0], AdUnit.getAdUnitActivity().getViews()[0]);
		assertEquals("View list second value not same than expected", swapViews[1], AdUnit.getAdUnitActivity().getViews()[1]);
		assertEquals("Current orientation was not the same that was given in intent", 6, AdUnit.getAdUnitActivity().getRequestedOrientation());
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));

		webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), totalEventCount >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEventCategories.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEvents.size() >= 6);

		for (int idx = 0; idx < allEventCategories.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", allEventCategories.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(allEvents), AdUnitEvent.ON_CREATE, allEvents.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(allEvents), AdUnitEvent.ON_START, allEvents.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(allEvents), AdUnitEvent.ON_RESUME, allEvents.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(allEvents), AdUnitEvent.ON_PAUSE, allEvents.get(allEvents.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(allEvents), AdUnitEvent.ON_STOP, allEvents.get(allEvents.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(allEvents), AdUnitEvent.ON_DESTROY, allEvents.get(allEvents.size() - 1));
	}

	@Test
	public void testIntentSetWithViewsAndOrientationRemoveViews () {
		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		final String[] removeViewsWebView = new String[]{WEB_VIEW};
		final String[] removeViewsVideoPlayer = new String[]{VIDEO_PLAYER_VIEW};
		Intent intent = new Intent();
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);
		intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, 6);

		ArrayList<Enum> allEvents = new ArrayList<>();
		ArrayList<Enum> allEventCategories = new ArrayList<>();
		int totalEventCount = 0;

		final Activity activity = waitForActivityStart(intent);
		MockWebViewApp webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		ConditionVariable cv = new ConditionVariable();
		cv.block(300);

		assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
		assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);

		final ConditionVariable cvSetViews = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				((AdUnitActivity)activity).setViews(removeViewsWebView);

				assertEquals("View list first value not same than expected", removeViewsWebView[0], AdUnit.getAdUnitActivity().getViews()[0]);

				((AdUnitActivity)activity).setViews(views);

				assertEquals("View list first value not same than expected after setting them back", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
				assertEquals("View list second value not same than expected after setting them back", views[1], AdUnit.getAdUnitActivity().getViews()[1]);

				((AdUnitActivity)activity).setViews(removeViewsVideoPlayer);
				cvSetViews.open();
			}
		});
		boolean success = cvSetViews.block(2000);

		assertTrue("Weird ConditionVariable problem", success);
		assertEquals("View list first value not same than expected", removeViewsVideoPlayer[0], AdUnit.getAdUnitActivity().getViews()[0]);
		assertEquals("Current orientation was not the same that was given in intent", 6, AdUnit.getAdUnitActivity().getRequestedOrientation());
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));

		webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), totalEventCount >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEventCategories.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEvents.size() >= 6);

		for (int idx = 0; idx < allEventCategories.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", allEventCategories.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(allEvents), AdUnitEvent.ON_CREATE, allEvents.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(allEvents), AdUnitEvent.ON_START, allEvents.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(allEvents), AdUnitEvent.ON_RESUME, allEvents.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(allEvents), AdUnitEvent.ON_PAUSE, allEvents.get(allEvents.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(allEvents), AdUnitEvent.ON_STOP, allEvents.get(allEvents.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(allEvents), AdUnitEvent.ON_DESTROY, allEvents.get(allEvents.size() - 1));
	}

	@Test
	public void testIntentSetWithViewsSetOrientation () {
		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		Intent intent = new Intent();
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);
		intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, 6);

		ArrayList<Enum> allEvents = new ArrayList<>();
		ArrayList<Enum> allEventCategories = new ArrayList<>();
		int totalEventCount = 0;

		final Activity activity = waitForActivityStart(intent);
		MockWebViewApp webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		ConditionVariable cv = new ConditionVariable();
		cv.block(300);

		assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
		assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);
		assertEquals("Current orientation was not the same that was given in intent", 6, AdUnit.getAdUnitActivity().getRequestedOrientation());

		final ConditionVariable cvSetViews = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				((AdUnitActivity)activity).setOrientation(1);
				cvSetViews.open();
			}
		});
		boolean success = cvSetViews.block(2000);

		assertTrue("Weird ConditionVariable problem", success);
		assertEquals("Requested orientation should be same as given", activity.getRequestedOrientation(), 1);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));

		webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), totalEventCount >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEventCategories.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEvents.size() >= 6);

		for (int idx = 0; idx < allEventCategories.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", allEventCategories.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(allEvents), AdUnitEvent.ON_CREATE, allEvents.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(allEvents), AdUnitEvent.ON_START, allEvents.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(allEvents), AdUnitEvent.ON_RESUME, allEvents.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(allEvents), AdUnitEvent.ON_PAUSE, allEvents.get(allEvents.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(allEvents), AdUnitEvent.ON_STOP, allEvents.get(allEvents.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(allEvents), AdUnitEvent.ON_DESTROY, allEvents.get(allEvents.size() - 1));
	}

	@Test
	public void testIntentSetWithViewsOnKeyDown () {
		Intent intent = new Intent();
		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);
		intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, 6);
		intent.putExtra(AdUnitActivity.EXTRA_KEY_EVENT_LIST, new ArrayList<>(Arrays.asList(new Integer[]{6})));

		ArrayList<Enum> allEvents = new ArrayList<>();
		ArrayList<Enum> allEventCategories = new ArrayList<>();
		int totalEventCount = 0;

		final Activity activity = waitForActivityStart(intent);
		MockWebViewApp webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		ConditionVariable cv = new ConditionVariable();
		cv.block(300);

		assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
		assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);
		assertEquals("Current orientation was not the same that was given in intent", 6, AdUnit.getAdUnitActivity().getRequestedOrientation());

		final ConditionVariable cvSetViews = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				activity.onKeyDown(6, new KeyEvent(2, 1, 3, 4, 5));

				assertEquals("Expected keyEvent to come in category of ADUNIT", WebViewEventCategory.ADUNIT, ((MockWebViewApp) WebViewApp.getCurrentApp()).EVENT_CATEGORIES.get(3));
				assertTrue("Expected events to contain KEY_DOWN", ((MockWebViewApp) WebViewApp.getCurrentApp()).EVENTS.contains(AdUnitEvent.KEY_DOWN));
				assertEquals("Expected first given parameter to be the triggered keyCode (6)", 6, ((MockWebViewApp) WebViewApp.getCurrentApp()).EVENT_PARAMS[0]);
				assertEquals("Expected second given parameter to be the event time (1)", (long) 1, ((MockWebViewApp) WebViewApp.getCurrentApp()).EVENT_PARAMS[1]);
				assertEquals("Expected third given parameter to be the event down time (2)", (long) 2, ((MockWebViewApp) WebViewApp.getCurrentApp()).EVENT_PARAMS[2]);
				assertEquals("Expected third given parameter to be the event repeat count (5)", 5, ((MockWebViewApp) WebViewApp.getCurrentApp()).EVENT_PARAMS[3]);

				assertFalse(activity.onKeyDown(5, new KeyEvent(2, 1, 3, 4, 5)));
				cvSetViews.open();
			}
		});
		boolean success = cvSetViews.block(2000);

		assertTrue("Weird ConditionVariable problem", success);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));

		webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), totalEventCount >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEventCategories.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEvents.size() >= 6);

		for (int idx = 0; idx < allEventCategories.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", allEventCategories.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(allEvents), AdUnitEvent.ON_CREATE, allEvents.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(allEvents), AdUnitEvent.ON_START, allEvents.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(allEvents), AdUnitEvent.ON_RESUME, allEvents.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(allEvents), AdUnitEvent.ON_PAUSE, allEvents.get(allEvents.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(allEvents), AdUnitEvent.ON_STOP, allEvents.get(allEvents.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(allEvents), AdUnitEvent.ON_DESTROY, allEvents.get(allEvents.size() - 1));
	}

	@Test
	public void testKeepScreenOn() {
		Intent intent = new Intent();
		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);

		ArrayList<Enum> allEvents = new ArrayList<>();
		ArrayList<Enum> allEventCategories = new ArrayList<>();
		int totalEventCount = 0;

		final Activity activity = waitForActivityStart(intent);
		MockWebViewApp webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		ConditionVariable cv = new ConditionVariable();
		cv.block(300);

		assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
		assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);

		final ConditionVariable cvSetViews = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				((AdUnitActivity)activity).setKeepScreenOn(true);

				int flags = activity.getWindow().getAttributes().flags;
				assertTrue("Activity window has FLAG_KEEP_SCREEN_ON", (flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0);

				((AdUnitActivity)activity).setKeepScreenOn(false);

				flags = activity.getWindow().getAttributes().flags;
				assertTrue("Activity window does not have FLAG_KEEP_SCREEN_ON", (flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) == 0);
				cvSetViews.open();
			}
		});
		boolean success = cvSetViews.block(2000);

		assertTrue("Weird ConditionVariable problem", success);
		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));

		webViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		allEvents.addAll(webViewApp.EVENTS);
		allEventCategories.addAll(webViewApp.EVENT_CATEGORIES);
		totalEventCount += webViewApp.EVENT_COUNT;

		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), totalEventCount >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEventCategories.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(allEvents) + ", COUNT: " + allEvents.size(), allEvents.size() >= 6);

		for (int idx = 0; idx < allEventCategories.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", allEventCategories.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(allEvents), AdUnitEvent.ON_CREATE, allEvents.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(allEvents), AdUnitEvent.ON_START, allEvents.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(allEvents), AdUnitEvent.ON_RESUME, allEvents.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(allEvents), AdUnitEvent.ON_PAUSE, allEvents.get(allEvents.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(allEvents), AdUnitEvent.ON_STOP, allEvents.get(allEvents.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(allEvents), AdUnitEvent.ON_DESTROY, allEvents.get(allEvents.size() - 1));
	}

	@Test
	public void testSetViewFrame() {
		Intent intent = new Intent();
		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);

		final Activity activity = waitForActivityStart(intent);

		final ConditionVariable cv = new ConditionVariable();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				((AdUnitActivity)activity).setViewFrame("adunit", 50, 60, 510, 520);
				((AdUnitActivity)activity).setViewFrame("videoplayer", 110, 120, 130, 140);
				((AdUnitActivity)activity).setViewFrame("webview", 210, 220, 230, 240);
				cv.open();
			}
		});
		boolean success = cv.block(2000);

		ConditionVariable cv2 = new ConditionVariable();
		cv2.block(1000);

		assertTrue("Weird ConditionVariable problem", success);

		Map<String, Integer> adunitMap = ((AdUnitActivity)activity).getViewFrame("adunit");
		Map<String, Integer> videoPlayerMap = ((AdUnitActivity)activity).getViewFrame("videoplayer");
		Map<String, Integer> webViewMap = ((AdUnitActivity)activity).getViewFrame("webview");

		assertEquals("Unexpected AdUnit x", Integer.valueOf(50), adunitMap.get("x"));
		assertEquals("Unexpected AdUnit y", Integer.valueOf(60), adunitMap.get("y"));
		assertEquals("Unexpected AdUnit width", Integer.valueOf(510), adunitMap.get("width"));
		assertEquals("Unexpected AdUnit height", Integer.valueOf(520), adunitMap.get("height"));

		assertEquals("Unexpected VideoPlayer x",  Integer.valueOf(110), videoPlayerMap.get("x"));
		assertEquals("Unexpected VideoPlayer y", Integer.valueOf(120), videoPlayerMap.get("y"));
		assertEquals("Unexpected VideoPlayer width", Integer.valueOf(130), videoPlayerMap.get("width"));
		assertEquals("Unexpected VideoPlayer height", Integer.valueOf(140), videoPlayerMap.get("height"));

		assertEquals("Unexpected WebView x", Integer.valueOf(210), webViewMap.get("x"));
		assertEquals("Unexpected WebView y", Integer.valueOf(220), webViewMap.get("y"));
		assertEquals("Unexpected WebView width", Integer.valueOf(230), webViewMap.get("width"));
		assertEquals("Unexpected WebView height", Integer.valueOf(240), webViewMap.get("height"));

		assertTrue("Didn't get activity finish", waitForActivityFinish(activity));
	}
}
