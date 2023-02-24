package com.unity3d.services.ads.gmascar.managers;

import com.unity3d.ads.IUnityAdsTokenListener;

public class BiddingDisabledManager extends BiddingBaseManager {

	public BiddingDisabledManager(IUnityAdsTokenListener unityAdsTokenListener) {
		super(unityAdsTokenListener);
	}

	@Override
	public String getTokenIdentifier() {
		// SCAR bidding signals collection should be blocked.
		return null;
	}

	@Override
	public void start() {
		// SCAR bidding signals collection should be blocked.
	}

	@Override
	public void onUnityTokenSuccessfullyFetched() {
		// SCAR bidding signals collection should be blocked.
	}
}
