package com.unity3d.services.ads.token;


import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.InitializeEventsMetricSender;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.reader.DeviceInfoReaderBuilder;
import com.unity3d.services.core.device.reader.GameSessionIdReader;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TokenStorage {
	private static TokenStorage _instance;
	public static TokenStorage getInstance() {
		if (_instance == null) {
			_instance = new TokenStorage();
		}
		return _instance;
	}

	private final Object _lock = new Object();
	private ConcurrentLinkedQueue<String> _queue;
	private int _accessCounter = 0;
	private boolean _peekMode = false;
	private String _initToken = null;
	private final ExecutorService _executorService = Executors.newSingleThreadExecutor();

	private TokenStorage() {
	}

	public void createTokens(JSONArray tokens) throws JSONException {
		boolean shouldTriggerEvent;
		synchronized (_lock) {
			_queue = new ConcurrentLinkedQueue<>();
			_accessCounter = 0;

			for (int i = 0; i < tokens.length(); i++) {
				_queue.add(tokens.getString(i));
			}

			shouldTriggerEvent = !_queue.isEmpty();
		}

		if (shouldTriggerEvent) {
			triggerTokenAvailable(false);
			AsyncTokenStorage.getInstance().onTokenAvailable();
		}
	}

	public void appendTokens(JSONArray tokens) throws JSONException {
		boolean shouldTriggerEvent;
		synchronized (_lock) {
			if (_queue == null) {
				_queue = new ConcurrentLinkedQueue<>();
				_accessCounter = 0;
			}

			for (int i = 0; i < tokens.length(); i++) {
				_queue.add(tokens.getString(i));
			}

			shouldTriggerEvent = !_queue.isEmpty();
		}

		if (shouldTriggerEvent) {
			triggerTokenAvailable(false);
			AsyncTokenStorage.getInstance().onTokenAvailable();
		}
	}

	public void deleteTokens() {
		synchronized (_lock) {
			_queue = null;
			_accessCounter = 0;
		}
	}

	public String getToken() {
		synchronized (_lock) {
			if (_queue == null) {
				return _initToken;
			}

			if (_queue.isEmpty()) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.TOKEN, TokenEvent.QUEUE_EMPTY);
				return null;
			} else if (_peekMode) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.TOKEN, TokenEvent.TOKEN_ACCESS, _accessCounter++);
				return _queue.peek();
			} else {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.TOKEN, TokenEvent.TOKEN_ACCESS, _accessCounter++);
				return _queue.poll();
			}
		}
	}

	public void setPeekMode(boolean mode) {
		synchronized (_lock) {
			_peekMode = mode;
		}
	}

	public void getNativeGeneratedToken() {
		NativeTokenGenerator nativeTokenGenerator = new NativeTokenGenerator(_executorService, new DeviceInfoReaderBuilder(new ConfigurationReader(), PrivacyConfigStorage.getInstance(), GameSessionIdReader.getInstance()), null);
		nativeTokenGenerator.generateToken(new INativeTokenGeneratorListener() {
			@Override
			public void onReady(String token) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.TOKEN, TokenEvent.TOKEN_NATIVE_DATA, token);
			}
		});
	}

	public void setInitToken(String value) {
		boolean shouldTriggerEvent;
		synchronized (_lock) {
			_initToken = value;
			shouldTriggerEvent = _initToken != null;
		}

		if (shouldTriggerEvent) {
			triggerTokenAvailable(true);
			AsyncTokenStorage.getInstance().onTokenAvailable();
		}
	}

	private void triggerTokenAvailable(Boolean withConfig) {
		InitializeEventsMetricSender.getInstance().sdkTokenDidBecomeAvailableWithConfig(withConfig);
	}

}
