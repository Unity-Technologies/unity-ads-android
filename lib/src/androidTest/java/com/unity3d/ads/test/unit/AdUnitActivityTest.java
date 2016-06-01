package com.unity3d.ads.test.unit;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import com.unity3d.ads.adunit.AdUnitActivity;
import com.unity3d.ads.adunit.AdUnitEvent;
import com.unity3d.ads.api.AdUnit;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.webview.WebView;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;
import com.unity3d.ads.webview.bridge.CallbackStatus;
import com.unity3d.ads.webview.bridge.Invocation;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.WindowManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

@RunWith(AndroidJUnit4.class)
public class AdUnitActivityTest extends ActivityInstrumentationTestCase2<AdUnitActivity> {

	private static final String VIDEO_PLAYER_VIEW = "videoplayer";
	private static final String WEB_VIEW = "webview";

	private static ConditionVariable _conditionVariable;

	@Rule
	public MyCustomRule<AdUnitActivity> testRule = new MyCustomRule<>(AdUnitActivity.class, false, false);

	private class MyCustomRule<A extends AdUnitActivity> extends ActivityTestRule<A> {
		public MyCustomRule(Class<A> activityClass, boolean initialTouchMode, boolean launchActivity) {
			super(activityClass, initialTouchMode, launchActivity);
		}

		@Override
		protected void afterActivityFinished() {
			super.afterActivityFinished();
			ConditionVariable cv = new ConditionVariable();
			cv.block(1000);
			WebViewApp.setCurrentApp(null);
		}
	}

	public AdUnitActivityTest() {
		super(AdUnitActivity.class);
	}

	@Before
	public void beforeTest () {
		injectInstrumentation(InstrumentationRegistry.getInstrumentation());
	}

