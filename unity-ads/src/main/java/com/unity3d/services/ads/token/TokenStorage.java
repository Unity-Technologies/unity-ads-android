package com.unity3d.services.ads.token;

import static com.unity3d.services.core.device.TokenType.TOKEN_REMOTE;

import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.InitializeEventsMetricSender;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.reader.DeviceInfoReaderBuilder;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class TokenStorage {
	private static final Object _lock = new Object();
	private static ConcurrentLinkedQueue<String> _queue;
	private static int _accessCounter = 0;
	private static boolean _peekMode = false;
	private static String _initToken = null;
	private static ExecutorService _executorService = Executors.newSingleThreadExecutor();

	public static void createTokens(JSONArray tokens) throws JSONException {
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
			AsyncTokenStorage.getInstance().onTokenAvailable(TOKEN_REMOTE);
		}
	}

	public static void appendTokens(JSONArray tokens) throws JSONException {
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
			AsyncTokenStorage.getInstance().onTokenAvailable(TOKEN_REMOTE);
		}
	}

	public static void deleteTokens() {
		synchronized (_lock) {
			_queue = null;
			_accessCounter = 0;
		}
	}

	public static String getToken() {
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

	public static void setPeekMode(boolean mode) {
		synchronized (_lock) {
			_peekMode = mode;
		}
	}

	public static void getNativeGeneratedToken() {
		NativeTokenGenerator nativeTokenGenerator = new NativeTokenGenerator(_executorService, new DeviceInfoReaderBuilder(new ConfigurationReader(), PrivacyConfigStorage.getInstance()), null);
		nativeTokenGenerator.generateToken(new INativeTokenGeneratorListener() {
			@Override
			public void onReady(String token) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.TOKEN, TokenEvent.TOKEN_NATIVE_DATA, token);
			}
		});
	}

	public static void setInitToken(String value) {
		boolean shouldTriggerEvent;
		synchronized (_lock) {
			_initToken = value;
			shouldTriggerEvent = _initToken != null;
		}

		if (shouldTriggerEvent) {
			AsyncTokenStorage.getInstance().onTokenAvailable(TOKEN_REMOTE);
			triggerTokenAvailable(true);
		}
	}

	private static void triggerTokenAvailable(Boolean withConfig) {
		InitializeEventsMetricSender.getInstance().sdkTokenDidBecomeAvailableWithConfig(withConfig);
	}

}
