package com.unity3d.services.core.webview.bridge.invocation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebViewBridgeInvocationSingleThreadedExecutor {
	private static WebViewBridgeInvocationSingleThreadedExecutor instance;
	private ExecutorService _ExecutorService;

	public static WebViewBridgeInvocationSingleThreadedExecutor getInstance() {
		if (instance == null) {
			instance = new WebViewBridgeInvocationSingleThreadedExecutor();
		}
		return instance;
	}

	private WebViewBridgeInvocationSingleThreadedExecutor() {
		_ExecutorService = Executors.newSingleThreadExecutor();
	}

	public ExecutorService getExecutorService() {
		return _ExecutorService;
	}
}
