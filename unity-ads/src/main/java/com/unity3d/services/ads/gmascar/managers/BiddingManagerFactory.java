package com.unity3d.services.ads.gmascar.managers;

import com.unity3d.ads.IUnityAdsTokenListener;
import com.unity3d.services.ads.gmascar.GMA;

import com.unity3d.services.core.configuration.IExperiments;

public class BiddingManagerFactory {

	private static BiddingManagerFactory instance;

	private BiddingManagerFactory() {}

	public static BiddingManagerFactory getInstance() {
		if (instance == null) {
			instance = new BiddingManagerFactory();
		}
		return instance;
	}

	public BiddingBaseManager createManager(IUnityAdsTokenListener unityAdsTokenListener,
											IExperiments experiments) {
		if (GMA.getInstance().hasSCARBiddingSupport()) {
			boolean isBannerEnabled = experiments != null && experiments.isScarBannerHbEnabled();
			return new BiddingEagerManager(isBannerEnabled, unityAdsTokenListener);
		}

		return new BiddingDisabledManager(unityAdsTokenListener);
	}
}