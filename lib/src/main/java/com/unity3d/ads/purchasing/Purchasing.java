package com.unity3d.ads.purchasing;

import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

public class Purchasing {

    public enum UnityAdsPurchasingEvent {
        COMMAND,
        VERSION,
        CATALOG,
        INITIALIZATION,
        EVENT
    }

    public static void initialize(IPurchasing purchasingInterface) {
        com.unity3d.ads.api.Purchasing.setPurchasingInterface(purchasingInterface);
    }

    public static void dispatchReturnEvent(int event, String payload) {

        WebViewApp webViewApp = WebViewApp.getCurrentApp();

        if (webViewApp == null || !webViewApp.isWebAppLoaded()) {
            return;
        }

        webViewApp.sendEvent(WebViewEventCategory.PURCHASING, UnityAdsPurchasingEvent.values()[event], payload);

    }
}
