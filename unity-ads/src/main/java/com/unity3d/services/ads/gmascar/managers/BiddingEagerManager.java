package com.unity3d.services.ads.gmascar.managers;

import com.unity3d.ads.IUnityAdsTokenListener;

public class BiddingEagerManager extends BiddingBaseManager {

	public BiddingEagerManager(IUnityAdsTokenListener unityAdsTokenListener) {
		super(unityAdsTokenListener);
	}

	@Override
	public void start() {
		permitSignalsUpload();
		fetchSignals();
	}

	@Override
	public void onUnityTokenSuccessfullyFetched() {
		// Not relevant.
		//
		// Signals upload will be start once scar signals are ready regardless of unityToken
		// validity.
	}
}
