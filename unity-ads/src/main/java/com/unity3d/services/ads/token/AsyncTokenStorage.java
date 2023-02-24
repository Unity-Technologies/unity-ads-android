package com.unity3d.services.ads.token;

import static com.unity3d.services.core.device.TokenType.TOKEN_NATIVE;
import static com.unity3d.services.core.device.TokenType.TOKEN_REMOTE;

import android.os.Handler;
import android.os.Looper;

import com.unity3d.services.ads.gmascar.GMA;
import com.unity3d.services.ads.gmascar.managers.IBiddingManager;
import com.unity3d.services.ads.gmascar.utils.ScarConstants;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.TokenType;
import com.unity3d.services.core.device.reader.DeviceInfoReaderBuilderWithExtras;
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
	private DeviceInfoReaderBuilderWithExtras _deviceInfoReaderBuilderWithExtras;
	private TokenStorage _tokenStorage;

	private static AsyncTokenStorage _instance;

	public static AsyncTokenStorage getInstance() {
		if (_instance == null) {
			_instance = new AsyncTokenStorage(
				null,
				new Handler(Looper.getMainLooper()),
				SDKMetrics.getInstance(),
				TokenStorage.getInstance());
		}
		return _instance;
	}

	class TokenListenerState
	{
		public IBiddingManager biddingManager;
		public Runnable runnable;
		public boolean invoked;
		public TokenType tokenType;
	}

	public AsyncTokenStorage(INativeTokenGenerator nativeTokenGenerator, Handler handler, ISDKMetrics sdkMetrics, TokenStorage tokenStorage) {
		_handler = handler;
		_nativeTokenGenerator = nativeTokenGenerator;
		_sdkMetrics = sdkMetrics;
		_tokenStorage = tokenStorage;
	}

	public synchronized void setConfiguration(Configuration configuration) {
		_configuration = configuration;
		_configurationWasSet = isValidConfig(_configuration);

		if (!_configurationWasSet) {
			return;
		}

		if (_nativeTokenGenerator == null) {
			_deviceInfoReaderBuilderWithExtras = new DeviceInfoReaderBuilderWithExtras(new ConfigurationReader(), PrivacyConfigStorage.getInstance(), GameSessionIdReader.getInstance());

			ExecutorService executorService = Executors.newSingleThreadExecutor();
			_nativeTokenGenerator = new NativeTokenGenerator(executorService, _deviceInfoReaderBuilderWithExtras);

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

	public synchronized void getToken(IBiddingManager biddingManager) {
		if (SdkProperties.getCurrentInitializationState() == SdkProperties.InitializationState.INITIALIZED_FAILED) {
			biddingManager.onUnityAdsTokenReady(null);
			sendTokenMetrics(null, TOKEN_REMOTE);
			return;
		}

		if (SdkProperties.getCurrentInitializationState() == SdkProperties.InitializationState.NOT_INITIALIZED) {
			biddingManager.onUnityAdsTokenReady(null);
			sendTokenMetrics(null, TOKEN_REMOTE);
			return;
		}

		final AsyncTokenStorage.TokenListenerState state = addTimeoutHandler(biddingManager);

		if (!_configurationWasSet) {
			return;
		}

		handleTokenInvocation(state);
	}

	private synchronized AsyncTokenStorage.TokenListenerState addTimeoutHandler(IBiddingManager biddingManager) {
		final AsyncTokenStorage.TokenListenerState state = new AsyncTokenStorage.TokenListenerState();
		state.biddingManager = biddingManager;
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
			String token = _tokenStorage.getToken();

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

		if (!_tokenAvailable) {
			state.tokenType = TOKEN_NATIVE;

			if (GMA.getInstance().hasSCARBiddingSupport() && _deviceInfoReaderBuilderWithExtras != null) {
				Map<String, String> extras = new HashMap<>();
				extras.put(ScarConstants.TOKEN_ID_KEY, state.biddingManager.getTokenIdentifier());
				_deviceInfoReaderBuilderWithExtras.setExtras(extras);
			}

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
			String token = _tokenStorage.getToken();

			if (token == null || token.isEmpty()) {
				return;
			}

			notifyTokenReady(state, token);
		}
	}

	private synchronized void notifyTokenReady(AsyncTokenStorage.TokenListenerState state, String token) {
		if (_tokenListeners.remove(state)) {

			// If SCAR token identifier exists and token is generated by the webView, then the
			// scar identifier is to be prepended to the token <ScarID>:<unityToken>. Otherwise
			// scarID will be included in the token map<String, String> payload.
			String formattedToken = state.tokenType == TOKEN_REMOTE
				? state.biddingManager.getFormattedToken(token)
				: token;

			state.biddingManager.onUnityAdsTokenReady(formattedToken);

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
		return configuration != null;
	}
}
