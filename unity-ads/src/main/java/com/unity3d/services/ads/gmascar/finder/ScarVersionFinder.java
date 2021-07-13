package com.unity3d.services.ads.gmascar.finder;

import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.services.ads.gmascar.bridges.MobileAdsBridge;
import com.unity3d.services.ads.gmascar.listeners.IInitializationStatusListener;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

public class ScarVersionFinder implements IInitializationStatusListener {

	private static MobileAdsBridge _mobileAdsBridge;
	private PresenceDetector _presenceDetector;
	private GMAInitializer _gmaInitializer;
	private long _gmaSdkVersionCode = -1;

	public ScarVersionFinder(MobileAdsBridge mobileAdsBridge, PresenceDetector presenceDetector, GMAInitializer gmaInitializer) {
		_mobileAdsBridge = mobileAdsBridge;
		_presenceDetector = presenceDetector;
		_gmaInitializer = gmaInitializer;
		_gmaInitializer.getInitializeListenerBridge().setStatusListener(this);
	}

	public void getVersion() {
		try {
			if (!_presenceDetector.areGMAClassesPresent()) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.INIT_GMA, GMAEvent.VERSION, "0.0.0");
				return;
			}

			if (!_gmaInitializer.isInitialized()) {
				_gmaInitializer.initializeGMA();
			} else {
				findAndSendVersion(true);
			}

		} catch (Exception e) {
			DeviceLog.debug("Got exception finding GMA SDK: %s", e.getLocalizedMessage());
		}
	}

	public void findAndSendVersion(boolean initializeSuccess) {
		String version = initializeSuccess ? _mobileAdsBridge.getVersionString() : "0.0.0";
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.INIT_GMA, GMAEvent.VERSION, version);
	}

	public long getGoogleSdkVersionCode() {
		if (_gmaSdkVersionCode == -1) {
			String gmaSdkVersion = _mobileAdsBridge.getVersionString();
			if (gmaSdkVersion != null) {
				String[] versionComponents = gmaSdkVersion.split("\\.");
				if (versionComponents.length > 1) {
					_gmaSdkVersionCode = Long.parseLong(versionComponents[1]);
				}
			}
		}

		return _gmaSdkVersionCode;
	}

	@Override
	// @param  initStatus InitializationStatus Object retrieved through reflection
	public void onInitializationComplete(Object initStatus) {
		boolean isInitSuccessful = _gmaInitializer.initSuccessful(initStatus);
		findAndSendVersion(isInitSuccessful);
	}
}