	@Test
	public void testIntentSetWithViewsNoOrientation () throws TimeoutException {
		WebViewApp.setCurrentApp(new MockWebViewApp());

		_conditionVariable = new ConditionVariable();

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebView(new WebView(InstrumentationRegistry.getTargetContext()));
				_conditionVariable.open();
			}
		});

		boolean success;
		success = _conditionVariable.block(10000);
		if (!success) {
			throw new TimeoutException("ConditionVariable timeout");
		}

		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		Intent intent = new Intent();
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);
		testRule.launchActivity(intent);

		handler.post(new Runnable() {
			@Override
			public void run() {
				assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
				assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);
				testRule.getActivity().finish();
			}
		});

		_conditionVariable = new ConditionVariable();
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		MockWebViewApp webView = (MockWebViewApp)WebViewApp.getCurrentApp();
		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.COUNTED_EVENTS >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENT_CATEGORIES.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENTS.size() >= 6);

		for (int idx = 0; idx < webView.EVENT_CATEGORIES.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", webView.EVENT_CATEGORIES.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_CREATE, webView.EVENTS.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(webView.EVENTS), AdUnitEvent.ON_START, webView.EVENTS.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(webView.EVENTS), AdUnitEvent.ON_RESUME, webView.EVENTS.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_PAUSE, webView.EVENTS.get(webView.EVENTS.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(webView.EVENTS), AdUnitEvent.ON_STOP, webView.EVENTS.get(webView.EVENTS.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(webView.EVENTS), AdUnitEvent.ON_DESTROY, webView.EVENTS.get(webView.EVENTS.size() - 1));
	}

	@Test
	public void testIntentSetWithViewsNoOrientationFinishWithCallback () throws TimeoutException {
		WebViewApp.setCurrentApp(new MockWebViewApp());

		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		Handler handler = new Handler(Looper.getMainLooper());

		_conditionVariable = new ConditionVariable();

		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebView(new WebView(InstrumentationRegistry.getTargetContext()));
				_conditionVariable.open();
			}
		});

		boolean success;
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		Intent intent = new Intent();
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);
		testRule.launchActivity(intent);

		handler.post(new Runnable() {
			@Override
			public void run() {
				assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
				assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);

				testRule.getActivity().finish();
			}
		});

		_conditionVariable = new ConditionVariable();
		success = _conditionVariable.block(10000);
		if (!success) throw new TimeoutException("ConditionVariable timeout");

		MockWebViewApp webView = (MockWebViewApp)WebViewApp.getCurrentApp();
		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.COUNTED_EVENTS >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENT_CATEGORIES.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENTS.size() >= 6);

		for (int idx = 0; idx < webView.EVENT_CATEGORIES.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", webView.EVENT_CATEGORIES.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_CREATE, webView.EVENTS.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(webView.EVENTS), AdUnitEvent.ON_START, webView.EVENTS.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(webView.EVENTS), AdUnitEvent.ON_RESUME, webView.EVENTS.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_PAUSE, webView.EVENTS.get(webView.EVENTS.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(webView.EVENTS), AdUnitEvent.ON_STOP, webView.EVENTS.get(webView.EVENTS.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(webView.EVENTS), AdUnitEvent.ON_DESTROY, webView.EVENTS.get(webView.EVENTS.size() - 1));
	}

	@Test
	public void testIntentSetWithViewsAndOrientation () {
		WebViewApp.setCurrentApp(new MockWebViewApp());
		_conditionVariable = new ConditionVariable();

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebView(new WebView(InstrumentationRegistry.getTargetContext()));
				_conditionVariable.open();
			}
		});

		boolean success;
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		Intent intent = new Intent();
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);
		intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, 6);
		testRule.launchActivity(intent);

		handler.post(new Runnable() {
			@Override
			public void run() {
				assertEquals("View list first value not same than expected: ", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
				assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);
				assertEquals("Current orientation was not the same that was given in intent", 6, AdUnit.getAdUnitActivity().getRequestedOrientation());

				testRule.getActivity().finish();
			}
		});

		_conditionVariable = new ConditionVariable();
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		MockWebViewApp webView = (MockWebViewApp)WebViewApp.getCurrentApp();
		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.COUNTED_EVENTS >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENT_CATEGORIES.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENTS.size() >= 6);

		for (int idx = 0; idx < webView.EVENT_CATEGORIES.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", webView.EVENT_CATEGORIES.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_CREATE, webView.EVENTS.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(webView.EVENTS), AdUnitEvent.ON_START, webView.EVENTS.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(webView.EVENTS), AdUnitEvent.ON_RESUME, webView.EVENTS.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_PAUSE, webView.EVENTS.get(webView.EVENTS.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(webView.EVENTS), AdUnitEvent.ON_STOP, webView.EVENTS.get(webView.EVENTS.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(webView.EVENTS), AdUnitEvent.ON_DESTROY, webView.EVENTS.get(webView.EVENTS.size() - 1));
	}

	@Test
	public void testIntentSetWithViewsSwapViews () {
		WebViewApp.setCurrentApp(new MockWebViewApp());

		_conditionVariable = new ConditionVariable();

		Intent intent = new Intent();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebView(new WebView(InstrumentationRegistry.getTargetContext()));
				_conditionVariable.open();
			}
		});

		boolean success;
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		final String[] swapViews = new String[]{WEB_VIEW, VIDEO_PLAYER_VIEW};
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);
		intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, 6);
		testRule.launchActivity(intent);

		handler.post(new Runnable() {
			@Override
			public void run() {
				assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
				assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);

				((AdUnitActivity) testRule.getActivity()).setViews(swapViews);

				assertEquals("View list first value not same than expected", swapViews[0], AdUnit.getAdUnitActivity().getViews()[0]);
				assertEquals("View list second value not same than expected", swapViews[1], AdUnit.getAdUnitActivity().getViews()[1]);
				assertEquals("Current orientation was not the same that was given in intent", 6, AdUnit.getAdUnitActivity().getRequestedOrientation());

				((AdUnitActivity) testRule.getActivity()).finish();
			}
		});

		_conditionVariable = new ConditionVariable();
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		MockWebViewApp webView = (MockWebViewApp)WebViewApp.getCurrentApp();
		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.COUNTED_EVENTS >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENT_CATEGORIES.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENTS.size() >= 6);

		for (int idx = 0; idx < webView.EVENT_CATEGORIES.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", webView.EVENT_CATEGORIES.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_CREATE, webView.EVENTS.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(webView.EVENTS), AdUnitEvent.ON_START, webView.EVENTS.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(webView.EVENTS), AdUnitEvent.ON_RESUME, webView.EVENTS.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_PAUSE, webView.EVENTS.get(webView.EVENTS.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(webView.EVENTS), AdUnitEvent.ON_STOP, webView.EVENTS.get(webView.EVENTS.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(webView.EVENTS), AdUnitEvent.ON_DESTROY, webView.EVENTS.get(webView.EVENTS.size() - 1));
	}

	@Test
	public void testIntentSetWithViewsRemoveViews () {
		WebViewApp.setCurrentApp(new MockWebViewApp());

		_conditionVariable = new ConditionVariable();
		Intent intent = new Intent();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebView(new WebView(InstrumentationRegistry.getTargetContext()));
				_conditionVariable.open();
			}
		});

		boolean success;
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		final String[] removeViewsWebView = new String[]{WEB_VIEW};
		final String[] removeViewsVideoPlayer = new String[]{VIDEO_PLAYER_VIEW};
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);
		intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, 6);
		testRule.launchActivity(intent);

		handler.post(new Runnable() {
			@Override
			public void run() {
				assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
				assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);

				((AdUnitActivity) testRule.getActivity()).setViews(removeViewsWebView);

				assertEquals("View list first value not same than expected", removeViewsWebView[0], AdUnit.getAdUnitActivity().getViews()[0]);

				((AdUnitActivity) testRule.getActivity()).setViews(views);

				assertEquals("View list first value not same than expected after setting them back", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
				assertEquals("View list second value not same than expected after setting them back", views[1], AdUnit.getAdUnitActivity().getViews()[1]);

				((AdUnitActivity) testRule.getActivity()).setViews(removeViewsVideoPlayer);

				assertEquals("View list first value not same than expected", removeViewsVideoPlayer[0], AdUnit.getAdUnitActivity().getViews()[0]);
				assertEquals("Current orientation was not the same that was given in intent", 6, AdUnit.getAdUnitActivity().getRequestedOrientation());

				((AdUnitActivity) testRule.getActivity()).finish();
			}
		});

		_conditionVariable = new ConditionVariable();
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		MockWebViewApp webView = (MockWebViewApp)WebViewApp.getCurrentApp();
		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.COUNTED_EVENTS >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENT_CATEGORIES.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENTS.size() >= 6);

		for (int idx = 0; idx < webView.EVENT_CATEGORIES.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", webView.EVENT_CATEGORIES.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_CREATE, webView.EVENTS.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(webView.EVENTS), AdUnitEvent.ON_START, webView.EVENTS.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(webView.EVENTS), AdUnitEvent.ON_RESUME, webView.EVENTS.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_PAUSE, webView.EVENTS.get(webView.EVENTS.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(webView.EVENTS), AdUnitEvent.ON_STOP, webView.EVENTS.get(webView.EVENTS.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(webView.EVENTS), AdUnitEvent.ON_DESTROY, webView.EVENTS.get(webView.EVENTS.size() - 1));
	}

	@Test
	public void testIntentSetWithViewsSetOrientation () {
		WebViewApp.setCurrentApp(new MockWebViewApp());

		_conditionVariable = new ConditionVariable();
		Intent intent = new Intent();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebView(new WebView(InstrumentationRegistry.getTargetContext()));
				_conditionVariable.open();
			}
		});

		boolean success;
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);

		intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, 6);
		testRule.launchActivity(intent);

		handler.post(new Runnable() {
			@Override
			public void run() {
				assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
				assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);
				assertEquals("Current orientation was not the same that was given in intent", 6, AdUnit.getAdUnitActivity().getRequestedOrientation());

				((AdUnitActivity) testRule.getActivity()).setOrientation(1);

				assertEquals("Requested orientation should be same as given", testRule.getActivity().getRequestedOrientation(), 1);

				((AdUnitActivity) testRule.getActivity()).finish();
			}
		});

		_conditionVariable = new ConditionVariable();
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		MockWebViewApp webView = (MockWebViewApp)WebViewApp.getCurrentApp();
		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.COUNTED_EVENTS >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENT_CATEGORIES.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENTS.size() >= 6);

		for (int idx = 0; idx < webView.EVENT_CATEGORIES.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", webView.EVENT_CATEGORIES.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_CREATE, webView.EVENTS.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(webView.EVENTS), AdUnitEvent.ON_START, webView.EVENTS.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(webView.EVENTS), AdUnitEvent.ON_RESUME, webView.EVENTS.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_PAUSE, webView.EVENTS.get(webView.EVENTS.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(webView.EVENTS), AdUnitEvent.ON_STOP, webView.EVENTS.get(webView.EVENTS.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(webView.EVENTS), AdUnitEvent.ON_DESTROY, webView.EVENTS.get(webView.EVENTS.size() - 1));
	}

	@Test
	public void testIntentSetWithViewsOnKeyDown () {
		WebViewApp.setCurrentApp(new MockWebViewApp());

		_conditionVariable = new ConditionVariable();
		Intent intent = new Intent();
		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				WebViewApp.getCurrentApp().setWebView(new WebView(InstrumentationRegistry.getTargetContext()));
				_conditionVariable.open();
			}
		});

		boolean success;
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		final String[] views = new String[]{VIDEO_PLAYER_VIEW, WEB_VIEW};
		intent.putExtra(AdUnitActivity.EXTRA_VIEWS, views);
		intent.putExtra(AdUnitActivity.EXTRA_ORIENTATION, 6);
		intent.putExtra(AdUnitActivity.EXTRA_KEY_EVENT_LIST, new ArrayList<>(Arrays.asList(new Integer[]{6})));
		testRule.launchActivity(intent);

		handler.post(new Runnable() {
			@Override
			public void run() {
				assertEquals("View list first value not same than expected", views[0], AdUnit.getAdUnitActivity().getViews()[0]);
				assertEquals("View list second value not same than expected", views[1], AdUnit.getAdUnitActivity().getViews()[1]);
				assertEquals("Current orientation was not the same that was given in intent", 6, AdUnit.getAdUnitActivity().getRequestedOrientation());

				testRule.getActivity().onKeyDown(6, new KeyEvent(2, 1, 3, 4, 5));

				assertEquals("Expected keyEvent to come in category of ADUNIT", WebViewEventCategory.ADUNIT, ((MockWebViewApp) WebViewApp.getCurrentApp()).ONEOFF_CATEGORY);
				assertEquals("Expected eventId to be KEY_DOWN", AdUnitEvent.KEY_DOWN, ((MockWebViewApp) WebViewApp.getCurrentApp()).ONEOFF_EVENTID);
				assertEquals("Expected first given parameter to be the triggered keyCode (6)", 6, ((MockWebViewApp) WebViewApp.getCurrentApp()).ONEOFF_PARAMS[0]);
				assertEquals("Expected second given parameter to be the event time (1)", (long) 1, ((MockWebViewApp) WebViewApp.getCurrentApp()).ONEOFF_PARAMS[1]);
				assertEquals("Expected third given parameter to be the event down time (2)", (long) 2, ((MockWebViewApp) WebViewApp.getCurrentApp()).ONEOFF_PARAMS[2]);
				assertEquals("Expected third given parameter to be the event repeat count (5)", 5, ((MockWebViewApp) WebViewApp.getCurrentApp()).ONEOFF_PARAMS[3]);

				assertFalse(testRule.getActivity().onKeyDown(5, new KeyEvent(2, 1, 3, 4, 5)));
				((AdUnitActivity) testRule.getActivity()).finish();
			}
		});

		_conditionVariable = new ConditionVariable();
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);

		MockWebViewApp webView = (MockWebViewApp)WebViewApp.getCurrentApp();
		assertTrue("Counted events should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.COUNTED_EVENTS >= 6);
		assertTrue("Counted event categories should be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENT_CATEGORIES.size() >= 6);
		assertTrue("Counted actual event types be at least 6. AdUnitActivity should have sent all 6 lifecycle events before finishing: " + printEvents(webView.EVENTS) + ", COUNT: " + webView.EVENTS.size(), webView.EVENTS.size() >= 6);

		for (int idx = 0; idx < webView.EVENT_CATEGORIES.size(); idx++) {
			assertEquals("Expected event categories to be WebViewEventCategory.ADUNIT", webView.EVENT_CATEGORIES.get(idx), WebViewEventCategory.ADUNIT);
		}

		assertEquals("Expected first event from the AdUnitActivity to be ON_CREATE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_CREATE, webView.EVENTS.get(0));
		assertEquals("Expected second event from the AdUnitActivity to be ON_START: " + printEvents(webView.EVENTS), AdUnitEvent.ON_START, webView.EVENTS.get(1));
		assertEquals("Expected third event from the AdUnitActivity to be ON_RESUME: " + printEvents(webView.EVENTS), AdUnitEvent.ON_RESUME, webView.EVENTS.get(2));
		assertEquals("Expected third last event from the AdUnitActivity to be ON_PAUSE: " + printEvents(webView.EVENTS), AdUnitEvent.ON_PAUSE, webView.EVENTS.get(webView.EVENTS.size() - 3));
		assertEquals("Expected second last event from the AdUnitActivity to be ON_STOP: " + printEvents(webView.EVENTS), AdUnitEvent.ON_STOP, webView.EVENTS.get(webView.EVENTS.size() - 2));
		assertEquals("Expected last event from the AdUnitActivity to be ON_DESTROY: " + printEvents(webView.EVENTS), AdUnitEvent.ON_DESTROY, webView.EVENTS.get(webView.EVENTS.size() - 1));
	}

	@Test
	public void testKeepScreenOn() {
		WebViewApp.setCurrentApp(new MockWebViewApp());
		Handler handler = new Handler(Looper.getMainLooper());
		Intent intent = new Intent();
		testRule.launchActivity(intent);

		_conditionVariable = new ConditionVariable();

		handler.post(new Runnable() {
			@Override
			public void run() {
				((AdUnitActivity)testRule.getActivity()).setKeepScreenOn(true);

				int flags = testRule.getActivity().getWindow().getAttributes().flags;
				assertTrue("Activity window has FLAG_KEEP_SCREEN_ON", (flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0);

				((AdUnitActivity)testRule.getActivity()).setKeepScreenOn(false);

				flags = testRule.getActivity().getWindow().getAttributes().flags;
				assertTrue("Activity window does not have FLAG_KEEP_SCREEN_ON", (flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) == 0);

				testRule.getActivity().finish();
			}
		});

		boolean success;
		success = _conditionVariable.block(10000);
		assertTrue("Condition Variable was not opened", success);
	}

	private class MockWebViewApp extends WebViewApp {
		public CallbackStatus CALLBACK_STATUS = null;
		public Enum CALLBACK_ERROR = null;
		public Object[] CALLBACK_PARAMS = null;
		public boolean CALLBACK_INVOKED = false;

		public ArrayList<WebViewEventCategory> EVENT_CATEGORIES = new ArrayList<>();
		public ArrayList<AdUnitEvent> EVENTS = new ArrayList<>();
		public int COUNTED_EVENTS = 0;

		public Enum ONEOFF_CATEGORY = null;
		public Enum ONEOFF_EVENTID = null;
		public Object[] ONEOFF_PARAMS = null;

		private boolean allowEvents = true;

		public MockWebViewApp () {
			super();
		}

		@Override
		public boolean invokeCallback(Invocation invocation) {
			if (invocation != null && invocation.getResponses() != null) {
				for (ArrayList<Object> response : invocation.getResponses()) {
					CALLBACK_INVOKED = true;

					CallbackStatus status = (CallbackStatus)response.get(0);
					CALLBACK_STATUS = status;

					Enum error = (Enum)response.get(1);
					CALLBACK_ERROR = error;

					Object[] params = (Object[])response.get(2);
					CALLBACK_PARAMS = params;
				}
			}

			return true;
		}

		@Override
		public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {

			if (eventId.name().startsWith("ON_") && allowEvents) {
				EVENT_CATEGORIES.add((WebViewEventCategory) eventCategory);
				EVENTS.add((AdUnitEvent)eventId);
				COUNTED_EVENTS++;

				DeviceLog.debug(eventId.name());

				if ("ON_DESTROY".equals(eventId.name())) {
					allowEvents = false;
					_conditionVariable.open();
				}
			}

			else {
				ONEOFF_CATEGORY = eventCategory;
				ONEOFF_EVENTID = eventId;
				ONEOFF_PARAMS = params;
			}

			return true;
		}
	}

	private String printEvents (ArrayList<AdUnitEvent> events) {
		String retString = "";

		if (events != null) {
			for (AdUnitEvent event : events) {
				retString += event.name() + ", ";
			}
		}

		retString = retString.substring(0, retString.length() - 1);
		return retString;
	}
}
