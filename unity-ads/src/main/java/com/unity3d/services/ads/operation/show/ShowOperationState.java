package com.unity3d.services.ads.operation.show;

import android.app.Activity;
import android.os.ConditionVariable;

import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.webview.bridge.IWebViewSharedObject;

import java.util.UUID;

public class ShowOperationState implements IWebViewSharedObject {
	public String id;
	public Activity activity;
	public IUnityAdsShowListener listener;
	public String placementId;
	public UnityAdsShowOptions showOptions;
	public Configuration configuration;
	public ConditionVariable timeoutCV;

	public ShowOperationState(String placementId, IUnityAdsShowListener listener, Activity activity, UnityAdsShowOptions showOptions, Configuration configuration) {
		this.listener = listener;
		this.placementId = placementId;
		this.activity = activity;
		this.showOptions = showOptions;
		this.configuration = configuration;
		this.timeoutCV = new ConditionVariable();
		id = UUID.randomUUID().toString();
	}

	@Override
	public String getId() {
		return id;
	}
}
