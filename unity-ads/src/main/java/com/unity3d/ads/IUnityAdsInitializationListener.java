package com.unity3d.ads;

public interface IUnityAdsInitializationListener {
	/**
	 * Callback which notifies UnityAds has been successfully initialized.
	 */
	void onInitializationComplete();

	/**
	 * Callback which notifies UnityAds has failed initialization
	 * with error message and error category.
	 *
	 * @param message Human-readable error message
	 * @param error If UnityAdsInitializationError.INTERNAL_ERROR, initialization failed due to environment or internal services
	 *              If UnityAdsInitializationError.INVALID_ARGUMENT, initialization failed due to invalid argument(e.g. game ID)
	 *              If UnityAdsInitializationError.AD_BLOCKER_DETECTED, initialization failed due to url being blocked
	 */
	void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message);
}
