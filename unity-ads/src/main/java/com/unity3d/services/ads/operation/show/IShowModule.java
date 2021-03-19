package com.unity3d.services.ads.operation.show;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.operation.IAdModule;

public interface IShowModule extends IAdModule<IShowOperation, ShowOperationState> {
	void onUnityAdsShowFailure(String id,  UnityAds.UnityAdsShowError error, String message);
	void onUnityAdsShowStart(String id);
	void onUnityAdsShowClick(String id);
	void onUnityAdsShowComplete(String id, UnityAds.UnityAdsShowCompletionState state);
}