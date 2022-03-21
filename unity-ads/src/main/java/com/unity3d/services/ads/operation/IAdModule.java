package com.unity3d.services.ads.operation;

import com.unity3d.services.core.request.metrics.ISDKMetricSender;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeSharedObjectStore;
import com.unity3d.services.core.webview.bridge.IWebViewSharedObject;

public interface IAdModule<T extends IWebViewSharedObject, T2> extends IWebViewBridgeSharedObjectStore<T> {
	void executeAdOperation(final IWebViewBridgeInvoker webViewBridgeInvoker, final T2 state);
	ISDKMetricSender getMetricSender();
}
