package com.unity3d.services.core.webview.bridge.invocation;

import com.unity3d.services.core.webview.bridge.CallbackStatus;

public interface IWebViewBridgeInvocationCallback {
	void onSuccess();
	void onFailure(String message, CallbackStatus callbackStatus);
	void onTimeout();
}
