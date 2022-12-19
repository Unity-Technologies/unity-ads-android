package com.unity3d.scar.adapter.common;

public interface IScarAdListenerWrapper {

	/**
	 * Called when an ad is loaded.
	 */
	void onAdLoaded();

	/**
	 * Called when an ad request failed to load.
	 */
	void onAdFailedToLoad(int errorCode, String errorString);

	/**
	 * Called when an ad opens.
	 */
	void onAdOpened();

	/**
	 * Called when a click is recorded for an ad.
	 */
	void onAdClicked();

	/**
	 * Called when the user does not watch the ad to completion.
	 */
	void onAdSkipped();

	/**
	 * Called when an ad is closed.
	 */
	void onAdClosed();
}
