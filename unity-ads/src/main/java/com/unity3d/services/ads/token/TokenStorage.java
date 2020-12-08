package com.unity3d.services.ads.token;

import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TokenStorage {
	private static ConcurrentLinkedQueue<String> _queue;
	private static int _accessCounter = 0;
	private static boolean _peekMode = false;

	public synchronized static void createTokens(JSONArray tokens) throws JSONException {
		_queue = new ConcurrentLinkedQueue<String>();
		_accessCounter = 0;

		for(int i = 0; i < tokens.length(); i++) {
			_queue.add(tokens.getString(i));
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
	}

	public synchronized static void deleteTokens() {
		_queue = null;
		_accessCounter = 0;
	}

	public synchronized static String getToken() {
		if(_queue == null) {
			return null;
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
}
