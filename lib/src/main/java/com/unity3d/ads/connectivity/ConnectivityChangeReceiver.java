package com.unity3d.ads.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.unity3d.ads.properties.ClientProperties;

public class ConnectivityChangeReceiver extends BroadcastReceiver {
	private static ConnectivityChangeReceiver _receiver = null;

	public static void register() {
		if(_receiver == null) {
			_receiver = new ConnectivityChangeReceiver();
			ClientProperties.getApplicationContext().registerReceiver(_receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		}
	}

	public static void unregister() {
		if(_receiver != null) {
			ClientProperties.getApplicationContext().unregisterReceiver(_receiver);
			_receiver = null;
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
			ConnectivityMonitor.disconnected();
			return;
		}

		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

		if(cm == null) return;

		NetworkInfo ni  = cm.getActiveNetworkInfo();

		if(ni != null && ni.isConnected()) {
			ConnectivityMonitor.connected();
		}
	}
}