package com.unity3d.services.core.api;

import com.unity3d.services.core.broadcast.BroadcastError;
import com.unity3d.services.core.broadcast.BroadcastMonitor;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;

public class Broadcast {
	@WebViewExposed
	public static void addBroadcastListener(String name, JSONArray actions, WebViewCallback callback) {
		addBroadcastListener(name, null, actions, callback);
	}

	@WebViewExposed
	public static void addBroadcastListener(String name, String dataScheme, JSONArray actions, WebViewCallback callback) {
		try {
			if(actions.length() > 0) {
				String[] parsedActions = new String[actions.length()];
				for (int i = 0; i < actions.length(); i++) {
					parsedActions[i] = actions.getString(i);
				}
				BroadcastMonitor.getInstance().addBroadcastListener(name, dataScheme, parsedActions);
			}
		} catch(JSONException e) {
			callback.error(BroadcastError.JSON_ERROR);
			return;
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void removeBroadcastListener(String name, WebViewCallback callback) {
		BroadcastMonitor.getInstance().removeBroadcastListener(name);
		callback.invoke();
	}

	@WebViewExposed
	public static void removeAllBroadcastListeners(WebViewCallback callback) {
		BroadcastMonitor.getInstance().removeAllBroadcastListeners();
		callback.invoke();
	}
}