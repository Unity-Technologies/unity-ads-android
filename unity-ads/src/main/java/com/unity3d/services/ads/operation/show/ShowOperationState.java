package com.unity3d.services.ads.operation.show;

import android.app.Activity;

import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.services.ads.operation.OperationState;
import com.unity3d.services.core.configuration.Configuration;

public class ShowOperationState extends OperationState {
	public Activity activity;
	public IUnityAdsShowListener listener;
	public UnityAdsShowOptions showOptions;

	public ShowOperationState(String placementId, IUnityAdsShowListener listener, Activity activity, UnityAdsShowOptions showOptions, Configuration configuration) {
		super(placementId, configuration);
		this.listener = listener;
		this.activity = activity;
		this.showOptions = showOptions;
	}

	public void onUnityAdsShowFailure(UnityAds.UnityAdsShowError error, String message) {
		if (listener != null) {
			listener.onUnityAdsShowFailure(this.placementId, error, message);
		}
	}

	public void onUnityAdsShowClick() {
		if (listener != null) {
			listener.onUnityAdsShowClick(this.placementId);
		}
	}

	public void onUnityAdsShowStart(String placementId) {
		if (listener != null) {
			listener.onUnityAdsShowStart(placementId);
		}
	}

	public void onUnityAdsShowComplete(UnityAds.UnityAdsShowCompletionState state) {
		if (listener != null) {
			listener.onUnityAdsShowComplete(this.placementId, state);
		}
	}
}
