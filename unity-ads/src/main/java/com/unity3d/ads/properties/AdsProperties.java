package com.unity3d.ads.properties;

import com.unity3d.ads.IUnityAdsListener;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class AdsProperties {
	private static IUnityAdsListener _listener = null;
	private static Set<IUnityAdsListener> _listeners = Collections.synchronizedSet(new LinkedHashSet<IUnityAdsListener>());
	private static int _showTimeout = 5000;

	public static void setShowTimeout(int timeout) {
		_showTimeout = timeout;
	}

	public static int getShowTimeout() {
		return _showTimeout;
	}

	public static void setListener(IUnityAdsListener listener) {
		// cleanup possible reference in _listeners
		if (_listener != null) {
			_listeners.remove(_listener);
		}
		_listener = listener;
	}

	public static IUnityAdsListener getListener() {
		return _listener;
	}

	public static void addListener(IUnityAdsListener listener) {
		if (_listener == null) {
			// needed to bridge set/get listener and add/remove listener
			_listener = listener;
		}
		if (listener != null) {
			if (!_listeners.contains(listener)) {
				_listeners.add(listener);
			}
		}
	}

	public static Set<IUnityAdsListener> getListeners() {
		LinkedHashSet<IUnityAdsListener> listeners = new LinkedHashSet<>(_listeners);
		if (_listener != null) {
			listeners.add(_listener);
		}
		return listeners;
	}

	public static void removeListener(IUnityAdsListener listener) {
		// cleanup possible reference in _listener
		if (_listener != null && _listener.equals(listener)) {
			_listener = null;
		}
		_listeners.remove(listener);
	}
}

