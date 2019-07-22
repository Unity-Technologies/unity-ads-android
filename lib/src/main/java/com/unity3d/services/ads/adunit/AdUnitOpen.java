package com.unity3d.services.ads.adunit;

import android.os.ConditionVariable;

import com.unity3d.services.ads.properties.AdsProperties;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.CallbackStatus;

import org.json.JSONObject;

import java.lang.reflect.Method;

public class AdUnitOpen {
	private static ConditionVariable _waitShowStatus;

	public static synchronized boolean open(String placementId, JSONObject options) throws NoSuchMethodException {
		Method showCallback = AdUnitOpen.class.getMethod("showCallback", CallbackStatus.class);
		_waitShowStatus = new ConditionVariable();
		WebViewApp.getCurrentApp().invokeMethod("webview", "show", showCallback, placementId, options);
		boolean success = _waitShowStatus.block(AdsProperties.getShowTimeout());
		_waitShowStatus = null;
		return success;
	}

	public static void showCallback(CallbackStatus status) {
		if (_waitShowStatus != null && status.equals(CallbackStatus.OK)) {
			_waitShowStatus.open();
		}
	}
}