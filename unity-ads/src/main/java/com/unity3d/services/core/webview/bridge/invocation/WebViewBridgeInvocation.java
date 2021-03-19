package com.unity3d.services.core.webview.bridge.invocation;

import android.os.ConditionVariable;

import com.unity3d.services.core.webview.bridge.CallbackStatus;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebViewBridgeInvocation implements IWebViewBridgeInvocation {
	private static ConditionVariable responseTimeout;
	private static IWebViewBridgeInvocationCallback invocationCallback;

	private IWebViewBridgeInvoker _webViewBridgeInvoker;
	private Method webViewBridgeCallbackMethod;

	private ExecutorService executorService;

	public WebViewBridgeInvocation(IWebViewBridgeInvoker webViewBridgeInvoker, IWebViewBridgeInvocationCallback invocationCallback) {
		responseTimeout = new ConditionVariable();
		this.invocationCallback = invocationCallback;

		if (webViewBridgeInvoker == null) {
			throw new IllegalArgumentException("webViewBridgeInvoker cannot be null");
		}

		_webViewBridgeInvoker = webViewBridgeInvoker;
		executorService = Executors.newSingleThreadExecutor();

		try {
			webViewBridgeCallbackMethod = WebViewBridgeInvocation.class.getMethod("onInvocationComplete", CallbackStatus.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("loadCallback cannot be null");
		}
	}

	@Override
	public synchronized void invoke(final String className, final String methodName, final int timeoutLengthInMilliSeconds, final Object...invocationParameters) {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				if (!_webViewBridgeInvoker.invokeMethod(className, methodName, webViewBridgeCallbackMethod, invocationParameters)) {
					if (invocationCallback != null) {
						invocationCallback.onFailure("WebViewBridgeInvocation:execute: invokeMethod failure", null);
					}
					return;
				}

				if (!responseTimeout.block(timeoutLengthInMilliSeconds)) {
					if (invocationCallback != null) {
						invocationCallback.onTimeout();
					}
				}
			}
		});
	}

	public static synchronized void onInvocationComplete(CallbackStatus status) {
		if (responseTimeout != null) {
			responseTimeout.open();
		}

		if (invocationCallback == null) return;

		switch (status) {
			case OK:
				invocationCallback.onSuccess();
				break;
			default:
				invocationCallback.onFailure("WebViewBridgeInvocation:OnInvocationComplete: CallbackStatus.Error", status);
				break;
		}
	}
}