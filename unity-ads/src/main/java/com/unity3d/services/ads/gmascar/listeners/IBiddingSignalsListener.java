package com.unity3d.services.ads.gmascar.listeners;

import com.unity3d.services.ads.gmascar.models.BiddingSignals;

import org.jetbrains.annotations.Nullable;

/**
 * Interface to notify sender when GMA Bidding Signals are ready.
 */
public interface IBiddingSignalsListener {

	/**
	 * Success callback.
	 *
	 * @param signals {@link BiddingSignals} retrieved GMA SCAR bidding signals.
	 */
	void onSignalsReady(@Nullable BiddingSignals signals);

	/**
	 * Fail callback.
	 *
	 * @param msg error msg.
	 */
	void onSignalsFailure(String msg);
}
