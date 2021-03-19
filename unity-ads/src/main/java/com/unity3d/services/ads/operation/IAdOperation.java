package com.unity3d.services.ads.operation;

import com.unity3d.services.core.webview.bridge.IWebViewSharedObject;

public interface IAdOperation extends IWebViewSharedObject {
	void invoke(final int timeout, final Object...invocationParameters);
}
