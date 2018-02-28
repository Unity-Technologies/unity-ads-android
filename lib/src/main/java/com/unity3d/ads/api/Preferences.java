package com.unity3d.ads.api;

import com.unity3d.ads.preferences.AndroidPreferences;
import com.unity3d.ads.preferences.PreferencesError;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

public class Preferences {
	@WebViewExposed
	public static void hasKey(String name, String key, WebViewCallback callback) {
		callback.invoke(Boolean.valueOf(AndroidPreferences.hasKey(name, key)));
	}

	@WebViewExposed
	public static void getString(String name, String key, WebViewCallback callback) {
		String value = AndroidPreferences.getString(name, key);

		if(value != null) {
			callback.invoke(value);
		} else {
			callback.error(PreferencesError.COULDNT_GET_VALUE, name, key);
		}
	}

	@WebViewExposed
	public static void getInt(String name, String key, WebViewCallback callback) {
		Integer value = AndroidPreferences.getInteger(name, key);

		if(value != null) {
			callback.invoke(value);
		} else {
			callback.error(PreferencesError.COULDNT_GET_VALUE, name, key);
		}
	}

	@WebViewExposed
	public static void getLong(String name, String key, WebViewCallback callback) {
		Long value = AndroidPreferences.getLong(name, key);

		if(value != null) {
			callback.invoke(value);
		} else {
			callback.error(PreferencesError.COULDNT_GET_VALUE, name, key);
		}
	}

	@WebViewExposed
	public static void getBoolean(String name, String key, WebViewCallback callback) {
		Boolean value = AndroidPreferences.getBoolean(name, key);

		if(value != null) {
			callback.invoke(value);
		} else {
			callback.error(PreferencesError.COULDNT_GET_VALUE, name, key);
		}
	}

	@WebViewExposed
	public static void getFloat(String name, String key, WebViewCallback callback) {
		Float value = AndroidPreferences.getFloat(name, key);

		if(value != null) {
			callback.invoke(value);
		} else {
			callback.error(PreferencesError.COULDNT_GET_VALUE, name, key);
		}
	}

	@WebViewExposed
	public static void setString(String name, String key, String value, WebViewCallback callback) {
		AndroidPreferences.setString(name, key, value);
		callback.invoke();
	}

	@WebViewExposed
	public static void setInt(String name, String key, Integer value, WebViewCallback callback) {
		AndroidPreferences.setInteger(name, key, value);
		callback.invoke();
	}

	@WebViewExposed
	public static void setLong(String name, String key, Long value, WebViewCallback callback) {
		AndroidPreferences.setLong(name, key, value);
		callback.invoke();
	}

	@WebViewExposed
	public static void setBoolean(String name, String key, Boolean value, WebViewCallback callback) {
		AndroidPreferences.setBoolean(name, key, value);
		callback.invoke();
	}

	@WebViewExposed
	public static void setFloat(String name, String key, Double value, WebViewCallback callback) {
		AndroidPreferences.setFloat(name, key, value);
		callback.invoke();
	}

	@WebViewExposed
	public static void removeKey(String name, String key, WebViewCallback callback) {
		AndroidPreferences.removeKey(name, key);
		callback.invoke();
	}
}
