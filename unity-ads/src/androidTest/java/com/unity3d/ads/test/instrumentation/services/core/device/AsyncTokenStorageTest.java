package com.unity3d.ads.test.instrumentation.services.core.device;

import static com.unity3d.services.core.device.TokenType.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

import android.os.Handler;
import android.os.Message;

import com.unity3d.ads.IUnityAdsTokenListener;
import com.unity3d.services.ads.token.AsyncTokenStorage;
import com.unity3d.services.ads.token.INativeTokenGenerator;
import com.unity3d.services.ads.token.INativeTokenGeneratorListener;
import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.device.TokenType;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.webview.WebViewApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AsyncTokenStorageTest {
	@Mock
	INativeTokenGenerator _nativeTokenGenerator;
	@Mock
	Handler _handler;
	@Mock
	IUnityAdsTokenListener _listener;

	AsyncTokenStorage _asyncTokenStorage;
	Runnable timeoutRunnable;
	List<Runnable> handlerRunnable = new ArrayList<>();

	@Before
	public void Before() {
		ClientProperties.setApplicationContext(androidx.test.core.app.ApplicationProvider.getApplicationContext());
		TokenStorage.deleteTokens();
		TokenStorage.setInitToken(null);
		MockWebViewApp webViewApp = new MockWebViewApp();
		WebViewApp.setCurrentApp(webViewApp);
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);

		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler);
		_asyncTokenStorage.setConfiguration(new Configuration());

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				Message message = invocation.getArgument(0);
				timeoutRunnable = message.getCallback();
				handlerRunnable.add(message.getCallback());
				return true;
			}
		}).when(_handler).sendMessageAtTime(any(Message.class), anyLong());
	}

	@After
	public void After() {
		ClientProperties.setApplicationContext(null);
		WebViewApp.setCurrentApp(null);
		SdkProperties.setInitializeState(SdkProperties.InitializationState.NOT_INITIALIZED);
	}

	@Test
	public void testNotInitializedSdk() {
		SdkProperties.setInitializeState(SdkProperties.InitializationState.NOT_INITIALIZED);

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testGetTokenBeforeInit() {
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());
	}

	@Test
	public void testGetTokenBeforeInitAndTimeout() {
		_asyncTokenStorage.getToken(_listener);

		timeoutRunnable.run();

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testGetTokenBeforeInitAndTimeoutGuard() {
		_asyncTokenStorage.getToken(_listener);

		timeoutRunnable.run();
		timeoutRunnable.run();
		timeoutRunnable.run();

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testGetTokenBeforeInitAndInitTimeTokenReady() {
		_asyncTokenStorage.getToken(_listener);

		TokenStorage.setInitToken("init_time_token");
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("init_time_token");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testGetTokenBeforeInitAndCreateEmptyQueue() throws JSONException {
		_asyncTokenStorage.getToken(_listener);

		TokenStorage.createTokens(new JSONArray());
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testGetTokenBeforeInitAndCreateQueueTokenReady() throws JSONException {
		_asyncTokenStorage.getToken(_listener);

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testGetTokenAfterInitTokenReady() throws JSONException {
		TokenStorage.setInitToken("init_time_token");
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("init_time_token");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));

	}

	@Test
	public void testGetTokenAfterQueueTokenReady() throws JSONException {
		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testMultipleCallsBeforeInit() throws JSONException {
		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(3)).sendMessageAtTime(any(Message.class), anyLong());

		TokenStorage.setInitToken("init_time_token");
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		Mockito.verify(_listener, Mockito.times(3)).onUnityAdsTokenReady("init_time_token");
		Mockito.verify(_listener, Mockito.times(3)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testMultipleCallsBeforeInitAndQueue() throws JSONException {
		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(3)).sendMessageAtTime(any(Message.class), anyLong());

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		array.put("queue_token_2");
		array.put("queue_token_3");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		InOrder order = Mockito.inOrder(_listener);
		order.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
		order.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_2");
		order.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_3");
		Mockito.verify(_listener, Mockito.times(3)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testMultipleCallsBeforeInitAndSingleAfterInit() throws JSONException {
		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(3)).sendMessageAtTime(any(Message.class), anyLong());

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		array.put("queue_token_2");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		Mockito.verify(_listener, Mockito.times(2)).onUnityAdsTokenReady(nullable(String.class));

		array = new JSONArray();
		array.put("queue_token_3");
		TokenStorage.appendTokens(array);
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_3");
		Mockito.verify(_listener, Mockito.times(3)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testGetTokenFailedInit() throws JSONException {
		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_FAILED);

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(0)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testGetTokenFailedInitCancelAllRequests() {
		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_FAILED);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testNativeToken() throws JSONException {
		Configuration configuration = Mockito.mock(Configuration.class);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("tsi_nt", true);
		Mockito.when(configuration.getExperiments()).thenReturn(new Experiments(jsonObject));
		_asyncTokenStorage.setConfiguration(configuration);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				invocation.getArgument(0, INativeTokenGeneratorListener.class).onReady("native_token");
				return null;
			}}).when(_nativeTokenGenerator).generateToken(any(INativeTokenGeneratorListener.class));

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(2)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		handlerRunnable.get(1).run();

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("native_token");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testDoNotUseNativeToken() throws JSONException {
		Configuration configuration = Mockito.mock(Configuration.class);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("tsi_nt", true);
		Mockito.when(configuration.getExperiments()).thenReturn(new Experiments(jsonObject));
		_asyncTokenStorage.setConfiguration(configuration);

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_nativeTokenGenerator, times(0)).generateToken(any(INativeTokenGeneratorListener.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testDelayedConfiguration() throws JSONException {
		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		_asyncTokenStorage.setConfiguration(new Configuration());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
	}

	@Test
	public void testDelayedConfigurationTwoConfigSets() throws JSONException {
		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		_asyncTokenStorage.setConfiguration(new Configuration());
		_asyncTokenStorage.setConfiguration(new Configuration());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
	}

	@Test
	public void testDelayedConfigurationConfigurationArrivedFirst() throws JSONException {
		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		_asyncTokenStorage.setConfiguration(new Configuration());

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		TokenStorage.setInitToken("init_token");
		_asyncTokenStorage.onTokenAvailable(TOKEN_NATIVE);

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("init_token");
	}

	@Test
	public void testNullConfiguration() throws JSONException {
		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable(TOKEN_REMOTE);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		_asyncTokenStorage.setConfiguration(null);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
	}

	private class MockWebViewApp extends WebViewApp {
		@Override
		public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
			return true;
		}
	}
}
