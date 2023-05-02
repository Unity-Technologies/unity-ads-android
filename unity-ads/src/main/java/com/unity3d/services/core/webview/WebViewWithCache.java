package com.unity3d.services.core.webview;

import android.content.Context;
import android.webkit.WebSettings;

import com.unity3d.services.core.configuration.IExperiments;
import com.unity3d.services.core.webview.bridge.IInvocationCallbackInvoker;
import com.unity3d.services.core.webview.bridge.IWebViewBridge;
import com.unity3d.services.core.webview.bridge.SharedInstances;

public class WebViewWithCache extends WebView {
	public WebViewWithCache(Context context, boolean shouldNotRequireGesturePlayback, IExperiments experiments) {
		this(context, shouldNotRequireGesturePlayback, SharedInstances.INSTANCE.getWebViewBridge(), SharedInstances.INSTANCE.getWebViewAppInvocationCallbackInvoker(), experiments);
	}

	public WebViewWithCache(Context context, boolean shouldNotRequireGesturePlayback, IWebViewBridge webViewBridge, IInvocationCallbackInvoker callbackInvoker, IExperiments experiments) {
		super(context, shouldNotRequireGesturePlayback, webViewBridge, callbackInvoker, experiments);
		WebSettings settings = getSettings();
		settings.setCacheMode(WebSettings.LOAD_DEFAULT);
		settings.setDomStorageEnabled(true);
		if (shouldNotRequireGesturePlayback) {
			settings.setMediaPlaybackRequiresUserGesture(false);
		}
	}
}
