package com.unity3d.ads.properties;

import com.unity3d.ads.IUnityAdsListener;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class AdsProperties {
	private static Set<IUnityAdsListener> _listeners = Collections.synchronizedSet(new LinkedHashSet<IUnityAdsListener>());
	private static int _showTimeout = 5000;

	public static void setShowTimeout(int timeout) {
		_showTimeout = timeout;
	}

	public static int getShowTimeout() {
		return _showTimeout;
	}

	public static void addListener(IUnityAdsListener listener) {
		if (listener != null) {
			if (!_listeners.contains(listener)) {
				_listeners.add(listener);
			}
		}
	}

	public static Set<IUnityAdsListener> getListeners() {
		return new LinkedHashSet<>(_listeners);
	}

	public static void removeListener(IUnityAdsListener listener) {
		_listeners.remove(listener);
	}
}
