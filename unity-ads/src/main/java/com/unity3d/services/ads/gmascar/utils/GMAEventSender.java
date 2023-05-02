package com.unity3d.services.ads.gmascar.utils;

import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.services.core.webview.WebViewEventCategory;
import com.unity3d.services.core.webview.bridge.IEventSender;
import com.unity3d.services.core.webview.bridge.SharedInstances;

public class GMAEventSender {
	private final IEventSender _eventSender;

	public GMAEventSender() {
		this(SharedInstances.INSTANCE.getWebViewEventSender());
	}

	public GMAEventSender(IEventSender eventSender) {
		_eventSender = eventSender;
	}

	public void send(GMAEvent event, Object... params) {
		_eventSender.sendEvent(WebViewEventCategory.GMA, event, params);
	}

	public void sendVersion(String version) {
		_eventSender.sendEvent(WebViewEventCategory.INIT_GMA, GMAEvent.VERSION, version);
	}
}
