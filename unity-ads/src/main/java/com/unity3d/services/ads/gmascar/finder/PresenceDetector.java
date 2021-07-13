package com.unity3d.services.ads.gmascar.finder;

import com.unity3d.services.ads.gmascar.bridges.AdapterStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializationStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;
import com.unity3d.services.ads.gmascar.bridges.MobileAdsBridge;

public class PresenceDetector {
	private MobileAdsBridge _mobileAdsBridge;
	private InitializeListenerBridge _initializationListenerBridge;
	private InitializationStatusBridge _initializationStatusBridge;
	private AdapterStatusBridge _adapterStatusBridge;

	public PresenceDetector(MobileAdsBridge mobileAdsBridge, InitializeListenerBridge initializeListenerBridge,
							InitializationStatusBridge initializationStatusBridge, AdapterStatusBridge adapterStatusBridge) {
		_mobileAdsBridge = mobileAdsBridge;
		_initializationListenerBridge = initializeListenerBridge;
		_initializationStatusBridge = initializationStatusBridge;
		_adapterStatusBridge = adapterStatusBridge;
	}

	public boolean areGMAClassesPresent() {
		if (_mobileAdsBridge == null || _initializationListenerBridge == null ||
			_initializationStatusBridge == null || _adapterStatusBridge == null) {
			return false;
		}

		return _mobileAdsBridge.exists() && _initializationListenerBridge.exists() && _initializationStatusBridge.exists()
			&& _adapterStatusBridge.exists();
	}
}
