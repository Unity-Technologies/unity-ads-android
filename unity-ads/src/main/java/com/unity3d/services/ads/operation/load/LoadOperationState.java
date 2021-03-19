package com.unity3d.services.ads.operation.load;

import android.os.ConditionVariable;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.services.core.webview.bridge.IWebViewSharedObject;
import com.unity3d.services.core.configuration.Configuration;

import java.util.UUID;

public class LoadOperationState implements IWebViewSharedObject {
	public String id;
	public IUnityAdsLoadListener listener;
	public UnityAdsLoadOptions loadOptions;
	public String placementId;
	public Configuration configuration;
	public ConditionVariable timeoutCV;

	public LoadOperationState(String placementId, IUnityAdsLoadListener listener, UnityAdsLoadOptions loadOptions, Configuration configuration) {
		this.listener = listener;
		this.loadOptions = loadOptions;
		this.placementId = placementId;
		this.configuration = configuration;
		this.timeoutCV = new ConditionVariable();
		id = UUID.randomUUID().toString();
	}

	@Override
	public String getId() {
		return id;
	}
}
