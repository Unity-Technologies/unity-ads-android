package com.unity3d.services.core.webview.bridge;

public interface IWebViewBridgeSharedObjectStore<T extends IWebViewSharedObject> {
	T get(String id);
	void set(T sharedObject);
	void remove(String id);
}
