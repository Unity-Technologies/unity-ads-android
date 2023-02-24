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
			if (unityAdsTokenListener == null || experiments == null) {
				// sync flow
				return new BiddingEagerManager(null);
			}

			return getExperiment(unityAdsTokenListener, experiments);
		}

		return new BiddingDisabledManager(unityAdsTokenListener);
	}

	private BiddingBaseManager getExperiment(IUnityAdsTokenListener unityAdsTokenListener,
											 IExperiments experiments) {
		String biddingManager = experiments.getScarBiddingManager();

		switch (SCARBiddingManagerType.fromName(biddingManager)) {
			case EAGER:
				return new BiddingEagerManager(unityAdsTokenListener);
			case LAZY:
				return new BiddingLazyManager(unityAdsTokenListener);
			case HYBRID:
				return new BiddingOnDemandManager(unityAdsTokenListener);
			case DISABLED:
			default:
				return new BiddingDisabledManager(unityAdsTokenListener);
		}
	}
}
