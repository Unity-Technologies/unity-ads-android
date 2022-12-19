package com.unity3d.services.ads.gmascar.bridges.mobileads;

public class MobileAdsBridgeFactory {

	public MobileAdsBridgeBase createMobileAdsBridge() {
		MobileAdsBridgeBase _mobileAdsBridge = new MobileAdsBridge();
		if (_mobileAdsBridge.exists()) {
			return _mobileAdsBridge;
		}

		MobileAdsBridgeLegacy _mobileAdsBridgeLegacy = new MobileAdsBridgeLegacy();
		if (_mobileAdsBridgeLegacy.exists()) {
			return _mobileAdsBridgeLegacy;
		}

		return null;
	}
}
