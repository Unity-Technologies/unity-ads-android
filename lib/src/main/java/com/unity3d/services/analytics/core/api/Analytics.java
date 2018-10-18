package com.unity3d.services.analytics.core.api;

import com.unity3d.services.analytics.interfaces.AnalyticsError;
import com.unity3d.services.analytics.interfaces.IAnalytics;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class Analytics {

    public static IAnalytics analyticsInterface = null;

    public static void setAnalyticsInterface(IAnalytics analytics) {
        analyticsInterface = analytics;
    }

    @WebViewExposed
    public static void addExtras(final String extras, WebViewCallback callback) {
        if (analyticsInterface != null) {
            Utilities.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    analyticsInterface.onAddExtras(extras);
                }
            });
            callback.invoke();
        } else {
            callback.error(AnalyticsError.API_NOT_FOUND, extras);
        }
    }

}
