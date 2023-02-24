package com.unity3d.ads.test.instrumentation.services.core.device;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import android.os.Handler;
import android.os.Message;

import com.unity3d.ads.IUnityAdsTokenListener;
import com.unity3d.services.ads.gmascar.managers.IBiddingManager;
import com.unity3d.services.ads.token.AsyncTokenStorage;
import com.unity3d.services.ads.token.INativeTokenGenerator;
import com.unity3d.services.ads.token.INativeTokenGeneratorListener;
import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.metrics.ISDKMetrics;
import com.unity3d.services.core.request.metrics.Metric;
import com.unity3d.services.core.request.metrics.TSIMetric;
import com.unity3d.services.core.webview.WebViewApp;

import org.json.JSONException;
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
	IBiddingManager _biddingManager;
	@Mock
	ISDKMetrics _sdkMetrics;
	@Mock
	TokenStorage _tokenStorage;

	AsyncTokenStorage _asyncTokenStorage;
	Runnable timeoutRunnable;
	List<Runnable> handlerRunnable = new ArrayList<>();
	ArgumentCaptor<Metric> _metricsCaptor = ArgumentCaptor.forClass(Metric.class);
	private static final String SCAR_TOKEN_IDENTIFIER_MOCK = "scarTokenIdentifier";

	@Before
	public void Before() {
		ClientProperties.setApplicationContext(androidx.test.core.app.ApplicationProvider.getApplicationContext());
		MockWebViewApp webViewApp = new MockWebViewApp();
		WebViewApp.setCurrentApp(webViewApp);
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);

		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler, _sdkMetrics, _tokenStorage);
		_asyncTokenStorage.setConfiguration(new Configuration());

		Mockito.when(_biddingManager.getTokenIdentifier()).thenReturn(SCAR_TOKEN_IDENTIFIER_MOCK);

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

		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenBeforeInit() {
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());
	}

	@Test
	public void testGetTokenBeforeInitAndTimeout() {
		Metric _metric = TSIMetric.newNativeGeneratedTokenNull(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_biddingManager);

		timeoutRunnable.run();

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenBeforeInitAndTimeoutGuard() {
		Metric _metric = TSIMetric.newNativeGeneratedTokenNull(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_biddingManager);

		timeoutRunnable.run();
		timeoutRunnable.run();
		timeoutRunnable.run();

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(3)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenBeforeInitAndInitTimeTokenReady() {
		Metric _metric = TSIMetric.newNativeGeneratedTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_biddingManager);

		when(_tokenStorage.getToken())
			.thenReturn("init_time_token")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		// token will be generated by native flow, SCAR token identifier will be in the payload and
		// not prepended.
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady("init_time_token");
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenBeforeInitAndCreateEmptyQueue() throws JSONException {
		_asyncTokenStorage.getToken(_biddingManager);

		when(_tokenStorage.getToken()).thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(0)).sendMetric(any(Metric.class));
	}

	@Test
	public void testGetTokenBeforeInitAndCreateQueueTokenReady() {
		Metric _metric = TSIMetric.newNativeGeneratedTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_biddingManager);

		when(_tokenStorage.getToken()).thenReturn("queue_token_1")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		// token will be generated by native flow, SCAR token identifier will be in the payload and
		// not prepended.
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenAfterInitTokenReady() {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		when(_tokenStorage.getToken()).thenReturn("init_time_token")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.when(_biddingManager.getFormattedToken(anyString())).thenReturn(SCAR_TOKEN_IDENTIFIER_MOCK + ":init_time_token");
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		// token is remote, SCAR token identifier will prepended.
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(SCAR_TOKEN_IDENTIFIER_MOCK+":init_time_token");
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenAfterQueueTokenReady() {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		when(_tokenStorage.getToken()).thenReturn("queue_token_1")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.when(_biddingManager.getFormattedToken(anyString())).thenReturn(SCAR_TOKEN_IDENTIFIER_MOCK + ":queue_token_1");
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(SCAR_TOKEN_IDENTIFIER_MOCK+":queue_token_1");
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testMultipleCallsBeforeInit() {
		Metric _metric = TSIMetric.newNativeGeneratedTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_biddingManager);
		_asyncTokenStorage.getToken(_biddingManager);
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_handler, times(3)).sendMessageAtTime(any(Message.class), anyLong());

		when(_tokenStorage.getToken()).thenReturn("init_time_token");
		_asyncTokenStorage.onTokenAvailable();

		// All tokens will be native. Scar will be in the payload
		Mockito.verify(_nativeTokenGenerator, Mockito.times(3)).generateToken(any(INativeTokenGeneratorListener.class));
		Mockito.verify(_biddingManager, Mockito.times(3)).onUnityAdsTokenReady("init_time_token");
		Mockito.verify(_biddingManager, Mockito.times(3)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(3)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testMultipleCallsBeforeInitAndQueue() {
		Metric _metric = TSIMetric.newNativeGeneratedTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_biddingManager);
		_asyncTokenStorage.getToken(_biddingManager);
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_handler, times(3)).sendMessageAtTime(any(Message.class), anyLong());

		when(_tokenStorage.getToken())
			.thenReturn("queue_token_1", "queue_token_2", "queue_token_3", null);
		_asyncTokenStorage.onTokenAvailable();

		InOrder order = Mockito.inOrder(_biddingManager);
		Mockito.verify(_nativeTokenGenerator, Mockito.times(3)).generateToken(any(INativeTokenGeneratorListener.class));
		order.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady("queue_token_1");
		order.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady("queue_token_2");
		order.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady("queue_token_3");
		Mockito.verify(_biddingManager, Mockito.times(3)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(3)).sendMetric(_metricsCaptor.capture());
		final Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testMultipleCallsBeforeInitAndSingleAfterInit() {
		Metric _metric = TSIMetric.newNativeGeneratedTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});

		_asyncTokenStorage.getToken(_biddingManager);
		_asyncTokenStorage.getToken(_biddingManager);
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_handler, times(3)).sendMessageAtTime(any(Message.class), anyLong());

		when(_tokenStorage.getToken())
			.thenReturn("queue_token_1", "queue_token_2", null);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_nativeTokenGenerator, Mockito.times(3)).generateToken(any(INativeTokenGeneratorListener.class));
		Mockito.verify(_biddingManager, Mockito.times(2)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(2)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);

		when(_tokenStorage.getToken())
			.thenReturn("queue_token_3")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady("queue_token_3");
		Mockito.verify(_biddingManager, Mockito.times(3)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(3)).sendMetric(_metricsCaptor.capture());
		capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenFailedInit() {
		Metric _metric = TSIMetric.newAsyncTokenNull(new HashMap<String, String>(){{
			put ("state", "initialized_failed");
		}});

		when(_tokenStorage.getToken())
			.thenReturn("queue_token_1")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_FAILED);

		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_handler, times(0)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(null);
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testGetTokenFailedInitCancelAllRequests() {
		_asyncTokenStorage.getToken(_biddingManager);
		_asyncTokenStorage.getToken(_biddingManager);

		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_FAILED);

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(0)).sendMetric(any(Metric.class));
	}

	@Test
	public void testNativeToken() throws JSONException {
		Metric _metric = TSIMetric.newNativeGeneratedTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});
		Configuration configuration = Mockito.mock(Configuration.class);

		_asyncTokenStorage.setConfiguration(configuration);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				invocation.getArgument(0, INativeTokenGeneratorListener.class).onReady("native_token");
				return null;
			}}).when(_nativeTokenGenerator).generateToken(any(INativeTokenGeneratorListener.class));

		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_handler, times(2)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		handlerRunnable.get(1).run();

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady("native_token");
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
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

		_asyncTokenStorage.setConfiguration(configuration);

		when(_tokenStorage.getToken())
			.thenReturn("queue_token_1")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.when(_biddingManager.getFormattedToken(anyString())).thenReturn(SCAR_TOKEN_IDENTIFIER_MOCK + ":queue_token_1");
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_nativeTokenGenerator, times(0)).generateToken(any(INativeTokenGeneratorListener.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(SCAR_TOKEN_IDENTIFIER_MOCK+":queue_token_1");
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testDelayedConfiguration() {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});
		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler, _sdkMetrics, _tokenStorage);
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		when(_tokenStorage.getToken())
			.thenReturn("queue_token_1")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		_asyncTokenStorage.setConfiguration(new Configuration());

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testDelayedConfigurationTwoConfigSets() {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});
		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler, _sdkMetrics, _tokenStorage);
		Mockito.when(_biddingManager.getFormattedToken(anyString())).thenReturn(SCAR_TOKEN_IDENTIFIER_MOCK + ":queue_token_1");
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		when(_tokenStorage.getToken())
			.thenReturn("queue_token_1")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		_asyncTokenStorage.setConfiguration(new Configuration());
		_asyncTokenStorage.setConfiguration(new Configuration());

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(SCAR_TOKEN_IDENTIFIER_MOCK+":queue_token_1");
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testDelayedConfigurationConfigurationArrivedFirst() {
		Metric _metric = TSIMetric.newNativeGeneratedTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		_asyncTokenStorage.setConfiguration(new Configuration());

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));

		when(_tokenStorage.getToken())
			.thenReturn("init_token")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady("init_token");
		Mockito.verify(_sdkMetrics, Mockito.times(1)).sendMetric(_metricsCaptor.capture());
		Metric capturedMetric = _metricsCaptor.getValue();
		Assert.assertEquals(_metric, capturedMetric);
	}

	@Test
	public void testNullConfiguration() throws JSONException {
		_asyncTokenStorage = new AsyncTokenStorage(_nativeTokenGenerator, _handler, _sdkMetrics, _tokenStorage);
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());

		when(_tokenStorage.getToken())
			.thenReturn("token_1")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(0)).sendMetric(any(Metric.class));

		_asyncTokenStorage.setConfiguration(null);

		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(0)).sendMetric(any(Metric.class));
	}

	@Test
	public void testGetTokenWhenQueueEmptyWaitForNextToken() {
		Metric _metric = TSIMetric.newAsyncTokenAvailable(new HashMap<String, String>(){{
			put ("state", "initializing");
		}});
		when(_tokenStorage.getToken())
			.thenReturn("create_token_1")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		assertEquals("create_token_1", _tokenStorage.getToken());
		Mockito.when(_biddingManager.getFormattedToken(anyString())).thenReturn(SCAR_TOKEN_IDENTIFIER_MOCK + ":append_token_1");
		_asyncTokenStorage.getToken(_biddingManager);

		Mockito.verify(_handler, times(1)).sendMessageAtTime(any(Message.class), anyLong());
		Mockito.verify(_biddingManager, Mockito.times(0)).onUnityAdsTokenReady(nullable(String.class));
		Mockito.verify(_sdkMetrics, Mockito.times(0)).sendMetric(any(Metric.class));

		when(_tokenStorage.getToken())
			.thenReturn("append_token_1")
			.thenReturn(null);
		_asyncTokenStorage.onTokenAvailable();

		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(SCAR_TOKEN_IDENTIFIER_MOCK+":append_token_1");
		Mockito.verify(_biddingManager, Mockito.times(1)).onUnityAdsTokenReady(nullable(String.class));
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