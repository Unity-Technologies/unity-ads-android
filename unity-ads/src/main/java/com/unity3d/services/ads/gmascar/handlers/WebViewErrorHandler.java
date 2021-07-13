package com.unity3d.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.WebViewAdsError;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

public class WebViewErrorHandler implements IAdsErrorHandler<WebViewAdsError> {

	@Override
	public void handleError(WebViewAdsError webViewAdsError) {
		WebViewEventCategory category = WebViewEventCategory.valueOf(webViewAdsError.getDomain());
		WebViewApp.getCurrentApp().sendEvent(category, webViewAdsError.getErrorCategory(), webViewAdsError.getErrorArguments());
	}
}
