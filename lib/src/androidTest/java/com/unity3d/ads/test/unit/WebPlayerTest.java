package com.unity3d.ads.test.unit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.ViewGroup;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.ads.webplayer.WebPlayer;
import com.unity3d.ads.webview.WebViewApp;

@RunWith(AndroidJUnit4.class)
public class WebPlayerTest extends AdUnitActivityTestBaseClass {

	private static WebPlayer _webPlayer;

	@BeforeClass
	public static void prepareTests () {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
	}

	@Before
	public void beforeEach () {
		_webPlayer = null;
	}

	@After
	public void afterEach () {
		_webPlayer = null;
	}

	@Test
	public void testNullSettings () throws Exception {
		final Activity activity = waitForActivityStart(null);
		assertNotNull("Started activity should not be null!", activity);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				DeviceLog.debug("GOT EVENT: " + eventId.name() + ": " + CONDITION_VARIABLE);

				if (eventId.name().equals("PAGE_FINISHED") && CONDITION_VARIABLE != null) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				_webPlayer = new WebPlayer(activity, null, null);
				activity.addContentView(_webPlayer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		assertNotNull("WebPlayer should not be null", _webPlayer);
		assertNull("There shouldn't be errored settings", _webPlayer.getErroredSettings());

		boolean didFinish = waitForActivityFinish(activity);
		assertTrue("Activity should have finished", didFinish);
	}

	@Test
	public void testSettings () throws Exception {
		final Activity activity = waitForActivityStart(null);
		assertNotNull("Started activity should not be null!", activity);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				DeviceLog.debug("GOT EVENT: " + eventId.name() + ": " + CONDITION_VARIABLE);

				if (eventId.name().equals("PAGE_FINISHED") && CONDITION_VARIABLE != null) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();

		final JSONObject webSettings = new JSONObject();
		webSettings.put("setSaveFormData", new JSONArray("[true]"));
		webSettings.put("setSavePassword", new JSONArray("[true]"));
		webSettings.put("setLayoutAlgorithm", new JSONArray("[{\"value\": \"NORMAL\", \"type\": \"Enum\", \"className\": \"android.webkit.WebSettings$LayoutAlgorithm\"}]"));

		final JSONObject webPlayerSettings = new JSONObject();
		webPlayerSettings.put("setHorizontalScrollBarEnabled", new JSONArray("[false]"));
		webPlayerSettings.put("setVerticalScrollBarEnabled", new JSONArray("[false]"));
		webPlayerSettings.put("setInitialScale", new JSONArray("[1]"));

		handler.post(new Runnable() {
			@Override
			public void run() {
				_webPlayer = new WebPlayer(activity, webSettings, webPlayerSettings);
				activity.addContentView(_webPlayer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		assertNotNull("WebPlayer should not be null", _webPlayer);
		assertNull("There shouldn't be errored settings", _webPlayer.getErroredSettings());

		boolean didFinish = waitForActivityFinish(activity);
		assertTrue("Activity should have finished", didFinish);
	}

	@Test
	public void testWrongSettings () throws Exception {
		final Activity activity = waitForActivityStart(null);
		assertNotNull("Started activity should not be null!", activity);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				DeviceLog.debug("GOT EVENT: " + eventId.name() + ": " + CONDITION_VARIABLE);

				if (eventId.name().equals("PAGE_FINISHED") && CONDITION_VARIABLE != null) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();

		final JSONObject webSettings = new JSONObject();
		webSettings.put("xxsetSaveFormData", new JSONArray("[true]"));
		webSettings.put("xxsetSavePassword", new JSONArray("[true]"));
		webSettings.put("setLayoutAlgorithm", new JSONArray("[{\"type\": \"Enum\", \"className\": \"android.webkit.WebSettings$LayoutAlgorithm\"}]"));

		final JSONObject webPlayerSettings = new JSONObject();
		webPlayerSettings.put("xxsetHorizontalScrollBarEnabled", new JSONArray("[false]"));
		webPlayerSettings.put("xxsetVerticalScrollBarEnabled", new JSONArray("[false]"));

		handler.post(new Runnable() {
			@Override
			public void run() {
				_webPlayer = new WebPlayer(activity, webSettings, webPlayerSettings);
				activity.addContentView(_webPlayer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
			}
		});
		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);

		assertNotNull("WebPlayer should not be null", _webPlayer);
		assertNotNull("There should be errored settings", _webPlayer.getErroredSettings());
		assertEquals("There should be 4 errored settings",5, _webPlayer.getErroredSettings().size());

		boolean didFinish = waitForActivityFinish(activity);
		assertTrue("Activity should have finished", didFinish);
	}

	@Test
	public void testCorrectUrl () throws Exception {
		final Activity activity = waitForActivityStart(null);
		assertNotNull("Started activity should not be null!", activity);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				DeviceLog.debug("GOT EVENT: " + eventId.name() + ": " + CONDITION_VARIABLE);

				if (eventId.name().equals("PAGE_FINISHED") && CONDITION_VARIABLE != null) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();

		final JSONObject eventSettings = new JSONObject();
		eventSettings.put("onPageStarted", new JSONObject("{\"sendEvent\":true}"));
		eventSettings.put("onPageFinished", new JSONObject("{\"sendEvent\":true}"));
		eventSettings.put("onReceivedError", new JSONObject("{\"sendEvent\":true}"));

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final ConditionVariable loadUrlCV = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = loadUrlCV;

		handler.post(new Runnable() {
			@Override
			public void run() {
				_webPlayer = new WebPlayer(activity, null, null);
				_webPlayer.setEventSettings(eventSettings);
				activity.addContentView(_webPlayer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
				_webPlayer.loadUrl(TestUtilities.getTestServerAddress());
			}
		});

		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);
		assertNotNull("WebPlayer should not be null", _webPlayer);

		boolean loadUrlSuccess = loadUrlCV.block(10000);
		assertTrue("LoadUrl ConditionVariable did not open successfully", loadUrlSuccess);

		boolean startEventFound = false;
		boolean finishEventFound = false;
		boolean receivedError = false;

		for (Enum eventId : mockWebViewApp.EVENTS) {
			if (eventId.name().equals("PAGE_STARTED")) {
				startEventFound = true;
			}
			if (eventId.name().equals("PAGE_FINISHED")) {
				finishEventFound = true;
			}
			if (eventId.name().equals("ERROR")) {
				receivedError = true;
			}
		}

		assertTrue("Should have received start event", startEventFound);
		assertTrue("Should have received finish event", finishEventFound);
		assertFalse("Shouldn't have received an error", receivedError);

		boolean didFinish = waitForActivityFinish(activity);
		assertTrue("Activity should have finished", didFinish);
	}

	@Test
	public void testIncorrectUrl () throws Exception {
		final Activity activity = waitForActivityStart(null);
		assertNotNull("Started activity should not be null!", activity);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				DeviceLog.debug("GOT EVENT: " + eventId.name() + ": " + CONDITION_VARIABLE);

				if (eventId.name().equals("PAGE_FINISHED") && CONDITION_VARIABLE != null) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();

		final JSONObject eventSettings = new JSONObject();
		eventSettings.put("onPageStarted", new JSONObject("{\"sendEvent\":true}"));
		eventSettings.put("onPageFinished", new JSONObject("{\"sendEvent\":true}"));
		eventSettings.put("onReceivedError", new JSONObject("{\"sendEvent\":true}"));

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();
		final ConditionVariable loadUrlCV = new ConditionVariable();
		mockWebViewApp.CONDITION_VARIABLE = loadUrlCV;

		handler.post(new Runnable() {
			@Override
			public void run() {
				_webPlayer = new WebPlayer(activity, null, null);
				_webPlayer.setEventSettings(eventSettings);
				activity.addContentView(_webPlayer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
				_webPlayer.loadUrl("http://testing.wrong.url");
			}
		});

		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);
		assertNotNull("WebPlayer should not be null", _webPlayer);

		boolean loadUrlSuccess = loadUrlCV.block(10000);
		assertTrue("LoadUrl ConditionVariable did not open successfully", loadUrlSuccess);

		boolean startEventFound = false;
		boolean finishEventFound = false;
		boolean receivedError = false;

		for (Enum eventId : mockWebViewApp.EVENTS) {
			if (eventId.name().equals("PAGE_STARTED")) {
				startEventFound = true;
			}
			if (eventId.name().equals("PAGE_FINISHED")) {
				finishEventFound = true;
			}
			if (eventId.name().equals("ERROR")) {
				receivedError = true;
			}
		}

		assertTrue("Should have received start event", startEventFound);
		assertTrue("Should have received finish event", finishEventFound);
		assertTrue("Should have received an error", receivedError);

		boolean didFinish = waitForActivityFinish(activity);
		assertTrue("Activity should have finished", didFinish);
	}

	@Test
	public void testCorrectUrlNullEvents () throws Exception {
		final Activity activity = waitForActivityStart(null);
		assertNotNull("Started activity should not be null!", activity);

		WebViewApp.setCurrentApp(new MockWebViewApp() {
			public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
				super.sendEvent(eventCategory, eventId, params);

				EVENT_CATEGORIES.add(eventCategory);
				EVENTS.add(eventId);
				EVENT_PARAMS = params;
				EVENT_COUNT++;

				DeviceLog.debug("GOT EVENT: " + eventId.name() + ": " + CONDITION_VARIABLE);

				if (eventId.name().equals("PAGE_FINISHED") && CONDITION_VARIABLE != null) {
					CONDITION_VARIABLE.open();
				}

				return true;
			}
		});

		final Handler handler = new Handler(Looper.getMainLooper());
		final ConditionVariable viewAddCV = new ConditionVariable();

		final MockWebViewApp mockWebViewApp = (MockWebViewApp)WebViewApp.getCurrentApp();

		handler.post(new Runnable() {
			@Override
			public void run() {
				_webPlayer = new WebPlayer(activity, null, null);
				_webPlayer.setEventSettings(null);
				activity.addContentView(_webPlayer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				viewAddCV.open();
				_webPlayer.loadUrl(TestUtilities.getTestServerAddress());
			}
		});

		boolean success = viewAddCV.block(3000);
		assertTrue("ConditionVariable did not open in view add", success);
		assertNotNull("WebPlayer should not be null", _webPlayer);

		boolean startEventFound = false;
		boolean finishEventFound = false;
		boolean receivedError = false;

		for (Enum eventId : mockWebViewApp.EVENTS) {
			if (eventId.name().equals("PAGE_STARTED")) {
				startEventFound = true;
			}
			if (eventId.name().equals("PAGE_FINISHED")) {
				finishEventFound = true;
			}
			if (eventId.name().equals("ERROR")) {
				receivedError = true;
			}
		}

		assertFalse("Shouldn't have received start event", startEventFound);
		assertFalse("Shouldn't have received finish event", finishEventFound);
		assertFalse("Shouldn't have received an error", receivedError);

		boolean didFinish = waitForActivityFinish(activity);
		assertTrue("Activity should have finished", didFinish);
	}
}
