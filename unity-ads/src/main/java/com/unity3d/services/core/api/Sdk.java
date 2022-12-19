package com.unity3d.services.core.api;

import com.unity3d.services.core.configuration.InitializeThread;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

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
			SdkProperties.getConfigUrl(),
			WebViewApp.getCurrentApp().getConfiguration().getWebViewUrl(),
			WebViewApp.getCurrentApp().getConfiguration().getWebViewHash(),
			WebViewApp.getCurrentApp().getConfiguration().getWebViewVersion(),
			SdkProperties.getInitializationTime(),
			SdkProperties.isReinitialized(),
			// perPlacementLoadEnabled is now always true
			true,
			SdkProperties.getLatestConfiguration() != null,
			Device.getElapsedRealtime(),
			WebViewApp.getCurrentApp().getConfiguration().getStateId(),
			PrivacyConfigStorage.getInstance().getPrivacyConfig().getPrivacyStatus().toLowerCase()
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
	public static void initError(String message, final Integer code, WebViewCallback callback) {
		WebViewApp.getCurrentApp().setWebAppFailureMessage(message);
		WebViewApp.getCurrentApp().setWebAppFailureCode(code);
		WebViewApp.getCurrentApp().setWebAppInitialized(false);

		callback.invoke();
	}

	@WebViewExposed
	public static void getTrrData(WebViewCallback callback) {
		callback.invoke(WebViewApp.getCurrentApp().getConfiguration().getRawConfigData().toString());
	}

	@WebViewExposed
	public static void setDebugMode(Boolean debugMode, WebViewCallback callback) {
		SdkProperties.setDebugMode(debugMode);
		callback.invoke();
	}

	@WebViewExposed
	public static void getDebugMode(WebViewCallback callback) {
		callback.invoke(Boolean.valueOf(SdkProperties.getDebugMode()));
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
	public static void reinitialize(WebViewCallback callback) {
		SdkProperties.setReinitialized(true);
		InitializeThread.initialize(WebViewApp.getCurrentApp().getConfiguration());

		// Callback will not be invoked because reinitialize will destroy webview
	}

	@WebViewExposed
	public static void downloadLatestWebView(WebViewCallback callback) {
		DeviceLog.debug("Unity Ads init: WebView called download");
		callback.invoke(InitializeThread.downloadLatestWebView().getValue());
	}
}
