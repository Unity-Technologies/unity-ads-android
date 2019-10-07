package com.unity3d.services.monetization.placementcontent.core;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.monetization.UnityMonetization;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.monetization.core.placementcontent.PlacementContentEvent;
import com.unity3d.services.monetization.core.webview.WebViewEventCategory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PlacementContent {
    protected String placementId;
    private String type;
    private Map<String, Object> extras = new HashMap<>();
    private UnityMonetization.PlacementContentState state;

    public PlacementContent(String placementId, Map<String, Object> params) {
        this.placementId = placementId;
        this.type = (String) params.get("type");
        this.extras.putAll(params);
    }

    public Object getExtra(String key) {
        return this.extras.get(key);
    }

    public Map<String, Object> getExtras() {
        return this.extras;
    }

    public String getType() {
        return type;
    }

    public void setState(UnityMonetization.PlacementContentState state) {
        this.state = state;
    }

    public void sendCustomEvent(CustomEvent customEvent) {
        if (customEvent.getCategory() == null) {
            customEvent.setCategory(getDefaultEventCategory());
        }
        WebViewApp app = WebViewApp.getCurrentApp();
        if (app == null) {
            DeviceLog.warning("Could not send custom event due to app being null");
        } else {
            JSONObject eventDataMap = getJsonForCustomEvent(customEvent);
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.PLACEMENT_CONTENT, PlacementContentEvent.CUSTOM, placementId, eventDataMap);
        }
    }

    private JSONObject getJsonForCustomEvent(CustomEvent customEvent) {
        JSONObject json = new JSONObject();
        try {
            json.put("category", customEvent.getCategory());
            json.put("type", customEvent.getType());
            json.put("data", customEvent.getData());
        } catch (JSONException e) {
            DeviceLog.warning("Error creating json for custom event: ", e.getMessage());
        }
        return json;
    }

    public boolean isReady() {
        return this.state == UnityMonetization.PlacementContentState.READY;
    }

    public void sendCustomEvent(String type, Map<String, Object> eventData) {
        sendCustomEvent(new CustomEvent(type, eventData));
    }

    public void sendCustomEvent(String category, String type, Map<String, Object> eventData) {
        sendCustomEvent(new CustomEvent(category, type, eventData));
    }

    public UnityMonetization.PlacementContentState getState() {
        return state;
    }

    protected String getDefaultEventCategory() {
        return "PLACEMENT_CONTENT";
    }
}
