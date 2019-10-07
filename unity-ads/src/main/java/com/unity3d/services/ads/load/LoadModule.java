package com.unity3d.services.ads.load;

import android.text.TextUtils;

import com.unity3d.services.core.configuration.IInitializationListener;
import com.unity3d.services.core.configuration.IInitializationNotificationCenter;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;
import com.unity3d.services.core.properties.SdkProperties;

import java.util.LinkedHashMap;

public class LoadModule implements IInitializationListener {

	private static LoadModule instance;

	public static LoadModule getInstance() {
		if (instance == null) {
			ILoadBridge loadBridge = new LoadBridge();
			IInitializationNotificationCenter initializationNotificationCenter = InitializationNotificationCenter.getInstance();
			instance = new LoadModule(loadBridge, initializationNotificationCenter);
		}
		return instance;
	}

	private ILoadBridge _loadBridge;
	private IInitializationNotificationCenter _initializationNotificationCenter;
	private LinkedHashMap<String, Integer> _loadEventBuffer = new LinkedHashMap<>();

	public LoadModule(ILoadBridge loadBridge, IInitializationNotificationCenter initializationNotificationCenter) {
		_loadBridge = loadBridge;
		_initializationNotificationCenter = initializationNotificationCenter;
		_initializationNotificationCenter.addListener(this);
	}

	public synchronized void load(String placementId) {
		this.addPlacementId(placementId);
		if (SdkProperties.isInitialized()) {
			this.sendLoadEvents();
		}
	}

	@Override
	public synchronized void onSdkInitialized() {
		this.sendLoadEvents();
	}

	@Override
	public void onSdkInitializationFailed(String message, int code) {

	}

	private void addPlacementId(String placementId) {
		if (!TextUtils.isEmpty(placementId)) {
			if (this._loadEventBuffer.containsKey(placementId)) {
				Integer count = this._loadEventBuffer.get(placementId);
				if (count != null) {
					Integer updatedCount = count + 1;
					this._loadEventBuffer.put(placementId, updatedCount);
				} else {
					this._loadEventBuffer.put(placementId, new Integer(1));
				}
			} else {
				this._loadEventBuffer.put(placementId, new Integer(1));
			}
		}
	}

	private void sendLoadEvents() {
		if (this._loadEventBuffer.keySet().size() > 0) {
			this._loadBridge.loadPlacements(this._loadEventBuffer);
		}
		this._loadEventBuffer = new LinkedHashMap<>();
	}

}
