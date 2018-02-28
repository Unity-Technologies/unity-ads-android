package com.unity3d.ads.webplayer;

import android.webkit.JavascriptInterface;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONException;

public class WebPlayerBridgeInterface {
    @JavascriptInterface
    public void handleEvent(String data) {
        if (WebViewApp.getCurrentApp() != null) {
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.WEBPLAYER, WebPlayerEvent.WEBPLAYER_EVENT, data);
        }
    }
}
