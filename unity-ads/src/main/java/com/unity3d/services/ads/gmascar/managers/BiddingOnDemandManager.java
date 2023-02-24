package com.unity3d.services.ads.gmascar.managers;

import com.unity3d.ads.IUnityAdsTokenListener;

public class BiddingOnDemandManager extends BiddingBaseManager {

	public BiddingOnDemandManager(IUnityAdsTokenListener unityAdsTokenListener) {
		super(unityAdsTokenListener);
	}

	@Override
	public void start() {
		fetchSignals();
	}

	@Override
	public void onUnityTokenSuccessfullyFetched() {
		permitSignalsUpload();
	}
}
