package com.unity3d.scar.adapter.common;

public interface IScarBannerAdListenerWrapper extends IScarAdListenerWrapper {

	/**
	 * Called when an impression is recorded for an ad.
	 */
	void onAdImpression();
}
