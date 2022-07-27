package com.unity3d.services.ads.api;

import com.unity3d.services.ads.token.TokenError;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;

public class Token {
	@WebViewExposed
	public static void createTokens(JSONArray tokens, WebViewCallback callback) {
		try {
			com.unity3d.services.ads.token.TokenStorage.createTokens(tokens);
		} catch (JSONException e) {
			callback.error(TokenError.JSON_EXCEPTION, e.getMessage());
			return;
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void appendTokens(JSONArray tokens, WebViewCallback callback) {
		try {
			com.unity3d.services.ads.token.TokenStorage.appendTokens(tokens);
		} catch (JSONException e) {
			callback.error(TokenError.JSON_EXCEPTION, e.getMessage());
			return;
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void deleteTokens(WebViewCallback callback) {
		com.unity3d.services.ads.token.TokenStorage.deleteTokens();

		callback.invoke();
	}

	@WebViewExposed
	public static void setPeekMode(Boolean mode, WebViewCallback callback) {
		com.unity3d.services.ads.token.TokenStorage.setPeekMode(mode);

		callback.invoke();
	}

	@WebViewExposed
	public static void getNativeGeneratedToken(WebViewCallback callback) {
		com.unity3d.services.ads.token.TokenStorage.getNativeGeneratedToken();

		callback.invoke();
	}
}
