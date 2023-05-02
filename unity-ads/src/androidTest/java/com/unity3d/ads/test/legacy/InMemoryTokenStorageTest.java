package com.unity3d.ads.test.legacy;

import android.os.Handler;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.ads.gmascar.managers.IBiddingManager;
import com.unity3d.services.ads.token.*;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

@RunWith(AndroidJUnit4.class)
public class InMemoryTokenStorageTest {
	private TokenStorage tokenStorage;
	private AsyncTokenStorage asyncTokenStorage;

	@Before
	public void reset() {
		MockWebViewApp webViewApp = new MockWebViewApp();
		WebViewApp.setCurrentApp(webViewApp);

		tokenStorage = new InMemoryTokenStorage();
		tokenStorage.deleteTokens();
		tokenStorage.setInitToken(null);

		asyncTokenStorage = new InMemoryAsyncTokenStorage(
				null,
				new Handler(Looper.getMainLooper()),
				SDKMetrics.getInstance(),
				tokenStorage
		);
	}

	@After
	public void After() {
		ClientProperties.setApplicationContext(null);
		WebViewApp.setCurrentApp(null);
		SdkProperties.setInitializeState(SdkProperties.InitializationState.NOT_INITIALIZED);
	}

	@Test
	public void testCreateGetToken() throws JSONException {
		MockWebViewApp currentApp = (MockWebViewApp)WebViewApp.getCurrentApp();

		tokenStorage.createTokens(getTestArray());

		String firstToken = tokenStorage.getToken();
		assertEquals("Token create+get test: first value was not one", firstToken, "one");
		assertEquals("Token create+get test: event count not one after first event", currentApp.getEventCount(), 1);
		assertEquals("Token create+get test: event category not TOKEN in first event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+get test: event ID not TOKEN_ACCESS in first event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+get test: access counter is not zero in first event", currentApp.getLastParams()[0], 0);

		String secondToken = tokenStorage.getToken();
		assertEquals("Token create+get test: second value was not two", secondToken, "two");
		assertEquals("Token create+get test: event count not two after second event", currentApp.getEventCount(), 2);
		assertEquals("Token create+get test: event category not TOKEN in second event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+get test: event ID not TOKEN_ACCESS in second event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+get test: access counter is not one in second event", currentApp.getLastParams()[0], 1);

		String thirdToken = tokenStorage.getToken();
		assertEquals("Token create+get test: third value was not three", thirdToken, "three");
		assertEquals("Token create+get test: event count not three after third event", currentApp.getEventCount(), 3);
		assertEquals("Token create+get test: event category not TOKEN in third event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+get test: event ID not TOKEN_ACCESS in third event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+get test: access counter is not two in third event", currentApp.getLastParams()[0], 2);

		String nullToken = tokenStorage.getToken();
		assertNull("Token create+get test: value is not null when queue is empty", nullToken);
		assertEquals("Token create+get test: event count not four after fourth event", currentApp.getEventCount(), 4);
		assertEquals("Token create+get test: event category not TOKEN in fourth event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+get test: event ID not QUEUE_EMPTY in fourth event", currentApp.getLastEventId(), TokenEvent.QUEUE_EMPTY);
	}

	@Test
	public void testCreateDeleteToken() throws JSONException {
		MockWebViewApp currentApp = (MockWebViewApp)WebViewApp.getCurrentApp();

		tokenStorage.createTokens(getTestArray());
		tokenStorage.deleteTokens();

		String nullToken = tokenStorage.getToken();
		assertNull("Token create+delete test: value is not null when queue was deleted", nullToken);
		assertEquals("Token create+delete test: webview event was triggered when queue was deleted", currentApp.getEventCount(), 0);
	}

