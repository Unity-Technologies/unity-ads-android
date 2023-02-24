package com.unity3d.services.ads.api;

import com.unity3d.services.ads.token.TokenError;
import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;

public class Token {
	@WebViewExposed
	public static void createTokens(JSONArray tokens, WebViewCallback callback) {
		try {
			TokenStorage.getInstance().createTokens(tokens);
		} catch (JSONException e) {
			callback.error(TokenError.JSON_EXCEPTION, e.getMessage());
			return;
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void appendTokens(JSONArray tokens, WebViewCallback callback) {
		try {
			TokenStorage.getInstance().appendTokens(tokens);
		} catch (JSONException e) {
			callback.error(TokenError.JSON_EXCEPTION, e.getMessage());
			return;
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void deleteTokens(WebViewCallback callback) {
		TokenStorage.getInstance().deleteTokens();

		callback.invoke();
	}

	@WebViewExposed
	public static void setPeekMode(Boolean mode, WebViewCallback callback) {
		TokenStorage.getInstance().setPeekMode(mode);

		callback.invoke();
	}

	@WebViewExposed
	public static void getNativeGeneratedToken(WebViewCallback callback) {
		TokenStorage.getInstance().getNativeGeneratedToken();

		callback.invoke();
	}
}
