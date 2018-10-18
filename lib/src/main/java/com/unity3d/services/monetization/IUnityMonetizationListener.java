package com.unity3d.services.monetization; import android.app.Activity;

import com.unity3d.services.IUnityServicesListener;
import com.unity3d.services.monetization.placementcontent.core.PlacementContent;

/**
 * Listener for events within the Monetization service. Used in {@link UnityMonetization#setListener(IUnityMonetizationListener)},
 * {@link UnityMonetization#initialize(Activity, String, IUnityMonetizationListener)}, or {@link UnityMonetization#initialize(Activity, String, IUnityMonetizationListener, boolean)}
 */
public interface IUnityMonetizationListener extends IUnityServicesListener {
    void onPlacementContentReady(String placementId, PlacementContent placementcontent);
    void onPlacementContentStateChange(String placementId, PlacementContent placementcontent, UnityMonetization.PlacementContentState previousState, UnityMonetization.PlacementContentState newState);
}
