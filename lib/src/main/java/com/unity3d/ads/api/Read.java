package com.unity3d.ads.api;

import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

public class Read {
	@WebViewExposed
	public static void read(WebViewCallback callback) {
    WebViewApp currentApp = WebViewApp.getCurrentApp();
    if(currentApp != null) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        CookieManager.getInstance().flush();
      }
      else {
        //noinspection deprecation
        CookieSyncManager.getInstance().sync();
      }
    }
	}


}