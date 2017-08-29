package com.unity3d.ads.device;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.unity3d.ads.properties.ClientProperties;

import java.util.ArrayList;

public class VolumeChange {
	private static ContentObserver _contentObserver;
	private static ArrayList<IVolumeChangeListener> _listeners;

	public static void startObserving() {
		if (_contentObserver == null) {
			_contentObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
				@Override
				public boolean deliverSelfNotifications() {
					return false;
				}

				@Override
				public void onChange(boolean selfChange, Uri uri) {
					triggerListeners();
				}
			};

			Context context = ClientProperties.getApplicationContext();

			if (context != null) {
				ContentResolver contentResolver = context.getContentResolver();

				if (contentResolver != null) {
					contentResolver.registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, _contentObserver);
				}
			}
		}
	}

	public static void stopObservering() {
		if(_contentObserver != null) {
			Context context = ClientProperties.getApplicationContext();

			if (context != null) {
				ContentResolver contentResolver = context.getContentResolver();

				if (contentResolver != null) {
					contentResolver.unregisterContentObserver(_contentObserver);
				}
			}

			_contentObserver = null;
		}
	}

	public static void registerListener(IVolumeChangeListener listener) {
		if (_listeners == null) {
			_listeners = new ArrayList<>();
		}

		if (!_listeners.contains(listener)) {
			startObserving();
			_listeners.add(listener);
		}
	}

	public static void unregisterListener(IVolumeChangeListener listener) {
		if (_listeners.contains(listener)) {
			_listeners.remove(listener);
		}

		if (_listeners == null || _listeners.size() == 0) {
			stopObservering();
		}
	}

	public static void clearAllListeners() {
		if (_listeners != null) {
			_listeners.clear();
		}

		stopObservering();
		_listeners = null;
	}

	private static void triggerListeners() {
		if (_listeners != null) {
			for (IVolumeChangeListener listener : _listeners) {
				int currentVolume = Device.getStreamVolume(listener.getStreamType());
				listener.onVolumeChanged(currentVolume);
			}
		}
	}
}
