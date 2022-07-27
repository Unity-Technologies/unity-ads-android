package com.unity3d.services.ads.adunit;

import android.os.ConditionVariable;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.request.metrics.AdOperationError;
import com.unity3d.services.core.request.metrics.AdOperationMetric;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.CallbackStatus;

import org.json.JSONObject;

import java.lang.reflect.Method;

public class AdUnitOpen {
	private static ConditionVariable _waitShowStatus;
	private static Configuration _configuration;

	public static synchronized boolean open(String placementId, JSONObject options) throws NoSuchMethodException {
		Method showCallback = AdUnitOpen.class.getMethod("showCallback", CallbackStatus.class);
		_waitShowStatus = new ConditionVariable();
		if (_configuration == null) {
			_configuration = new Configuration();
		}
		WebViewApp.getCurrentApp().invokeMethod("webview", "show", showCallback, placementId, options);
		boolean success = _waitShowStatus.block(_configuration.getShowTimeout());
		_waitShowStatus = null;
		if (!success) {
			SDKMetrics.getInstance().sendMetric(AdOperationMetric.newAdShowFailure(AdOperationError.timeout, Long.valueOf(_configuration.getShowTimeout())));
		}
		return success;
	}

	public static void showCallback(CallbackStatus status) {
		if (_waitShowStatus != null && status.equals(CallbackStatus.OK)) {
			_waitShowStatus.open();
		}
	}

	public static void setConfiguration (Configuration configuration) {
		_configuration = configuration;
	}
}