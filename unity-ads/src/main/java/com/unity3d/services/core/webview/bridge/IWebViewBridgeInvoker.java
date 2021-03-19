package com.unity3d.services.core.webview.bridge;

import java.lang.reflect.Method;

public interface IWebViewBridgeInvoker {
	boolean invokeMethod(String className, String methodName, Method callbackMethod, Object...options);
}
