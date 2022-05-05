package com.unity3d.services.ads.operation.load;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.services.ads.operation.OperationState;
import com.unity3d.services.core.configuration.Configuration;

public class LoadOperationState extends OperationState {
	public IUnityAdsLoadListener listener;
	public UnityAdsLoadOptions loadOptions;

	public LoadOperationState(String placementId, IUnityAdsLoadListener listener, UnityAdsLoadOptions loadOptions, Configuration configuration) {
		super(placementId, configuration);
		this.listener = listener;
		this.loadOptions = loadOptions;
	}

	public void onUnityAdsFailedToLoad(UnityAds.UnityAdsLoadError error, String message) {
		if (listener != null) {
			listener.onUnityAdsFailedToLoad(this.placementId, error, message);
		}
	}

	public void onUnityAdsAdLoaded() {
		if (listener != null) {
			listener.onUnityAdsAdLoaded(this.placementId);
		}
	}
}
