package com.unity3d.ads.test.instrumentation.services.core.device;

import static org.junit.Assert.assertEquals;
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
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.metrics.ISDKMetrics;
import com.unity3d.services.core.request.metrics.Metric;
import com.unity3d.services.core.request.metrics.TSIMetric;
import com.unity3d.services.core.webview.WebViewApp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class AsyncTokenStorageTest {
	@Mock
	INativeTokenGenerator _nativeTokenGenerator;
	@Mock
	Handler _handler;
	@Mock
	IUnityAdsTokenListener _listener;
	@Mock
	ISDKMetrics _sdkMetrics;

	AsyncTokenStorage _asyncTokenStorage;
	Runnable timeoutRunnable;
	List<Runnable> handlerRunnable = new ArrayList<>();
	ArgumentCaptor<Metric> _metricsCaptor = ArgumentCaptor.forClass(Metric.class);

	@Before
	public void Before() {
		ClientProperties.setApplicationContext(androidx.test.core.app.ApplicationProvider.getApplicationContext());
		TokenStorage.deleteTokens();
		TokenStorage.setInitToken(null);
		MockWebViewApp webViewApp = new MockWebViewApp();
		WebViewApp.setCurrentApp(webViewApp);
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);

		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler, _sdkMetrics);
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
		Metric _metric = TSIMetric.newAsyncTokenNull(new HashMap<String, String>(){{
			put ("state", "not_initialized");
		}});

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenBeforeInit() {
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());
	}

	@Test
	public void testGetTokenBeforeInitAndTimeout() {
		Metric _metric = TSIMetric.newAsyncTokenNull(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_listener);

		timeoutRunnable.run();

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenBeforeInitAndTimeoutGuard() {
		Metric _metric = TSIMetric.newAsyncTokenNull(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_listener);

		timeoutRunnable.run();
		timeoutRunnable.run();
		timeoutRunnable.run();

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(3)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenBeforeInitAndInitTimeTokenReady() {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_listener);

		TokenStorage.setInitToken("init_time_token");
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("init_time_token");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenBeforeInitAndCreateEmptyQueue() throws JSONException {
		_asyncTokenStorage.getToken(_listener);

		TokenStorage.createTokens(new JSONArray());
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(0)).sendMetric(any(Metric.class));
	}

	@Test
	public void testGetTokenBeforeInitAndCreateQueueTokenReady() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_listener);

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenAfterInitTokenReady() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		TokenStorage.setInitToken("init_time_token");
		_asyncTokenStorage.onTokenAvailable();

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("init_time_token");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenAfterQueueTokenReady() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testMultipleCallsBeforeInit() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(3)).sendMessageAtTime(any(Message.class), anyLong());

		TokenStorage.setInitToken("init_time_token");
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_listener, Mockito.times(3)).onUnityAdsTokenReady("init_time_token");
		Mockito.verify(_listener, Mockito.times(3)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(3)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testMultipleCallsBeforeInitAndQueue() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(3)).sendMessageAtTime(any(Message.class), anyLong());

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		array.put("queue_token_2");
		array.put("queue_token_3");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		InOrder order = Mockito.inOrder(_listener);
		order.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
		order.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_2");
		order.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_3");
		Mockito.verify(_listener, Mockito.times(3)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(3)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testMultipleCallsBeforeInitAndSingleAfterInit() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(3)).sendMessageAtTime(any(Message.class), anyLong());

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		array.put("queue_token_2");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_listener, Mockito.times(2)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(2)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);

		array = new JSONArray();
		array.put("queue_token_3");
		TokenStorage.appendTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_3");
		Mockito.verify(_listener, Mockito.times(3)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(3)).sendMetric(_metricsCaptor.capture());
		capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenFailedInit() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenNull(new HashMap<String, String>(){{
			put ("state", "initialized_failed");
		}});

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_FAILED);

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(0)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenFailedInitCancelAllRequests() {
		_asyncTokenStorage.getToken(_listener);
		_asyncTokenStorage.getToken(_listener);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_FAILED);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(0)).sendMetric(any(Metric.class));
	}

	@Test
	public void testNativeToken() throws JSONException {
		Metric _metric = TSIMetric.newNativeGeneratedTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});
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
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testDoNotUseNativeToken() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});
		Configuration configuration = Mockito.mock(Configuration.class);

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("tsi_nt", true);
		Mockito.when(configuration.getExperiments()).thenReturn(new Experiments(jsonObject));
		_asyncTokenStorage.setConfiguration(configuration);

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_nativeTokenGenerator, times(0)).generateToken(any(INativeTokenGeneratorListener.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testDelayedConfiguration() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});
		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler, _sdkMetrics);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		_asyncTokenStorage.setConfiguration(new Configuration());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testDelayedConfigurationTwoConfigSets() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});
		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler, _sdkMetrics);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		_asyncTokenStorage.setConfiguration(new Configuration());
		_asyncTokenStorage.setConfiguration(new Configuration());

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testDelayedConfigurationConfigurationArrivedFirst() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});
		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler, _sdkMetrics);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		_asyncTokenStorage.setConfiguration(new Configuration());

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		TokenStorage.setInitToken("init_token");
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("init_token");
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testNullConfiguration() throws JSONException {
		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler, _sdkMetrics);
		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		JSONArray array = new JSONArray();
		array.put("queue_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(0)).sendMetric(any(Metric.class));

		_asyncTokenStorage.setConfiguration(null);

		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(0)).sendMetric(any(Metric.class));
	}

	@Test
	public void testGetTokenWhenQueueEmptyWaitForNextToken() throws JSONException {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});
		JSONArray array = new JSONArray();
		array.put("create_token_1");
		TokenStorage.createTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		assertEquals("create_token_1", TokenStorage.getToken());

		_asyncTokenStorage.getToken(_listener);

		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());
		Mockito.verify(_listener, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(0)).sendMetric(any(Metric.class));

		array = new JSONArray();
		array.put("append_token_1");
		TokenStorage.appendTokens(array);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady("append_token_1");
		Mockito.verify(_listener, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	private class MockWebViewApp extends WebViewApp {
		@Override
		public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
			return true;
		}
	}
}
