package com.unity3d.ads.test.legacy;

import android.support.test.runner.AndroidJUnit4;

import com.unity3d.services.ads.token.TokenEvent;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class TokenStorageTest {
	@Before
	public void reset() {
		MockWebViewApp webViewApp = new MockWebViewApp();
		WebViewApp.setCurrentApp(webViewApp);

		TokenStorage.setPeekMode(false);
		TokenStorage.deleteTokens();
	}

	@Test
	public void testCreateGetToken() throws JSONException {
		MockWebViewApp currentApp = (MockWebViewApp)WebViewApp.getCurrentApp();

		TokenStorage.createTokens(getTestArray());

		String firstToken = TokenStorage.getToken();
		assertEquals("Token create+get test: first value was not one", firstToken, "one");
		assertEquals("Token create+get test: event count not one after first event", currentApp.getEventCount(), 1);
		assertEquals("Token create+get test: event category not TOKEN in first event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+get test: event ID not TOKEN_ACCESS in first event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+get test: access counter is not zero in first event", currentApp.getLastParams()[0], 0);

		String secondToken = TokenStorage.getToken();
		assertEquals("Token create+get test: second value was not two", secondToken, "two");
		assertEquals("Token create+get test: event count not two after second event", currentApp.getEventCount(), 2);
		assertEquals("Token create+get test: event category not TOKEN in second event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+get test: event ID not TOKEN_ACCESS in second event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+get test: access counter is not one in second event", currentApp.getLastParams()[0], 1);

		String thirdToken = TokenStorage.getToken();
		assertEquals("Token create+get test: third value was not three", thirdToken, "three");
		assertEquals("Token create+get test: event count not three after third event", currentApp.getEventCount(), 3);
		assertEquals("Token create+get test: event category not TOKEN in third event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+get test: event ID not TOKEN_ACCESS in third event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+get test: access counter is not two in third event", currentApp.getLastParams()[0], 2);

		String nullToken = TokenStorage.getToken();
		assertNull("Token create+get test: value is not null when queue is empty", nullToken);
		assertEquals("Token create+get test: event count not four after fourth event", currentApp.getEventCount(), 4);
		assertEquals("Token create+get test: event category not TOKEN in fourth event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+get test: event ID not QUEUE_EMPTY in fourth event", currentApp.getLastEventId(), TokenEvent.QUEUE_EMPTY);
	}

	@Test
	public void testCreateDeleteToken() throws JSONException {
		MockWebViewApp currentApp = (MockWebViewApp)WebViewApp.getCurrentApp();

		TokenStorage.createTokens(getTestArray());
		TokenStorage.deleteTokens();

		String nullToken = TokenStorage.getToken();
		assertNull("Token create+delete test: value is not null when queue was deleted", nullToken);
		assertEquals("Token create+delete test: webview event was triggered when queue was deleted", currentApp.getEventCount(), 0);
	}

	@Test
	public void testCreateAppendGetToken() throws JSONException {
		MockWebViewApp currentApp = (MockWebViewApp)WebViewApp.getCurrentApp();

		TokenStorage.createTokens(getTestArray());

		String firstToken = TokenStorage.getToken();
		assertEquals("Token create+append+get test: first value was not one", firstToken, "one");
		assertEquals("Token create+append+get test: event count not one after first event", currentApp.getEventCount(), 1);
		assertEquals("Token create+append+get test: event category not TOKEN in first event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in first event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not zero in first event", currentApp.getLastParams()[0], 0);

		TokenStorage.appendTokens(getTestArray());

		String secondToken = TokenStorage.getToken();
		assertEquals("Token create+append+get test: second value was not two", secondToken, "two");
		assertEquals("Token create+append+get test: event count not two after second event", currentApp.getEventCount(), 2);
		assertEquals("Token create+append+get test: event category not TOKEN in second event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in second event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not one in second event", currentApp.getLastParams()[0], 1);

		String thirdToken = TokenStorage.getToken();
		assertEquals("Token create+append+get test: third value was not three", thirdToken, "three");
		assertEquals("Token create+append+get test: event count not three after third event", currentApp.getEventCount(), 3);
		assertEquals("Token create+append+get test: event category not TOKEN in third event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in third event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not two in third event", currentApp.getLastParams()[0], 2);

		String fourthToken = TokenStorage.getToken();
		assertEquals("Token create+append+get test: fourth value was not one", fourthToken, "one");
		assertEquals("Token create+append+get test: event count not four after fourth event", currentApp.getEventCount(), 4);
		assertEquals("Token create+append+get test: event category not TOKEN in fourth event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in fourth event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not three in fourth event", currentApp.getLastParams()[0], 3);

		String fifthToken = TokenStorage.getToken();
		assertEquals("Token create+append+get test: fifth value was not two", fifthToken, "two");
		assertEquals("Token create+append+get test: event count not five after fifth event", currentApp.getEventCount(), 5);
		assertEquals("Token create+append+get test: event category not TOKEN in fifth event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in fifth event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not four in fifth event", currentApp.getLastParams()[0], 4);

		String sixthToken = TokenStorage.getToken();
		assertEquals("Token create+append+get test: sixth value was not three", sixthToken, "three");
		assertEquals("Token create+append+get test: event count not six after sixth event", currentApp.getEventCount(), 6);
		assertEquals("Token create+append+get test: event category not TOKEN in sixth event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in sixth event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not five in sixth event", currentApp.getLastParams()[0], 5);

		String nullToken = TokenStorage.getToken();
		assertNull("Token create+append+get test: value is not null when queue is empty", nullToken);
		assertEquals("Token create+append+get test: event count not seven after seventh event", currentApp.getEventCount(), 7);
		assertEquals("Token create+append+get test: event category not TOKEN in seventh event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not QUEUE_EMPTY in seventh event", currentApp.getLastEventId(), TokenEvent.QUEUE_EMPTY);
	}

	@Test
	public void testPeekMode() throws JSONException {
		String staticToken = "static_token";

		MockWebViewApp currentApp = (MockWebViewApp)WebViewApp.getCurrentApp();

		JSONArray array = new JSONArray();
		array.put(staticToken);

		TokenStorage.setPeekMode(true);
		TokenStorage.createTokens(array);

		String firstToken = TokenStorage.getToken();
		assertEquals("Token peekmode test: first value was not correct", firstToken, staticToken);
		assertEquals("Token peekmode test: event count not one after first event", currentApp.getEventCount(), 1);
		assertEquals("Token peekmode test: event category not TOKEN in first event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token peekmode test: event ID not TOKEN_ACCESS in first event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token peekmode test: access counter is not zero in first event", currentApp.getLastParams()[0], 0);

		String secondToken = TokenStorage.getToken();
		assertEquals("Token peekmode test: second value was not correct", secondToken, staticToken);
		assertEquals("Token peekmode test: event count not two after second event", currentApp.getEventCount(), 2);
		assertEquals("Token peekmode test: event category not TOKEN in second event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token peekmode test: event ID not TOKEN_ACCESS in second event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token peekmode test: access counter is not one in second event", currentApp.getLastParams()[0], 1);

		String thirdToken = TokenStorage.getToken();
		assertEquals("Token peekmode test: third value was not correct", thirdToken, staticToken);
		assertEquals("Token peekmode test: event count not one after third event", currentApp.getEventCount(), 3);
		assertEquals("Token peekmode test: event category not TOKEN in third event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token peekmode test: event ID not TOKEN_ACCESS in third event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token peekmode test: access counter is not two in third event", currentApp.getLastParams()[0], 2);
	}

	private JSONArray getTestArray() {
		JSONArray array = new JSONArray();
		array.put("one");
		array.put("two");
		array.put("three");
		return array;
	}

	private class MockWebViewApp extends WebViewApp {
		private Object[] lastParams = null;
		private Enum lastEventCategory = null;
		private Enum lastEventId = null;
		private int eventCount = 0;

		@Override
		public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
			lastParams = params;
			lastEventCategory = eventCategory;
			lastEventId = eventId;
			eventCount++;
			return true;
		}

		public Object[] getLastParams() {
			return lastParams;
		}

		public Enum getLastEventCategory() {
			return lastEventCategory;
		}

		public Enum getLastEventId() {
			return lastEventId;
		}

		public int getEventCount() {
			return eventCount;
		}
	}
}
