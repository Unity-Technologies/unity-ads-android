package com.unity3d.services.ads.gmascar.managers;

import com.unity3d.ads.IUnityAdsTokenListener;

public class BiddingLazyManager extends BiddingBaseManager {

	public BiddingLazyManager(IUnityAdsTokenListener unityAdsTokenListener) {
		super(unityAdsTokenListener);
	}

	@Override
	public void start() {
		// Not relevant.
		//
		// Signals fetch will be start once valid unity token is fetched.
	}

	@Override
	public void onUnityTokenSuccessfullyFetched() {
		permitSignalsUpload();
		fetchSignals();
	}
}
