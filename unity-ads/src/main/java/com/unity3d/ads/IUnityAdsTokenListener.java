package com.unity3d.ads;

public interface IUnityAdsTokenListener {
	/**
	 * Callback triggered when a Header Bidding token is available.
	 *
	 * @param token A Header Bidding token
	 */
	void onUnityAdsTokenReady(String token);
}
