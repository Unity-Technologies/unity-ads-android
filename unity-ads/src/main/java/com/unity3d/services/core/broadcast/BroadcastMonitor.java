package com.unity3d.services.core.broadcast;

import android.content.Context;
import android.content.IntentFilter;

import com.unity3d.services.core.properties.ClientProperties;

import java.util.HashMap;
import java.util.Map;

public class BroadcastMonitor {
	private static BroadcastMonitor _instance;
	public static synchronized BroadcastMonitor getInstance() {
		if (_instance == null) {
			_instance = new BroadcastMonitor(ClientProperties.getApplicationContext());
		}
		return _instance;
	}

	private Map<String,BroadcastEventReceiver> _eventReceivers;
	private final Context _context;

	private BroadcastMonitor(Context context) {
		_context = context;
	}

	public void addBroadcastListener(String name, String dataScheme, String[] actions) {
		removeBroadcastListener(name);

		IntentFilter filter = new IntentFilter();

		for(String action : actions) {
			filter.addAction(action);
		}

		if(dataScheme != null) {
			filter.addDataScheme(dataScheme);
		}

		if(_eventReceivers == null) {
			_eventReceivers = new HashMap<>();
		}

		BroadcastEventReceiver eventReceiver = new BroadcastEventReceiver(name);
		_eventReceivers.put(name, eventReceiver);
		_context.registerReceiver(eventReceiver, filter);
	}

	public void removeBroadcastListener(String name) {
		if(_eventReceivers != null && _eventReceivers.containsKey(name)) {
			_context.unregisterReceiver(_eventReceivers.get(name));
			_eventReceivers.remove(name);
		}
	}

	public void removeAllBroadcastListeners() {
		if(_eventReceivers != null) {
			for(String key : _eventReceivers.keySet()) {
				_context.unregisterReceiver(_eventReceivers.get(key));
			}

			_eventReceivers = null;
		}
	}
}