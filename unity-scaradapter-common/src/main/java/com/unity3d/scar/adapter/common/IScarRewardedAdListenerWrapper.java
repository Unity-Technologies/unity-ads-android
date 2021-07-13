package com.unity3d.scar.adapter.common;

/**
 * A listener for receiving notifications during the lifecycle of an ad.
 */
public interface IScarRewardedAdListenerWrapper {

	/**
	 * Called when a rewarded ad has loaded successfully.
	 */
	void onRewardedAdLoaded();

	/**
	 * Called when a rewarded ad has failed to load.
	 */
	void onRewardedAdFailedToLoad(int errorCode, String errorString);

	/**
	 * Called when a rewarded ad opens.
	 */
	void onRewardedAdOpened();

	/**
	 * Called when the rewarded ad fails to show.
	 */
	void onRewardedAdFailedToShow(int errorCode, String errorString);

	/**
	 * Called when a user earned a reward for watching the ad.
	 */
	void onUserEarnedReward();

	/**
	 * Called when a rewarded ad is closed.
	 */
	void onRewardedAdClosed();

	/**
	 * Called when an impression is recorded for an ad.
	 */
	void onAdImpression();
}
