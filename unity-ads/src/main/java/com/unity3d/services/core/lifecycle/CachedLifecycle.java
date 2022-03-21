package com.unity3d.services.core.lifecycle;

import android.annotation.TargetApi;

import com.unity3d.services.core.properties.ClientProperties;

public class CachedLifecycle {

	private static LifecycleCache _listener;

	public static void register() {
		if (ClientProperties.getApplication() != null) {
			if (getLifecycleListener() == null) {
				setLifecycleListener(new LifecycleCache());
				ClientProperties.getApplication().registerActivityLifecycleCallbacks(getLifecycleListener());
			}
		}
	}

	public static void unregister() {
		if (ClientProperties.getApplication() != null) {
			if (getLifecycleListener() != null) {
				ClientProperties.getApplication().unregisterActivityLifecycleCallbacks(getLifecycleListener());
				setLifecycleListener(null);
			}
		}
	}

	public static LifecycleCache getLifecycleListener () {
		return _listener;
	}

	public static void setLifecycleListener (LifecycleCache listener) {
		_listener = listener;
	}
}
