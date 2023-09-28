package com.unity3d.services.ads.gmascar.models;

import android.text.TextUtils;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.unity3d.services.ads.gmascar.utils.ScarConstants.*;

/**
 * Data structure for GMA bidding signals.
 */
public class BiddingSignals {

	private final String rvSignal;
	private final String interstitialSignal;
	private final String bannerSignal;

	/**
	 * Constructor that initialized the object with passed GMA bidding signals.
	 *
	 * @param rvSignal           rewarded video GMA Scar bidding signal.
	 * @param interstitialSignal interstitial GMA Scar bidding signal.
	 */
	public BiddingSignals(String rvSignal, String interstitialSignal, String bannerSignal) {
		this.rvSignal = rvSignal;
		this.interstitialSignal = interstitialSignal;
		this.bannerSignal = bannerSignal;
	}

	public BiddingSignals(String rvSignal, String interstitialSignal) {
		this.rvSignal = rvSignal;
		this.interstitialSignal = interstitialSignal;
		this.bannerSignal = "";
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
	 * Getter for banner GMA SCAR signal.
	 *
	 * @return banner bidding signal
	 */
	@Nullable
	public String getBannerSignal() {
		return bannerSignal;
	}

	/**
	 * Checks if all bidding signals are empty in which case they should not be uploaded
	 *
	 * @return true if all signals are empty, false otherwise
	 */
	public boolean isEmpty () {
		return TextUtils.isEmpty(getRvSignal()) && TextUtils.isEmpty(getInterstitialSignal()) && TextUtils.isEmpty(getBannerSignal());
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

		if (!TextUtils.isEmpty(getBannerSignal())) {
			signalsMap.put(BN_SIGNAL_KEY, getBannerSignal());
		}

		return signalsMap;
	}
}
