package com.unity3d.services.ads.token;

import static com.unity3d.services.core.device.TokenType.TOKEN_NATIVE;
import static com.unity3d.services.core.device.TokenType.TOKEN_REMOTE;

import android.os.Handler;
import android.os.Looper;

import com.unity3d.ads.IUnityAdsTokenListener;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.TokenType;
import com.unity3d.services.core.device.reader.DeviceInfoReaderBuilder;
import com.unity3d.services.core.device.reader.GameSessionIdReader;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.InitializationStatusReader;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.metrics.ISDKMetrics;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.TSIMetric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncTokenStorage {
	private final List<AsyncTokenStorage.TokenListenerState> _tokenListeners = new LinkedList<>();
	private final Handler _handler;
	private boolean _tokenAvailable = false;
	private boolean _configurationWasSet = false;
	private Configuration _configuration = new Configuration();
	private INativeTokenGenerator _nativeTokenGenerator;
	private final InitializationStatusReader  _initStatusReader = new InitializationStatusReader();
	private final ISDKMetrics _sdkMetrics;

	private static AsyncTokenStorage _instance;

	public static AsyncTokenStorage getInstance() {
		if (_instance == null) {
			_instance = new AsyncTokenStorage(
				null,
				new Handler(Looper.getMainLooper()),
				SDKMetrics.getInstance());
		}
		return _instance;
	}

	class TokenListenerState
	{
		public IUnityAdsTokenListener listener;
		public Runnable runnable;
		public boolean invoked;
		public TokenType tokenType;
	}

	public AsyncTokenStorage(INativeTokenGenerator nativeTokenGenerator, Handler handler, ISDKMetrics sdkMetrics) {
		_handler = handler;
		_nativeTokenGenerator = nativeTokenGenerator;
		_sdkMetrics = sdkMetrics;
	}

	public synchronized void setConfiguration(Configuration configuration) {
		_configuration = configuration;
		_configurationWasSet = isValidConfig(_configuration);

		if (!_configurationWasSet) {
			return;
		}

		if (_nativeTokenGenerator == null) {
			DeviceInfoReaderBuilder deviceInfoReaderBuilder = new DeviceInfoReaderBuilder(new ConfigurationReader(), PrivacyConfigStorage.getInstance(), GameSessionIdReader.getInstance());
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			_nativeTokenGenerator = new NativeTokenGenerator(executorService, deviceInfoReaderBuilder);
			if (configuration.getExperiments().shouldNativeTokenAwaitPrivacy()) {
				_nativeTokenGenerator = new NativeTokenGeneratorWithPrivacyAwait(executorService, _nativeTokenGenerator, configuration.getPrivacyRequestWaitTimeout());
			}
		}

		ArrayList<TokenListenerState> tempList = new ArrayList<>(_tokenListeners);
		for(TokenListenerState state: tempList) {
			handleTokenInvocation(state);
		}
	}

	public synchronized void onTokenAvailable() {
		_tokenAvailable = true;

		if (!_configurationWasSet) {
			return;
		}

		notifyListenersTokenReady();
	}

	public synchronized void getToken(IUnityAdsTokenListener listener) {
		if (SdkProperties.getCurrentInitializationState() == SdkProperties.InitializationState.INITIALIZED_FAILED) {
			listener.onUnityAdsTokenReady(null);
			sendTokenMetrics(null, TOKEN_REMOTE);
			return;
		}

		if (SdkProperties.getCurrentInitializationState() == SdkProperties.InitializationState.NOT_INITIALIZED) {
			listener.onUnityAdsTokenReady(null);
			sendTokenMetrics(null, TOKEN_REMOTE);
			return;
		}

		final AsyncTokenStorage.TokenListenerState state = addTimeoutHandler(listener);

		if (!_configurationWasSet) {
			return;
		}

		handleTokenInvocation(state);
	}

	private synchronized AsyncTokenStorage.TokenListenerState addTimeoutHandler(IUnityAdsTokenListener listener) {
		final AsyncTokenStorage.TokenListenerState state = new AsyncTokenStorage.TokenListenerState();
		state.listener = listener;
		state.tokenType = TOKEN_REMOTE;
		state.runnable = new Runnable() {
			@Override
			public void run() {
				notifyTokenReady(state, null);
			}
		};

		_tokenListeners.add(state);
		_handler.postDelayed(state.runnable, _configuration.getTokenTimeout());

		return state;
	}

	private synchronized void notifyListenersTokenReady() {
		while(!_tokenListeners.isEmpty()) {
			String token = TokenStorage.getToken();

			if (token == null) {
				break;
			}

			notifyTokenReady(_tokenListeners.get(0), token);
		}
	}

	private void handleTokenInvocation(final AsyncTokenStorage.TokenListenerState state) {
		if (state.invoked) {
			return;
		}
		state.invoked = true;

		if (!_tokenAvailable && _configuration.getExperiments().isNativeTokenEnabled()) {
			state.tokenType = TOKEN_NATIVE;
			_nativeTokenGenerator.generateToken(new INativeTokenGeneratorListener() {
				@Override
				public void onReady(final String token) {
					_handler.post(new Runnable() {
						@Override
						public void run() {
							notifyTokenReady(state, token);
						}
					});
				}
			});
		} else {
			state.tokenType = TOKEN_REMOTE;
			String token = TokenStorage.getToken();

			if (token == null || token.isEmpty()) {
				return;
			}

			notifyTokenReady(state, token);
		}
	}

	private synchronized void notifyTokenReady(AsyncTokenStorage.TokenListenerState state, String token) {
		if (_tokenListeners.remove(state)) {
			state.listener.onUnityAdsTokenReady(token);
			try {
				_handler.removeCallbacks(state.runnable);
			} catch (Exception ex) {
				DeviceLog.exception("Failed to remove callback from a handler", ex);
			}
		}
		sendTokenMetrics(token, state.tokenType);
	}

	private void sendTokenMetrics(String token, TokenType type) {
		switch (type) {
			case TOKEN_NATIVE:
				sendNativeTokenMetrics(token);
				break;
			case TOKEN_REMOTE:
				sendRemoteTokenMetrics(token);
				break;
			default:
				DeviceLog.error("Unknown token type passed to sendTokenMetrics");
		}
	}

	private void sendNativeTokenMetrics(String token) {
		if (_sdkMetrics == null) return;
		if (token == null) {
			_sdkMetrics.sendMetric(TSIMetric.newNativeGeneratedTokenNull(getMetricTags()));
		} else {
			_sdkMetrics.sendMetric(TSIMetric.newNativeGeneratedTokenAvailable(getMetricTags()));
		}
	}

	private void sendRemoteTokenMetrics(String token) {
		if (_sdkMetrics == null) return;
		if (token == null || token.isEmpty()) {
			_sdkMetrics.sendMetric(TSIMetric.newAsyncTokenNull(getMetricTags()));
		} else {
			_sdkMetrics.sendMetric(TSIMetric.newAsyncTokenAvailable(getMetricTags()));
		}
	}

	private Map<String, String> getMetricTags() {
		Map<String, String> tags = new HashMap<>();
		tags.put("state", _initStatusReader.getInitializationStateString(SdkProperties.getCurrentInitializationState()));
		return tags;
	}

	private boolean isValidConfig(Configuration configuration) {
		return configuration != null && configuration.getExperiments() != null;
	}
}
