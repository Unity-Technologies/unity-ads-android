package com.unity3d.services.ads.gmascar.models;

import android.text.TextUtils;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.unity3d.services.ads.gmascar.utils.ScarConstants.IN_SIGNAL_KEY;
import static com.unity3d.services.ads.gmascar.utils.ScarConstants.RV_SIGNAL_KEY;

/**
 * Data structure for GMA bidding signals.
 */
public class BiddingSignals {

	private final String rvSignal;
	private final String interstitialSignal;

	/**
	 * Constructor that initialized the object with passed GMA bidding signals.
	 *
	 * @param rvSignal           rewarded video GMA Scar bidding signal.
	 * @param interstitialSignal interstitial GMA Scar bidding signal.
	 */
	public BiddingSignals(String rvSignal, String interstitialSignal) {
		this.rvSignal = rvSignal;
		this.interstitialSignal = interstitialSignal;
	}

	/**
	 * Getter for Rewarded Video GMA SCAR signal.
	 *
	 * @return rewarded video bidding signal
	 */
	@Nullable
	public String getRvSignal() {
		return rvSignal;
	}

	/**
	 * Getter for interstitial GMA SCAR signal.
	 *
	 * @return interstitial bidding signal
	 */
	@Nullable
	public String getInterstitialSignal() {
		return interstitialSignal;
	}

	/**
	 * Checks if both bidding signals are empty in which case they should not be uploaded
	 *
	 * @return true if both signals are empty, false otherwise
	 */
	public boolean isEmpty () {
		return TextUtils.isEmpty(getRvSignal()) && TextUtils.isEmpty(getInterstitialSignal());
	}

	/**
	 * Builds a hashmap of the signals which will be converted to JSON
	 *
	 * @return map of valid signals
	 */
	public Map<String, String> getMap() {
		Map<String, String> signalsMap = new HashMap<>();

		if (!TextUtils.isEmpty(getRvSignal())) {
			signalsMap.put(RV_SIGNAL_KEY, getRvSignal());
		}

		if (!TextUtils.isEmpty(getInterstitialSignal())) {
			signalsMap.put(IN_SIGNAL_KEY, getInterstitialSignal());
		}

		return signalsMap;
	}
}
