package com.unity3d.ads;

public interface IUnityAdsShowListener {
	/**
	 * Callback which notifies that UnityAds has failed to show a specific placement with an error message and error category.
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @param error If UnityAdsShowError.NOT_INITIALIZED, the show operation failed due to SDK is not initialized
	 *              If UnityAdsShowError.NOT_READY, the show operation failed due to placement not ready to show
	 *              If UnityAdsShowError.VIDEO_PLAYER_ERROR, the show operation failed due to an error in playing the video
	 *              If UnityAdsShowError.INVALID_ARGUMENT, the show operation failed due to invalid placement ID
	 *              If UnityAdsShowError.NO_CONNECTION, the show operation failed due to no internet connection
	 *              If UnityAdsShowError.ALREADY_SHOWING, the show operation failed due to ad is already being shown
	 *              If UnityAdsShowError.INTERNAL_ERROR, the show operation failed due to environment or internal services
	 * @param message Human-readable error message
	 */
	void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message);

	/**
	 * Callback which notifies that UnityAds has started to show ad with a specific placement.
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 */
	void onUnityAdsShowStart(String placementId);

	/**
	 * Callback which notifies that UnityAds has received a click while showing ad for a specific placement.
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 */
	void onUnityAdsShowClick(String placementId);

	/**
	 * Callback triggered when the show operation completes successfully for a placement.
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @param state If UnityAdsShowCompletionState.SKIPPED, the show operation completed after the user skipped the video playback
	 *              If UnityAdsShowCompletionState.COMPLETED, the show operation completed after the user allowed the video to play to completion before dismissing the ad
	 */
	void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state);
}
