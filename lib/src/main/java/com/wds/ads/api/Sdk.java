package com.wds.ads.api;

import com.wds.ads.UnityAds;
import com.wds.ads.configuration.InitializeThread;
import com.wds.ads.log.DeviceLog;
import com.wds.ads.properties.ClientProperties;
import com.wds.ads.properties.SdkProperties;
import com.wds.ads.webview.WebViewApp;
import com.wds.ads.webview.bridge.WebViewCallback;
import com.wds.ads.webview.bridge.WebViewExposed;

public class Sdk {
	@WebViewExposed
	public static void loadComplete(WebViewCallback callback) {
		DeviceLog.debug("Web Application loaded");
		WebViewApp.getCurrentApp().setWebAppLoaded(true);

		Object[] parameters = new Object[] {
			ClientProperties.getGameId(),
			SdkProperties.isTestMode(),
			ClientProperties.getAppName(),
			ClientProperties.getAppVersion(),
			SdkProperties.getVersionCode(),
			SdkProperties.getVersionName(),
			ClientProperties.isAppDebuggable(),
			WebViewApp.getCurrentApp().getConfiguration().getConfigUrl(),
			WebViewApp.getCurrentApp().getConfiguration().getWebViewUrl(),
			WebViewApp.getCurrentApp().getConfiguration().getWebViewHash(),
			WebViewApp.getCurrentApp().getConfiguration().getWebViewVersion()
		};

		callback.invoke(parameters);
	}

	@WebViewExposed
	public static void initComplete(WebViewCallback callback) {
		DeviceLog.debug("Web Application initialized");
		SdkProperties.setInitialized(true);
		WebViewApp.getCurrentApp().setWebAppInitialized(true);
		callback.invoke();
	}

	@WebViewExposed
	public static void setDebugMode(Boolean debugMode, WebViewCallback callback) {
		UnityAds.setDebugMode(debugMode);
		callback.invoke();
	}

	@WebViewExposed
	public static void getDebugMode(WebViewCallback callback) {
		callback.invoke(Boolean.valueOf(UnityAds.getDebugMode()));
	}

	@WebViewExposed
	public static void logError(String message, WebViewCallback callback) {
		DeviceLog.error(message);
		callback.invoke();
	}

	@WebViewExposed
	public static void logWarning(String message, WebViewCallback callback) {
		DeviceLog.warning(message);
		callback.invoke();
	}

	@WebViewExposed
	public static void logInfo(String message, WebViewCallback callback) {
		DeviceLog.info(message);
		callback.invoke();
	}

	@WebViewExposed
	public static void logDebug(String message, WebViewCallback callback) {
		DeviceLog.debug(message);
		callback.invoke();
	}

	@WebViewExposed
	public static void setShowTimeout(Integer timeout, WebViewCallback callback) {
		SdkProperties.setShowTimeout(timeout);
		callback.invoke();
	}

	@WebViewExposed
	public static void reinitialize (WebViewCallback callback) {
		InitializeThread.initialize(WebViewApp.getCurrentApp().getConfiguration());

		// Callback will not be invoked because reinitialize will destroy webview
	}
}