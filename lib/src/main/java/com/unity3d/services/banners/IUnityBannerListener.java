package com.unity3d.services.banners;

import android.view.View;

public interface IUnityBannerListener {
    /**
     * Called when the unity banner is finished loading an ad.
     * @param placementId The placement ID of the banner loaded, as defined in the Unity Ads Dashboard.
     * @param view A reference to the banner that should be inserted into the view hierarchy
     */
    void onUnityBannerLoaded(String placementId, View view);

    /**
     * Called when the unity banner is unloaded and references to its view should be removed.
     * @param placementId  The placement ID of the banner unloaded, as defined in the Unity Ads Dashboard.
     */
    void onUnityBannerUnloaded(String placementId);

    /**
     * Called when the banner is shown the first time and visible to the user.
     * @param placementId The placement ID of the banner shown, as defined in the Unity Ads Dashboard.
     */
    void onUnityBannerShow(String placementId);

    /**
     * Called when the banner is clicked.
     * @param placementId The placement ID of the banner clicked, as defined in the Unity Ads Dashboard.
     */
    void onUnityBannerClick(String placementId);

    /**
     * Called when the banner is hidden.
     * @param placementId The placement ID of the banner hidden, as defined in the Unity Ads Dashboard.
     */
    void onUnityBannerHide(String placementId);

    /**
     * Called when an error is shown showing the banner.
     * @param message A generic error that should be logged to the console.
     */
    void onUnityBannerError(String message);
}
