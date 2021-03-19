package com.unity3d.services.ads.operation;

import com.unity3d.services.core.request.ISDKMetricSender;
import com.unity3d.services.core.request.SDKMetricSender;
import com.unity3d.services.core.webview.bridge.IWebViewSharedObject;
import com.unity3d.services.core.webview.bridge.WebViewBridgeSharedObjectStore;

public abstract class AdModule<T extends IWebViewSharedObject, T2> extends WebViewBridgeSharedObjectStore<T> implements IAdModule<T, T2> {
	protected ISDKMetricSender _sdkMetricSender;

	protected AdModule(ISDKMetricSender sdkMetricSender) {
		super();
		_sdkMetricSender = sdkMetricSender;
	}

	public ISDKMetricSender getMetricSender() {
		return _sdkMetricSender;
	}
}
