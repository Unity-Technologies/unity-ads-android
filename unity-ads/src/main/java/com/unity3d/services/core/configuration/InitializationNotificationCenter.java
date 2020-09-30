package com.unity3d.services.core.configuration;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InitializationNotificationCenter implements IInitializationNotificationCenter {

	private static InitializationNotificationCenter instance = null;
	private HashMap<Integer, IInitializationListener> _sdkListeners =  new HashMap<>();

	public static InitializationNotificationCenter getInstance() {
		if (instance == null) {
			instance = new InitializationNotificationCenter();
		}
		return instance;
	}

	public void addListener(IInitializationListener listener) {
		synchronized (_sdkListeners) {
			if (listener != null) {
				_sdkListeners.put(new Integer(listener.hashCode()), listener);
			}
		}
	}

	public void removeListener(IInitializationListener listener) {
		synchronized (_sdkListeners) {
			if (listener != null) {
				this.removeListener(new Integer(listener.hashCode()));
			}
		}
	}

	public void triggerOnSdkInitialized() {
		synchronized (_sdkListeners) {
			ArrayList<Integer> keysToRemove = new ArrayList<>();
			for (final Map.Entry<Integer, IInitializationListener> entry : _sdkListeners.entrySet()) {
				if (entry.getValue() != null) {
					Utilities.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							entry.getValue().onSdkInitialized();
						}
					});
				} else {
					keysToRemove.add(entry.getKey());
				}
			}
			for (final Integer key : keysToRemove) {
				_sdkListeners.remove(key);
			}
		}
	}

	public void triggerOnSdkInitializationFailed(String message, final int code) {
		synchronized (_sdkListeners) {
			final String exceptionMessage = "SDK Failed to Initialize due to " + message;
			DeviceLog.error(exceptionMessage);

			ArrayList<Integer> keysToRemove = new ArrayList<>();
			for (final Map.Entry<Integer, IInitializationListener> entry : _sdkListeners.entrySet()) {
				if (entry.getValue() != null) {
					Utilities.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							entry.getValue().onSdkInitializationFailed(exceptionMessage, code);
						}
					});
				} else {
					keysToRemove.add(entry.getKey());
				}
			}
			for (final Integer key : keysToRemove) {
				_sdkListeners.remove(key);
			}
		}
	}

	private void removeListener(Integer key) {
		_sdkListeners.remove(key);
	}

}
