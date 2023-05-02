package com.unity3d.services.ads.api;

import com.unity3d.services.ads.token.TokenError;
import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;

public class Token {
	private static final TokenStorage tokenStorage = Utilities.getService(TokenStorage.class);

	@WebViewExposed
	public static void createTokens(JSONArray tokens, WebViewCallback callback) {
		try {
			tokenStorage.createTokens(tokens);
		} catch (JSONException e) {
			callback.error(TokenError.JSON_EXCEPTION, e.getMessage());
			return;
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void appendTokens(JSONArray tokens, WebViewCallback callback) {
		try {
			tokenStorage.appendTokens(tokens);
		} catch (JSONException e) {
			callback.error(TokenError.JSON_EXCEPTION, e.getMessage());
			return;
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void deleteTokens(WebViewCallback callback) {
		tokenStorage.deleteTokens();

		callback.invoke();
	}

	@WebViewExposed
	public static void getNativeGeneratedToken(WebViewCallback callback) {
		tokenStorage.getNativeGeneratedToken();

		callback.invoke();
	}
}
