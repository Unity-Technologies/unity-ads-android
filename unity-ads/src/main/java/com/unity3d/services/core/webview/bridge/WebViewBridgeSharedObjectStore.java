package com.unity3d.services.core.webview.bridge;

import java.util.concurrent.ConcurrentHashMap;

public abstract class WebViewBridgeSharedObjectStore<T extends IWebViewSharedObject> implements IWebViewBridgeSharedObjectStore<T> {
	private ConcurrentHashMap<String, T> _sharedObjects = new ConcurrentHashMap<>();

	public T get(String id) {
		if (id == null) return null;
		return _sharedObjects.get(id);
	}

	public void set(T sharedObject) {
		if (sharedObject == null) return;
		_sharedObjects.put(sharedObject.getId(), sharedObject);
	}

	public void remove(T sharedObject) {
		if (sharedObject == null) return;
		remove(sharedObject.getId());
	}

	public void remove(String id) {
		_sharedObjects.remove(id);
	}
}
