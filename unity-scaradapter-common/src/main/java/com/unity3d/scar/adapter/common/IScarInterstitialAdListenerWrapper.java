package com.unity3d.scar.adapter.common;

/**
 * A listener for receiving notifications during the lifecycle of an ad.
 */
public interface IScarInterstitialAdListenerWrapper extends IScarAdListenerWrapper {

	/**
	 * Called when an ad failed to show on screen.
	 */
	void onAdFailedToShow(int errorCode, String errorString);

	/**
	 * Called when a click is recorded for an ad.
	 */
	void onAdClicked();

	/**
	 * Called when the user has left the app.
	 */
	void onAdLeftApplication();

	/**
	 * Called when an impression is recorded for an ad.
	 */
	void onAdImpression();
}
