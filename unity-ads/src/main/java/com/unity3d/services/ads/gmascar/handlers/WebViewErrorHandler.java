package com.unity3d.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.WebViewAdsError;
import com.unity3d.services.core.webview.WebViewEventCategory;
import com.unity3d.services.core.webview.bridge.IEventSender;
import com.unity3d.services.core.webview.bridge.SharedInstances;

public class WebViewErrorHandler implements IAdsErrorHandler<WebViewAdsError> {

	private final IEventSender _eventSender;

	public WebViewErrorHandler() {
		 this(SharedInstances.INSTANCE.getWebViewEventSender());
	}

	public WebViewErrorHandler(IEventSender eventSender) {
		_eventSender = eventSender;
	}
	@Override
	public void handleError(WebViewAdsError webViewAdsError) {
		WebViewEventCategory category = WebViewEventCategory.valueOf(webViewAdsError.getDomain());
		_eventSender.sendEvent(category, webViewAdsError.getErrorCategory(), webViewAdsError.getErrorArguments());
	}
}
