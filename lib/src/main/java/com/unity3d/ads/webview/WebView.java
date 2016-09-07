package com.unity3d.ads.webview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.misc.ViewUtilities;
import com.unity3d.ads.webview.bridge.WebViewBridgeInterface;

import java.lang.reflect.Method;

public class WebView extends android.webkit.WebView {

	private static Method _evaluateJavascript = null;

	public WebView(Context context) {
		super(context);
		WebSettings settings = getSettings();

		if(Build.VERSION.SDK_INT >= 16) {
			settings.setAllowFileAccessFromFileURLs(true);
			settings.setAllowUniversalAccessFromFileURLs(true);
		}

		if (Build.VERSION.SDK_INT >= 19) {
			try {
				_evaluateJavascript = android.webkit.WebView.class.getMethod("evaluateJavascript", String.class, ValueCallback.class);
			} catch(NoSuchMethodException e) {
				DeviceLog.exception("Method evaluateJavascript not found", e);
				_evaluateJavascript = null;
			}
		}

		settings.setAppCacheEnabled(false);
		settings.setBlockNetworkImage(false);
		settings.setBlockNetworkLoads(false);
		settings.setBuiltInZoomControls(false);
		settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		settings.setDatabaseEnabled(false);

		if(Build.VERSION.SDK_INT >= 11) {
			settings.setDisplayZoomControls(false);
		}

		settings.setDomStorageEnabled(false);

		if(Build.VERSION.SDK_INT >= 11) {
			settings.setEnableSmoothTransition(false);
		}

		settings.setGeolocationEnabled(false);
		settings.setJavaScriptCanOpenWindowsAutomatically(false);
		settings.setJavaScriptEnabled(true);
		settings.setLightTouchEnabled(false);
		settings.setLoadWithOverviewMode(false);
		settings.setLoadsImagesAutomatically(true);

		if(Build.VERSION.SDK_INT >= 17) {
			settings.setMediaPlaybackRequiresUserGesture(false);
		}

		if(Build.VERSION.SDK_INT >= 21) {
			settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
		}

		settings.setNeedInitialFocus(true);
		settings.setPluginState(WebSettings.PluginState.OFF);
		settings.setRenderPriority(WebSettings.RenderPriority.NORMAL);
		settings.setSaveFormData(false);
		settings.setSavePassword(false);
		settings.setSupportMultipleWindows(false);
		settings.setSupportZoom(false);
		settings.setUseWideViewPort(true);

		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);
		setInitialScale(0);
		setBackgroundColor(Color.TRANSPARENT);
		ViewUtilities.setBackground(this, new ColorDrawable(Color.TRANSPARENT));
		setBackgroundResource(0);

		addJavascriptInterface(new WebViewBridgeInterface(), "webviewbridge");
	}

	public void invokeJavascript(String data) {
		Utilities.runOnUiThread(new JavaScriptInvocation(data, this));
	}

	@Override
	public void loadUrl (String url) {
		DeviceLog.debug("Loading url: " + url);
		super.loadUrl(url);
	}

	private class JavaScriptInvocation implements Runnable {
		private String _jsString = null;
		private android.webkit.WebView _webView = null;

		public JavaScriptInvocation(String jsString, android.webkit.WebView webView) {
			_jsString = jsString;
			_webView = webView;
		}

		@Override
		public void run() {
			if (_jsString != null) {
				try {
					if (Build.VERSION.SDK_INT >= 19) {
						_evaluateJavascript.invoke(_webView, _jsString, null);
					} else {
						loadUrl(_jsString);
					}
				} catch (Exception e) {
					DeviceLog.exception("Error while processing JavaScriptString", e);
				}
			} else {
				DeviceLog.error("Could not process JavaScript, the string is NULL");
			}
		}
	}
}
