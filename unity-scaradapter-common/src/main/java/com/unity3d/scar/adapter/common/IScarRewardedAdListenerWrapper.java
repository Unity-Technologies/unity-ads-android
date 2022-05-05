package com.unity3d.scar.adapter.common;

/**
 * A listener for receiving notifications during the lifecycle of an ad.
 */
public interface IScarRewardedAdListenerWrapper extends IScarAdListenerWrapper {

	/**
	 * Called when an ad failed to show on screen.
	 */
	void onAdFailedToShow(int errorCode, String errorString);

	/**
	 * Called when a user earned a reward for watching the ad.
	 */
	void onUserEarnedReward();

	/**
	 * Called when an impression is recorded for an ad.
	 */
	void onAdImpression();
}