	@Test
	public void testCreateAppendGetToken() throws JSONException {
		MockWebViewApp currentApp = (MockWebViewApp)WebViewApp.getCurrentApp();

		tokenStorage.createTokens(getTestArray());

		String firstToken = tokenStorage.getToken();
		assertEquals("Token create+append+get test: first value was not one", firstToken, "one");
		assertEquals("Token create+append+get test: event count not one after first event", currentApp.getEventCount(), 1);
		assertEquals("Token create+append+get test: event category not TOKEN in first event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in first event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not zero in first event", currentApp.getLastParams()[0], 0);

		tokenStorage.appendTokens(getTestArray());

		String secondToken = tokenStorage.getToken();
		assertEquals("Token create+append+get test: second value was not two", secondToken, "two");
		assertEquals("Token create+append+get test: event count not two after second event", currentApp.getEventCount(), 2);
		assertEquals("Token create+append+get test: event category not TOKEN in second event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in second event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not one in second event", currentApp.getLastParams()[0], 1);

		String thirdToken = tokenStorage.getToken();
		assertEquals("Token create+append+get test: third value was not three", thirdToken, "three");
		assertEquals("Token create+append+get test: event count not three after third event", currentApp.getEventCount(), 3);
		assertEquals("Token create+append+get test: event category not TOKEN in third event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in third event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not two in third event", currentApp.getLastParams()[0], 2);

		String fourthToken = tokenStorage.getToken();
		assertEquals("Token create+append+get test: fourth value was not one", fourthToken, "one");
		assertEquals("Token create+append+get test: event count not four after fourth event", currentApp.getEventCount(), 4);
		assertEquals("Token create+append+get test: event category not TOKEN in fourth event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in fourth event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not three in fourth event", currentApp.getLastParams()[0], 3);

		String fifthToken = tokenStorage.getToken();
		assertEquals("Token create+append+get test: fifth value was not two", fifthToken, "two");
		assertEquals("Token create+append+get test: event count not five after fifth event", currentApp.getEventCount(), 5);
		assertEquals("Token create+append+get test: event category not TOKEN in fifth event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in fifth event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not four in fifth event", currentApp.getLastParams()[0], 4);

		String sixthToken = tokenStorage.getToken();
		assertEquals("Token create+append+get test: sixth value was not three", sixthToken, "three");
		assertEquals("Token create+append+get test: event count not six after sixth event", currentApp.getEventCount(), 6);
		assertEquals("Token create+append+get test: event category not TOKEN in sixth event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not TOKEN_ACCESS in sixth event", currentApp.getLastEventId(), TokenEvent.TOKEN_ACCESS);
		assertEquals("Token create+append+get test: access counter is not five in sixth event", currentApp.getLastParams()[0], 5);

		String nullToken = tokenStorage.getToken();
		assertNull("Token create+append+get test: value is not null when queue is empty", nullToken);
		assertEquals("Token create+append+get test: event count not seven after seventh event", currentApp.getEventCount(), 7);
		assertEquals("Token create+append+get test: event category not TOKEN in seventh event", currentApp.getLastEventCategory(), WebViewEventCategory.TOKEN);
		assertEquals("Token create+append+get test: event ID not QUEUE_EMPTY in seventh event", currentApp.getLastEventId(), TokenEvent.QUEUE_EMPTY);
	}

	@Test(timeout = 2000)
	public void testDeadlockRegression() throws InterruptedException, IllegalAccessException {
		tokenStorage.deleteTokens();

		Field field = Arrays.stream(InMemoryTokenStorage.class.getDeclaredFields()).filter(f -> f.getName().contains("asyncTokenStorage")).findFirst().get();
		field.setAccessible(true);
		field.set(tokenStorage, kotlin.LazyKt.lazyOf(asyncTokenStorage));

		ClientProperties.setApplicationContext(androidx.test.core.app.ApplicationProvider.getApplicationContext());
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);

		final int tokenRequestCount = 2;
		final CountDownLatch appendTokensStart = new CountDownLatch(1);
		final CountDownLatch tokensReady = new CountDownLatch(tokenRequestCount);
		final String[] actualToken = {"", ""};

		asyncTokenStorage.setConfiguration(new Configuration());

		Thread appendTokens = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					appendTokensStart.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					tokenStorage.appendTokens(getTestArray());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});

		appendTokens.start();

		Thread.sleep(200);
		appendTokensStart.countDown();

		for (int i = 0; i < tokenRequestCount; i++) {
			final int finalI = i;
			asyncTokenStorage.getToken(new IBiddingManager() {
				@Override
				public String getTokenIdentifier() {
					return "";
				}

				@Override
				public String getFormattedToken(String unityToken) {
					return unityToken;
				}

				@Override
				public void onUnityAdsTokenReady(String token) {
					tokensReady.countDown();
					actualToken[finalI] = token;
				}
			});
		}

		tokensReady.await();
		Assert.assertEquals("one", actualToken[0]);
		Assert.assertEquals("two", actualToken[1]);
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
