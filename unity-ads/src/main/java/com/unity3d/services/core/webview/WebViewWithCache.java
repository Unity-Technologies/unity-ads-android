package com.unity3d.services.core.webview;

import android.content.Context;
import android.webkit.WebSettings;

public class WebViewWithCache extends WebView {
	public WebViewWithCache(Context context) {
		super(context);
		WebSettings settings = getSettings();
		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		settings.setDomStorageEnabled(true);
	}
}
