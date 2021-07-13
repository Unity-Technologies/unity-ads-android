package com.unity3d.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;
import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

public class SignalsHandler implements ISignalCollectionListener {

	@Override
	public void onSignalsCollected(String signalsMap) {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.SIGNALS, signalsMap);
	}

	@Override
	public void onSignalsCollectionFailed(String errorMsg) {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.SIGNALS_ERROR, errorMsg);
	}
}
