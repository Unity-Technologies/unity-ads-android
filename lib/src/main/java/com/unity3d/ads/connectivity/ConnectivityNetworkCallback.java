package com.unity3d.ads.connectivity;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;

import com.unity3d.ads.properties.ClientProperties;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class ConnectivityNetworkCallback extends ConnectivityManager.NetworkCallback {
	private static ConnectivityNetworkCallback _impl = null;

	public static void register() {
		if(_impl == null) {
			_impl = new ConnectivityNetworkCallback();

			ConnectivityManager cm = (ConnectivityManager)ClientProperties.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			cm.registerNetworkCallback(new NetworkRequest.Builder().build(), _impl);
		}
	}

	public static void unregister() {
		if(_impl != null) {
			ConnectivityManager cm = (ConnectivityManager)ClientProperties.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			cm.unregisterNetworkCallback(_impl);

			_impl = null;
		}
	}

	@Override
	public void onAvailable(Network network) {
		ConnectivityMonitor.connected();
	}

	@Override
	public void onLost(Network network) {
		ConnectivityMonitor.disconnected();
	}

	@Override
	public void onCapabilitiesChanged(Network network, NetworkCapabilities capabilities) {
		ConnectivityMonitor.connectionStatusChanged();
	}

	@Override
	public void onLinkPropertiesChanged(Network network, LinkProperties properties) {
		ConnectivityMonitor.connectionStatusChanged();
	}
}