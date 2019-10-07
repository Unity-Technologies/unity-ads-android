package com.unity3d.services.analytics.interfaces;

public class Analytics {
    /*
    * Set from c# layer
    */
    public static void initialize(IAnalytics analyticsInterfaces) {
        com.unity3d.services.analytics.core.api.Analytics.setAnalyticsInterface(analyticsInterfaces);
    }
}
