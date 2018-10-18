package com.unity3d.services.monetization.core.placementcontent;

import com.unity3d.services.monetization.UnityMonetization;
import com.unity3d.services.monetization.placementcontent.core.CustomEvent;
import com.unity3d.services.monetization.placementcontent.core.PlacementContent;

import java.util.HashMap;
import java.util.Map;

public class PlacementContents {
    private static Map<String, PlacementContent> placementContentsMap = new HashMap<>();
    private static final PlacementContent NOT_AVAILABLE = PlacementContents.getNotAvailablePlacementContent();

    public static PlacementContent getPlacementContent(String placementId) {
        PlacementContent content = placementContentsMap.get(placementId);
        if (content == null) {
            return NOT_AVAILABLE;
        }
        return content;
    }

    public static <T extends PlacementContent> T getPlacementContent(String placementId, Class<T> asClass) {
        PlacementContent placementcontent = getPlacementContent(placementId);
        if (asClass.isInstance(placementcontent)) {
            return asClass.cast(placementcontent);
        }
        return null;
    }

    public static PlacementContent putPlacementContent(String placementId, PlacementContent placementcontent) {
        return placementContentsMap.put(placementId, placementcontent);
    }

    public static boolean isReady(String placementId) {
        PlacementContent result = getPlacementContent(placementId);
        return result != null && result.isReady();
    }

    public static void removePlacementContent(String placementId) {
        placementContentsMap.remove(placementId);
    }

    public static void setPlacementContentState(String placementId, UnityMonetization.PlacementContentState state) {
        PlacementContent placementcontent = getPlacementContent(placementId);
        if (placementcontent != null) {
            placementcontent.setState(state);
        }
    }

    private static PlacementContent getNotAvailablePlacementContent() {
        Map<String, Object> params = new HashMap<>();
        params.put("type", "NOT_AVAILABLE");
        return new NotAvailablePlacementContent("", params);
    }

    private static final class NotAvailablePlacementContent extends PlacementContent {
        public NotAvailablePlacementContent(String placementId, Map<String, Object> params) {
            super(placementId, params);
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public UnityMonetization.PlacementContentState getState() {
            return UnityMonetization.PlacementContentState.NOT_AVAILABLE;
        }

        @Override
        public void sendCustomEvent(CustomEvent customEvent) {
        }

        @Override
        protected String getDefaultEventCategory() {
            return "NOT_AVAILABLE";
        }
    }
}
