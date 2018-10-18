package com.unity3d.services.monetization.placementcontent.ads;

import android.app.Activity;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.monetization.placementcontent.core.PlacementContent;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.monetization.placementcontent.core.RewardablePlacementContent;

import java.util.HashMap;
import java.util.Map;

public class ShowAdPlacementContent extends RewardablePlacementContent {
    private static Map<String, IShowAdListener> listenerMap = new HashMap<>();

    public ShowAdPlacementContent(String placementId, Map<String, Object> params) {
        super(placementId, params);
    }

    public void show(Activity activity, IShowAdListener listener) {
        listenerMap.put(placementId, listener);
        if (UnityAds.isReady(placementId)) {
            UnityAds.show(activity, placementId);
        } else {
            sendAdFinished(placementId, UnityAds.FinishState.ERROR);
        }
    }

    public static void sendAdFinished(String placementId, UnityAds.FinishState finishState) {
        IShowAdListener listener = listenerMap.remove(placementId);
        if (listener != null) {
            listener.onAdFinished(placementId, finishState);
        }
    }

    public static void sendAdStarted(String placementId) {
        IShowAdListener listener = listenerMap.get(placementId);
        if (listener != null) {
            listener.onAdStarted(placementId);
        }
    }
}
