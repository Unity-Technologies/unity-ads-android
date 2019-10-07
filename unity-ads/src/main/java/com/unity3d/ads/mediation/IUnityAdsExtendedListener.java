package com.unity3d.ads.mediation;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

public interface IUnityAdsExtendedListener extends IUnityAdsListener {

  /**
   * Callback for a click event.
   *
   * @param placementId Placement, as defined in Unity Ads admin tools
   */
  void onUnityAdsClick(String placementId);

  /**
   * Callback for a placement state change event.
   *
   * @param placementId Placement, as defined in Unity Ads admin tools
   * @param oldState Placement state before the change
   * @param newState Placement state after the change
   */
  void onUnityAdsPlacementStateChanged(String placementId, UnityAds.PlacementState oldState, UnityAds.PlacementState newState);

}
