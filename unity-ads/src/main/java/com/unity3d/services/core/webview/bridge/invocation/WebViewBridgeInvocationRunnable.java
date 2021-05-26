package com.unity3d.services.core.webview.bridge.invocation;

import android.os.ConditionVariable;

import com.unity3d.services.core.webview.bridge.CallbackStatus;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import java.lang.reflect.Method;

public class WebViewBridgeInvocationRunnable implements Runnable {
	private static ConditionVariable _responseTimeout;
	private static CallbackStatus _callbackStatus;

	private IWebViewBridgeInvocationCallback _invocationCallback;
	private IWebViewBridgeInvoker _webViewBridgeInvoker;
	private Method _webViewBridgeCallbackMethod;

	private String _className;
	private String _methodName;
	private int _timeoutLengthInMilliSeconds;
	private Object[] _invocationParameters;

	public WebViewBridgeInvocationRunnable(IWebViewBridgeInvocationCallback invocationCallback, IWebViewBridgeInvoker webViewBridgeInvoker, String className, String methodName, int timeoutLengthInMilliSeconds, Object...invocationParameters) {
		try {
			_webViewBridgeCallbackMethod = WebViewBridgeInvocationRunnable.class.getMethod("onInvocationComplete", CallbackStatus.class);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("WebViewBridgeInvocation callback method cannot be found", e);
		}

		_invocationCallback = invocationCallback;
		_webViewBridgeInvoker = webViewBridgeInvoker;
		_className = className;
		_methodName = methodName;
		_timeoutLengthInMilliSeconds = timeoutLengthInMilliSeconds;
		_invocationParameters = invocationParameters;
	}

	@Override
	public void run() {
		_callbackStatus = null;
		_responseTimeout = new ConditionVariable();

		boolean invokeMethodSuccess = _webViewBridgeInvoker.invokeMethod(_className, _methodName, _webViewBridgeCallbackMethod, _invocationParameters);

		if (_invocationCallback == null) return;

		if (!invokeMethodSuccess) {
			_invocationCallback.onFailure("WebViewBridgeInvocationRunnable:run: invokeMethod failure", null);
			return;
		}

		if (_responseTimeout.block(_timeoutLengthInMilliSeconds)) {
			if (_callbackStatus == CallbackStatus.OK) {
				_invocationCallback.onSuccess();
			} else {
				_invocationCallback.onFailure("WebViewBridgeInvocationRunnable:run CallbackStatus.Error", _callbackStatus);
			}
		} else {
			_invocationCallback.onTimeout();
		}
	}

	public static synchronized void onInvocationComplete(CallbackStatus status) {
		_callbackStatus = status;
		if (_responseTimeout != null) {
			_responseTimeout.open();
		}
	}
}
