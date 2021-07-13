package com.unity3d.services.ads.gmascar.finder;

import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.services.ads.gmascar.bridges.AdapterStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializationStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;
import com.unity3d.services.ads.gmascar.bridges.MobileAdsBridge;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import java.util.Map;

public class GMAInitializer {

	private MobileAdsBridge _mobileAdsBridge;
	private InitializeListenerBridge _initializationListenerBridge;
	private InitializationStatusBridge _initializationStatusBridge;
	private AdapterStatusBridge _adapterStatusBridge;

	public GMAInitializer(MobileAdsBridge mobileAdsBridge, InitializeListenerBridge initializeListenerBridge,
						  InitializationStatusBridge initializationStatusBridge, AdapterStatusBridge adapterStatusBridge) {
		_mobileAdsBridge = mobileAdsBridge;
		_initializationListenerBridge = initializeListenerBridge;
		_initializationStatusBridge = initializationStatusBridge;
		_adapterStatusBridge = adapterStatusBridge;
	}

	// We need to initialize GMA SDK in order to get the version string
	public void initializeGMA() {
		if (isInitialized()) {
			return;
		} else {
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
			isInitialized = false;
			DeviceLog.debug("ERROR: Could not get initialization status of GMA SDK - %s", e.getLocalizedMessage());
		} finally {
			return isInitialized;
		}
	}

	public InitializeListenerBridge getInitializeListenerBridge() {
		return _initializationListenerBridge;
	}
}
