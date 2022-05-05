package com.unity3d.services.ads.gmascar.utils;

import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

public class GMAEventSender {

	public void send(GMAEvent event, Object... params) {
		WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, event, params);
	}
}
