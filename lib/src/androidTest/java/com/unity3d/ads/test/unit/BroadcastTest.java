package com.unity3d.ads.test.unit;

import android.content.Intent;
import android.net.Uri;
import android.os.ConditionVariable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.broadcast.BroadcastEvent;
import com.unity3d.ads.broadcast.BroadcastEventReceiver;
import com.unity3d.ads.broadcast.BroadcastMonitor;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class BroadcastTest {
	@Before
	public void setup() {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
	}

	@After
	public void cleanup() {
		BroadcastMonitor.removeAllBroadcastListeners();
	}

	@Test
	public void testOneReceiver() {
		final String testAction = "com.unity3d.ads.ACTION_TEST";
		final String fakeAction = "com.unity3d.ads.ACTION_FAKE";
		final String testReceiverName = "testReceiver";

		final ConditionVariable broadcastCv = new ConditionVariable();
		final ConditionVariable broadcastCv2 = new ConditionVariable();

		MockWebViewApp webapp = new MockWebViewApp() {
			@Override
			public void broadcastEvent(BroadcastEvent eventId, String name, String action, String dataScheme, JSONObject extras) {
				assertEquals("Broadcast test one receiver: wrong event id", eventId, BroadcastEvent.ACTION);
				assertEquals("Broadcast test one receiver: wrong receiver name", name, testReceiverName);
				assertEquals("Broadcast test one receiver: data scheme should be empty", dataScheme, "");
				assertEquals("Broadcast test one receiver: json extras should be empty", extras.length(), 0);
				broadcastCv.open();
			}
		};
		WebViewApp.setCurrentApp(webapp);

		BroadcastMonitor.addBroadcastListener(testReceiverName, null, new String[]{testAction});

		Intent earlyIntent = new Intent();
		earlyIntent.setAction(testAction);
		ClientProperties.getApplicationContext().sendBroadcast(earlyIntent);

		boolean status = broadcastCv.block(500);
		assertFalse("Broadcast test one receiver: no event should be sent when webapp is not initialized", status);

		final BroadcastEventReceiver receiver = new BroadcastEventReceiver("test");
		new Thread() {
			@Override
			public void run() {
				try {
					sleep(100);
				} catch (InterruptedException e) { }

				receiver.onReceive(ClientProperties.getApplicationContext(), new Intent());
			}
		}.start();

		boolean status2 = broadcastCv.block(500);
		assertFalse("Broadcast test one receiver: no event should be sent when intent is empty", status2);

		WebViewApp.getCurrentApp().setWebAppLoaded(true);
		SdkProperties.setInitialized(true);

		Intent fakeIntent = new Intent();
		fakeIntent.setAction(fakeAction);
		ClientProperties.getApplicationContext().sendBroadcast(fakeIntent);

		Intent testIntent = new Intent();
		testIntent.setAction(testAction);
		ClientProperties.getApplicationContext().sendBroadcast(testIntent);

		boolean success = broadcastCv.block(30000);
		assertTrue("Broadcast test one receiver: timeout, condition variable was not opened", success);

		MockWebViewApp webapp2 = new MockWebViewApp() {
			@Override
			public void broadcastEvent(BroadcastEvent eventId, String name, String action, String dataScheme, JSONObject extras) {
				broadcastCv2.open();
			}
		};
		WebViewApp.setCurrentApp(webapp2);

		BroadcastMonitor.removeAllBroadcastListeners();

		Intent lateIntent = new Intent();
		lateIntent.setAction(testAction);
		ClientProperties.getApplicationContext().sendBroadcast(lateIntent);

		boolean success2 = broadcastCv2.block(500);
		assertFalse("Broadcast test one receiver: broadcast was processed but event receiver was already removed", success2);
	}

	@Test
	public void testTwoReceivers() throws JSONException {
		final String jsonAction = "com.unity3d.ads.ACTION_JSON";
		final String jsonReceiverName = "jsonReceiver";
		final String dataSchemeAction = "com.unity3d.ads.ACTION_DATA_SCHEME";
		final String dataSchemeReceiverName = "dataSchemeReceiver";

		final String testDataScheme = "test";
		final String testDataHost = "example.net";
		final String testDataPath = "/path";

		final String testStringKey = "testString";
		final String testBoolKey = "testBoolean";
		final String testLongKey = "testLong";
		final String testStringValue = "example";
		final boolean testBoolValue = true;
		final long testLongValue = (long)1234;

		final ConditionVariable jsonCv = new ConditionVariable();
		final ConditionVariable dataSchemeCv = new ConditionVariable();

		MockWebViewApp webapp = new MockWebViewApp() {
			@Override
			public void broadcastEvent(BroadcastEvent eventId, String name, String action, String data, JSONObject extras) {
				assertEquals("Broadcast test two receivers: wrong event id", eventId, BroadcastEvent.ACTION);
				assertTrue("Broadcast test two receivers: receiver name does not match json or data scheme receiver names", name.equals(jsonReceiverName) || name.equals(dataSchemeReceiverName));

				if(name.equals(jsonReceiverName)) {
					assertEquals("Broadcast test two receivers: action does not match", jsonAction, action);
					assertEquals("Broadcast test two receivers: there should be no data in event", "", data);
					assertEquals("Broadcast test two receivers: broadcast was sent with three values in extra bundle", 3, extras.length());
					try {
						assertEquals("Broadcast test two receivers: problem with string in bundle extras", testStringValue, extras.getString(testStringKey));
						assertEquals("Broadcast test two receivers: problem with boolean in bundle extras", testBoolValue, extras.getBoolean(testBoolKey));
						assertEquals("Broadcast test two receivers: problem with long in bundle extras", testLongValue, extras.getLong(testLongKey));
					} catch(JSONException e) {
						fail("Broadcast test two receivers: JSONException: " + e.getMessage());
					}

					jsonCv.open();
				} else {
					assertEquals("Broadcast test two receivers: action does not match", dataSchemeAction, action);
					assertEquals("Broadcast test two receivers: there should be no bundle extras in this event", 0, extras.length());

					Uri testUri = Uri.parse(data);
					assertEquals("Broadcast test two receivers: data scheme does not match", testDataScheme, testUri.getScheme());
					assertEquals("Broadcast test two receivers: data host does not match", testDataHost, testUri.getHost());
					assertEquals("Broadcast test two receivers: data path does not match", testDataPath, testUri.getPath());

					dataSchemeCv.open();
				}
			}
		};
		WebViewApp.setCurrentApp(webapp);
		WebViewApp.getCurrentApp().setWebAppLoaded(true);
		SdkProperties.setInitialized(true);

		BroadcastMonitor.addBroadcastListener(jsonReceiverName, null, new String[]{jsonAction});
		BroadcastMonitor.addBroadcastListener(dataSchemeReceiverName, testDataScheme, new String[]{dataSchemeAction});

		Intent jsonIntent = new Intent();
		jsonIntent.setAction(jsonAction);
		jsonIntent.putExtra(testStringKey, testStringValue);
		jsonIntent.putExtra(testBoolKey, testBoolValue);
		jsonIntent.putExtra(testLongKey, testLongValue);
		ClientProperties.getApplicationContext().sendBroadcast(jsonIntent);

		boolean success = jsonCv.block(30000);
		assertTrue("Broadcast test two receivers: ", success);

		Intent dataIntent = new Intent();
		dataIntent.setAction(dataSchemeAction);
		dataIntent.setData(Uri.parse(testDataScheme + "://" + testDataHost + testDataPath));
		ClientProperties.getApplicationContext().sendBroadcast(dataIntent);

		boolean success2 = dataSchemeCv.block(30000);
		assertTrue("Broadcast test two receivers: ", success2);
	}

	public class MockWebViewApp extends WebViewApp {
		public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
			switch((WebViewEventCategory) eventCategory) {
				case BROADCAST:
					if(params.length != 4) {
						throw new IllegalArgumentException("Wrong number of event arguments, should be 4, is actually " + params.length);
					}
					broadcastEvent((BroadcastEvent) eventId, (String)params[0], (String)params[1], (String)params[2], (JSONObject)params[3]);
					return true;

				default:
					throw new IllegalArgumentException("Unknown event category");
			}
		}

		public void broadcastEvent(BroadcastEvent eventId, String name, String action, String data, JSONObject extras) { }
	}
}