package com.unity3d.services.ads.token;

import static com.unity3d.services.core.device.TokenType.TOKEN_REMOTE;
import static com.unity3d.services.core.device.TokenType.TOKEN_NATIVE;

import android.text.TextUtils;

import com.unity3d.services.core.configuration.InitializeEventsMetricSender;
import com.unity3d.services.core.device.TokenType;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TokenStorage {
	private static ConcurrentLinkedQueue<String> _queue;
	private static int _accessCounter = 0;
	private static boolean _peekMode = false;
	private static String _initToken = null;

	public synchronized static void createTokens(JSONArray tokens) throws JSONException {
		_queue = new ConcurrentLinkedQueue<String>();
		_accessCounter = 0;

		for(int i = 0; i < tokens.length(); i++) {
			_queue.add(tokens.getString(i));
		}

		if (!_queue.isEmpty()) {
			triggerTokenAvailable();
			AsyncTokenStorage.getInstance().onTokenAvailable(TOKEN_REMOTE);
		}
	}

	public synchronized static void appendTokens(JSONArray tokens) throws JSONException {
		if(_queue == null) {
			_queue = new ConcurrentLinkedQueue<String>();
			_accessCounter = 0;
		}

		for(int i = 0; i < tokens.length(); i++) {
			_queue.add(tokens.getString(i));
		}

		if (!_queue.isEmpty()) {
			triggerTokenAvailable();
			AsyncTokenStorage.getInstance().onTokenAvailable(TOKEN_REMOTE);
		}
	}

	public synchronized static void deleteTokens() {
		_queue = null;
		_accessCounter = 0;
	}

	public synchronized static String getToken() {
		if(_queue == null) {
			return _initToken;
		}

		if(_queue.isEmpty()) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.TOKEN, TokenEvent.QUEUE_EMPTY);
			return null;
		} else if(_peekMode) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.TOKEN, TokenEvent.TOKEN_ACCESS, _accessCounter++);
			return _queue.peek();
		} else {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.TOKEN, TokenEvent.TOKEN_ACCESS, _accessCounter++);
			return _queue.poll();
		}
	}

	public synchronized static void setPeekMode(boolean mode) {
		_peekMode = mode;
	}

	public synchronized static void setInitToken(String value) {
		_initToken = value;

		if (_initToken != null) {
			AsyncTokenStorage.getInstance().onTokenAvailable(TOKEN_REMOTE);
			triggerTokenAvailable();
		}
	}

	private static void triggerTokenAvailable() {
		if (!TextUtils.isEmpty(_initToken)) {
			InitializeEventsMetricSender.getInstance().sdkTokenDidBecomeAvailableWithConfig(true);
		} else if (_queue != null && _queue.size() > 0) {
			InitializeEventsMetricSender.getInstance().sdkTokenDidBecomeAvailableWithConfig(false);
		}
	}

}
