package com.unity3d.services.monetization.placementcontent.core;

import com.unity3d.services.monetization.UnityMonetization;
import com.unity3d.services.monetization.placementcontent.core.PlacementContent;

import java.util.Map;

public class NoFillPlacementContent extends PlacementContent {
    public NoFillPlacementContent(String placementId, Map<String, Object> params) {
        super(placementId, params);
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public UnityMonetization.PlacementContentState getState() {
        return UnityMonetization.PlacementContentState.NO_FILL;
    }

    @Override
    public void sendCustomEvent(CustomEvent customEvent) {
    }

    @Override
    protected String getDefaultEventCategory() {
        return "NO_FILL";
    }
}
