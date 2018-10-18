package com.unity3d.services.analytics.mocks;

import com.unity3d.services.analytics.UnityAnalytics;

import org.json.JSONArray;

public class UnityAnalyticsMock extends UnityAnalytics {
    public static JSONArray getEventQueue() {
        return UnityAnalytics.eventQueue;
    }

    public static void clearEventQueue() {
        eventQueue = new JSONArray();
    }
}
