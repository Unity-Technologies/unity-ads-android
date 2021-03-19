package com.unity3d.ads;

public interface IUnityAdsLoadListener {
	/**
	 * Callback triggered when a load request has successfully filled the specified placementId with an ad that is ready to show.
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 */
	void onUnityAdsAdLoaded(String placementId);

	/**
	 * Callback triggered when load request has failed to load an ad for a requested placement.
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @param error Error code related to the error  See: {@link com.unity3d.ads.UnityAds.UnityAdsLoadError}
	 * @param message Human-readable error message
	 */
	void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message);
}
