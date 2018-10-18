package com.unity3d.services.ads.adunit;

import android.os.Bundle;
import android.view.View;

import com.unity3d.services.core.misc.ViewUtilities;
import com.unity3d.services.core.webview.WebViewApp;

public class WebViewHandler implements IAdUnitViewHandler {
	public boolean create(AdUnitActivity activity) {
		return true;
	}

	public boolean destroy() {
		if (WebViewApp.getCurrentApp() != null && WebViewApp.getCurrentApp().getWebView() != null) {
			ViewUtilities.removeViewFromParent(WebViewApp.getCurrentApp().getWebView());
		}
		return true;
	}

	public View getView() {
		if (WebViewApp.getCurrentApp() != null) {
			return WebViewApp.getCurrentApp().getWebView();
		}

		return null;
	}

	public void onCreate(AdUnitActivity activity, Bundle savedInstanceState) {
	}

	public void onStart(AdUnitActivity activity) {
	}

	public void onStop(AdUnitActivity activity) {
	}

	public void onResume(AdUnitActivity activity) {
	}

	public void onPause(AdUnitActivity activity) {
	}

	public void onDestroy(AdUnitActivity activity) {
		destroy();
	}
}
