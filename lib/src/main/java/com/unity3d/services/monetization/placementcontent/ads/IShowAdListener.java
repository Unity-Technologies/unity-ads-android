package com.unity3d.services.monetization.placementcontent.ads;

import android.app.Activity;

import com.unity3d.ads.UnityAds;

/**
 * Listener class for the {@link ShowAdPlacementContent#show(Activity, IShowAdListener)} method.
 * Can be used to know when an ad has started or finished.
 * A convenience class of {@link ShowAdListenerAdapter} can be used when only one method needs overriding.
 */
public interface IShowAdListener {
    /**
     * Called when the ad has finished.
     * @param placementId Placement ID as configured in the Unity Ads dashboard.
     * @param withState The state which the Ad has finished with.
     */
    void onAdFinished(String placementId, UnityAds.FinishState withState);

    /**
     * Notifies that the ad for the given placement has started.
     * @param placementId Placement ID as configured in the Unity Ads dashboard.
     */
    void onAdStarted(String placementId);
}
