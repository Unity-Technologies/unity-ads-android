package com.unity3d.services.ads.operation.show;

import android.app.Activity;

import com.unity3d.ads.IUnityAdsShowListener;
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
}
