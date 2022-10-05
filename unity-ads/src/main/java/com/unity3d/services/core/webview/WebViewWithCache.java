package com.unity3d.services.core.webview;

import android.content.Context;
import android.webkit.WebSettings;

public class WebViewWithCache extends WebView {
	public WebViewWithCache(Context context, boolean shouldNotRequireGesturePlayback) {
		super(context, shouldNotRequireGesturePlayback);
		WebSettings settings = getSettings();
		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		settings.setDomStorageEnabled(true);
		if (shouldNotRequireGesturePlayback) {
			settings.setMediaPlaybackRequiresUserGesture(false);
		}
	}
}
