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
			return getExperiment(unityAdsTokenListener, experiments);
		}

		return new BiddingDisabledManager(unityAdsTokenListener);
	}

	private BiddingBaseManager getExperiment(IUnityAdsTokenListener unityAdsTokenListener,
											 IExperiments experiments) {
		if (experiments == null || experiments.getScarBiddingManager() == null) {
			return new BiddingDisabledManager(unityAdsTokenListener);
		}

		String biddingManager = experiments.getScarBiddingManager();
		SCARBiddingManagerType biddingManagerType = SCARBiddingManagerType.fromName(biddingManager);

		/*
		  If unityAdsTokenListener is null it is a synchronous getToken call and we should use
		  the EAGER bidding manager if part of any of the enabled experiment types since we do not
		  have to listen for a token fetch result
		*/
		if (unityAdsTokenListener == null && biddingManagerType != SCARBiddingManagerType.DISABLED) {
			return new BiddingEagerManager(null);
		}

		switch (biddingManagerType) {
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
