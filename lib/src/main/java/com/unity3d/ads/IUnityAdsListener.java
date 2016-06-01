package com.unity3d.ads;

public interface IUnityAdsListener {
	/**
	 * Callback that tells placement is ready to show ads. After this callback you can call UnityAds.show method for this placement.
	 * Note that sometimes placement might no longer be ready due to exceptional reasons. These situations will give no new callbacks.
	 * To avoid error situations, it is always best to check isReady method status before calling show.
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 */
	void onUnityAdsReady(String placementId);

	/**
	 * Callback for a successful start of advertisement after UnityAds.show. If there are errors in advertisement start,
	 * Unity Ads might never call this method. Instead Unity Ads will directly call onUnityAdsFinish with error status.
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 */
	void onUnityAdsStart(String placementId);

	/**
	 * Callback after advertisement has closed.
	 * Each call to UnityAds.show is guaranteed to give this callback even in all failure scenarios.
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @param result If FinishState.COMPLETED, advertisement was watched to the end without skipping.
	 *               If FinishState.SKIPPED, advertisement was skipped.
	 *               If FinishState.ERROR, there was a technical error that prevented Unity Ads from showing ads.
	 */
	void onUnityAdsFinish(String placementId, UnityAds.FinishState result);

	/**
	 * Callback for different errors in Unity Ads. All errors will be logged but this method can be used as
	 * additional debugging aid. This callback can also be used for collecting statistics from different error scenarios.
	 *
	 * @param error Error category
	 * @param message Human-readable error message
	 */
	void onUnityAdsError(UnityAds.UnityAdsError error, String message);
}