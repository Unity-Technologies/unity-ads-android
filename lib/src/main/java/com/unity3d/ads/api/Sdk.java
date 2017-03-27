package com.unity3d.ads.api;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.configuration.InitializeThread;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

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
			WebViewApp.getCurrentApp().getConfiguration().getWebViewVersion(),
			SdkProperties.getInitializationTime(),
			SdkProperties.isReinitialized()
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
		SdkProperties.setReinitialized(true);
		InitializeThread.initialize(WebViewApp.getCurrentApp().getConfiguration());

		// Callback will not be invoked because reinitialize will destroy webview
	}
}