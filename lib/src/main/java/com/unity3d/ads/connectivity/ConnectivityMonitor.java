package com.unity3d.ads.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

import java.util.HashSet;

public class ConnectivityMonitor {
	private static int _connected = -1;
	private static boolean _listening = false;
	private static boolean _webappMonitoring = false;
	private static boolean _wifi = false;
	private static int _networkType = -1;
	private static HashSet<IConnectivityListener> _listeners = null;

	public static void setConnectionMonitoring(boolean monitoring) {
		_webappMonitoring = monitoring;
		updateListeningStatus();
	}

	public static void addListener(IConnectivityListener listener) {
		if(_listeners == null) {
			_listeners = new HashSet<>();
		}

		_listeners.add(listener);
		updateListeningStatus();
	}

	public static void removeListener(IConnectivityListener listener) {
		if(_listeners == null) {
			return;
		}

		_listeners.remove(listener);
		updateListeningStatus();
	}

	public static void stopAll() {
		_listeners = null;
		_webappMonitoring = false;
		updateListeningStatus();
	}

	private static void updateListeningStatus() {
		if(_webappMonitoring || (_listeners != null && !_listeners.isEmpty())) {
			startListening();
		} else {
			stopListening();
		}
	}

	private static void startListening() {
		if(_listening) {
			return;
		}
		_listening = true;

		initConnectionStatus();

		if(Build.VERSION.SDK_INT < 21) {
			ConnectivityChangeReceiver.register();
		} else {
			ConnectivityNetworkCallback.register();
		}
	}

	private static void stopListening() {
		if(!_listening) {
			return;
		}
		_listening = false;

		if(Build.VERSION.SDK_INT < 21) {
			ConnectivityChangeReceiver.unregister();
		} else {
			ConnectivityNetworkCallback.unregister();
		}
	}

	private static void initConnectionStatus() {
		ConnectivityManager cm = (ConnectivityManager)ClientProperties.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

		if(cm == null) {
			return;
		}

		NetworkInfo ni  = cm.getActiveNetworkInfo();

		if(ni != null && ni.isConnected()) {
			_connected = 1;
			_wifi = ni.getType() == ConnectivityManager.TYPE_WIFI;

			if(!_wifi) {
				TelephonyManager tm = (TelephonyManager)ClientProperties.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
				_networkType = tm.getNetworkType();
			}
		} else {
			_connected = 0;
		}
	}

	public static void connected() {
		// Connection events are sent for many reasons like various connection property changes
		// All events will be sent here so we need to ignore events that don't change connection status
		if(_connected == 1) {
			return;
		}

		DeviceLog.debug("Unity Ads connectivity change: connected");

		initConnectionStatus();

		if(_listeners != null) {
			for(IConnectivityListener listener : _listeners) {
				listener.onConnected();
			}
		}

		sendToWebview(ConnectivityEvent.CONNECTED, _wifi, _networkType);
	}

	public static void disconnected() {
		// Connection events are sent for many reasons like various connection property changes
		// All events will be sent here so we need to ignore events that don't change connection status
		if(_connected == 0) {
			return;
		}
		_connected = 0;

		DeviceLog.debug("Unity Ads connectivity change: disconnected");

		if(_listeners != null) {
			for (IConnectivityListener listener : _listeners) {
				listener.onDisconnected();
			}
		}

		sendToWebview(ConnectivityEvent.DISCONNECTED, false, 0);
	}

	public static void connectionStatusChanged() {
		// Ignore status change events if device is not connected
		if(_connected != 1) {
			return;
		}

		ConnectivityManager cm = (ConnectivityManager)ClientProperties.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni  = cm.getActiveNetworkInfo();

		if(ni != null && ni.isConnected()) {
			boolean wifiStatus = ni.getType() == ConnectivityManager.TYPE_WIFI;
			TelephonyManager tm = (TelephonyManager)ClientProperties.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			int mobileNetworkType = tm.getNetworkType();

			// If wifi status and network type have not changed, ignore this event. If wifi is on, mobile network type does not need to match.
			if(wifiStatus == _wifi && (mobileNetworkType == _networkType || _wifi)) {
				return;
			}

			_wifi = wifiStatus;
			_networkType = mobileNetworkType;

			DeviceLog.debug("Unity Ads connectivity change: network change");

			sendToWebview(ConnectivityEvent.NETWORK_CHANGE, wifiStatus, mobileNetworkType);
		}
	}

	private static void sendToWebview(ConnectivityEvent eventType, boolean wifi, int networkType) {
		if(!_webappMonitoring) {
			return;
		}

		WebViewApp webViewApp = WebViewApp.getCurrentApp();

		// Don't send events before webview is properly initialized
		if(webViewApp == null || !webViewApp.isWebAppLoaded()) {
			return;
		}

		switch(eventType) {
			case CONNECTED:
				if(wifi) {
					webViewApp.sendEvent(WebViewEventCategory.CONNECTIVITY, ConnectivityEvent.CONNECTED, Boolean.valueOf(wifi), Integer.valueOf(0));
				} else {
					webViewApp.sendEvent(WebViewEventCategory.CONNECTIVITY, ConnectivityEvent.CONNECTED, Boolean.valueOf(wifi), Integer.valueOf(networkType));
				}
				break;

			case DISCONNECTED:
				webViewApp.sendEvent(WebViewEventCategory.CONNECTIVITY, ConnectivityEvent.DISCONNECTED);
				break;

			case NETWORK_CHANGE:
				if(wifi) {
					webViewApp.sendEvent(WebViewEventCategory.CONNECTIVITY, ConnectivityEvent.NETWORK_CHANGE, Boolean.valueOf(wifi), Integer.valueOf(0));
				} else {
					webViewApp.sendEvent(WebViewEventCategory.CONNECTIVITY, ConnectivityEvent.NETWORK_CHANGE, Boolean.valueOf(wifi), Integer.valueOf(networkType));
				}
				break;
			default:
				break;
		}
	}
}