package com.unity3d.services.core.webview.bridge.invocation;

public interface IWebViewBridgeInvocation {
	void invoke(String className, String methodName, int timeoutLengthInMilliSeconds, Object...invocationParameters);
}
