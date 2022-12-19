package com.unity3d.services.ads.gmascar.finder;

import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.services.ads.gmascar.bridges.AdapterStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializationStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;
import com.unity3d.services.ads.gmascar.bridges.mobileads.MobileAdsBridgeBase;
import com.unity3d.services.ads.gmascar.utils.GMAEventSender;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import java.util.Map;

public class GMAInitializer {

	private MobileAdsBridgeBase _mobileAdsBridge;
	private InitializeListenerBridge _initializationListenerBridge;
	private InitializationStatusBridge _initializationStatusBridge;
	private AdapterStatusBridge _adapterStatusBridge;
	private GMAEventSender _gmaEventSender;

	public GMAInitializer(MobileAdsBridgeBase mobileAdsBridge, InitializeListenerBridge initializeListenerBridge,
						  InitializationStatusBridge initializationStatusBridge, AdapterStatusBridge adapterStatusBridge) {
		_mobileAdsBridge = mobileAdsBridge;
		_initializationListenerBridge = initializeListenerBridge;
		_initializationStatusBridge = initializationStatusBridge;
		_adapterStatusBridge = adapterStatusBridge;

		_gmaEventSender = new GMAEventSender();
	}

	// We need to initialize GMA SDK in order to get the version string in GMA SDK V20 and below or if part of the isScarInitEnabled experiment group
	public void initializeGMA() {
		if (shouldInitialize()) {
			_mobileAdsBridge.initialize(ClientProperties.getApplicationContext(), _initializationListenerBridge.createInitializeListenerProxy());
		}
	}

	public boolean initSuccessful(Object initStatus) {
		Map<String, Object> statusMap = _initializationStatusBridge.getAdapterStatusMap(initStatus);

		Object adapterState = statusMap.get(_mobileAdsBridge.getClassName());
		if (adapterState != null) {
			if (_adapterStatusBridge.isGMAInitialized(adapterState)) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.INIT_SUCCESS);
				return true;
			} else {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.INIT_ERROR);
				return false;
			}
		}

		return false;
	}

	public boolean isInitialized() {
		boolean isInitialized = false;
		try {
			isInitialized = initSuccessful(_mobileAdsBridge.getInitializationStatus());
		} catch (Exception e) {
			DeviceLog.debug("ERROR: Could not get initialization status of GMA SDK - %s", e.getLocalizedMessage());
		}
		return isInitialized;
	}

	public boolean shouldInitialize() {
		if (isInitialized()) {
			_gmaEventSender.send(GMAEvent.ALREADY_INITIALIZED);
			return false;
		}

		return _mobileAdsBridge.shouldInitialize();
	}

	public InitializeListenerBridge getInitializeListenerBridge() {
		return _initializationListenerBridge;
	}
}
