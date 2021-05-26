package com.unity3d.services.core.webview.bridge.invocation;

import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import java.util.concurrent.ExecutorService;

public class WebViewBridgeInvocation implements IWebViewBridgeInvocation {
	private static IWebViewBridgeInvocationCallback invocationCallback;

	private IWebViewBridgeInvoker _webViewBridgeInvoker;
	private ExecutorService _executorService;

	public WebViewBridgeInvocation(ExecutorService _executorService, IWebViewBridgeInvoker webViewBridgeInvoker, IWebViewBridgeInvocationCallback invocationCallback) {
		this._executorService = _executorService;
		this.invocationCallback = invocationCallback;

		if (webViewBridgeInvoker == null) {
			throw new IllegalArgumentException("webViewBridgeInvoker cannot be null");
		}

		_webViewBridgeInvoker = webViewBridgeInvoker;
	}

	@Override
	public synchronized void invoke(final String className, final String methodName, final int timeoutLengthInMilliSeconds, final Object... invocationParameters) {
		_executorService.submit(
			new WebViewBridgeInvocationRunnable(invocationCallback, _webViewBridgeInvoker, className, methodName, timeoutLengthInMilliSeconds, invocationParameters));
	}
}
