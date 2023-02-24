package com.unity3d.services.ads.gmascar;

import com.unity3d.services.ads.gmascar.adapters.ScarAdapterFactory;
import com.unity3d.services.ads.gmascar.bridges.AdapterStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializationStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;
import com.unity3d.services.ads.gmascar.bridges.mobileads.MobileAdsBridgeFactory;
import com.unity3d.services.ads.gmascar.handlers.BiddingSignalsHandler;
import com.unity3d.services.ads.gmascar.handlers.WebViewErrorHandler;
import com.unity3d.services.ads.gmascar.listeners.IBiddingSignalsListener;
import com.unity3d.services.ads.gmascar.utils.GMAEventSender;

public class GMA {

	private static GMA instance;

	private static GMAScarAdapterBridge _gmaScarAdapterBridge = new GMAScarAdapterBridge(
		new MobileAdsBridgeFactory().createMobileAdsBridge(),
		new InitializeListenerBridge(),
		new InitializationStatusBridge(),
		new AdapterStatusBridge(),
		new WebViewErrorHandler(),
		new ScarAdapterFactory(),
		new GMAEventSender()
	);

	private GMA() {}

	public static GMA getInstance() {
		if (instance == null) {
			instance = new GMA();
		}
		return instance;
	}

	public GMAScarAdapterBridge getBridge() {
		return _gmaScarAdapterBridge;
	}

	/**
	 * Helper function to check if GMA SCAR bidding signals collection is supported.
	 *
	 * @return true if supported, false otherwise
	 */
	public boolean hasSCARBiddingSupport() {
		return _gmaScarAdapterBridge.hasSCARBiddingSupport();
	}

	/**
	 * Helper function for GMAScar bidding signals retrieval.
	 *
	 * @param listener {@link IBiddingSignalsListener} implementation, to be notified when
	 *                 signals are ready.
	 */
	public void getSCARBiddingSignals(IBiddingSignalsListener listener) {
		_gmaScarAdapterBridge.getSCARBiddingSignals(new BiddingSignalsHandler(listener));
	}
}
