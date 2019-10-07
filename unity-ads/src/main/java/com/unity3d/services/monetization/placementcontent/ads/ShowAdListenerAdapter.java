package com.unity3d.services.monetization.placementcontent.ads;

import com.unity3d.ads.UnityAds;

public abstract class ShowAdListenerAdapter implements IShowAdListener {
    @Override
    public void onAdFinished(String placementId, UnityAds.FinishState withState) {
        // Intentionally left blank
    }

    @Override
    public void onAdStarted(String placementId) {
        // Intentionally left blank
    }
}
