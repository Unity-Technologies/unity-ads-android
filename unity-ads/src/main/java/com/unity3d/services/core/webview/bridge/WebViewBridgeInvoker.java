package com.unity3d.services.core.webview.bridge;

import com.unity3d.services.core.webview.WebViewApp;

import java.lang.reflect.Method;

public class WebViewBridgeInvoker implements IWebViewBridgeInvoker {
	@Override
	public boolean invokeMethod(String className, String methodName, Method callbackMethod, Object... options) {
		return WebViewApp.getCurrentApp().invokeMethod(className, methodName, callbackMethod, options);
	}
}
