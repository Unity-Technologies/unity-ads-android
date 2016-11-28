package com.unity3d.ads.mediation;

import com.unity3d.ads.IUnityAdsListener;

public interface IUnityAdsExtendedListener extends IUnityAdsListener {

  /**
   * Callback for a click event.
   *
   * @param placementId Placement, as defined in Unity Ads admin tools
   */
  void onUnityAdsClick(String placementId);

}
