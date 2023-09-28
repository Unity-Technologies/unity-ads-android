package com.unity3d.services.ads.gmascar.managers;

import com.unity3d.ads.IUnityAdsTokenListener;

public class BiddingEagerManager extends BiddingBaseManager {

	public BiddingEagerManager(boolean isBannerEnabled, IUnityAdsTokenListener unityAdsTokenListener) {
		super(isBannerEnabled, unityAdsTokenListener);
	}

	@Override
	public void start() {
		permitSignalsUpload();
		fetchSignals();
	}
}
