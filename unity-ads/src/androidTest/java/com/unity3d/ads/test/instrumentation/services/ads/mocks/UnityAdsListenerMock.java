package com.unity3d.ads.test.instrumentation.services.ads.mocks;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

public class UnityAdsListenerMock implements IUnityAdsListener {

	public void onUnityAdsReady(String placementId) {

	}

	public void onUnityAdsStart(String placementId) {

	}

	public void onUnityAdsFinish(String placementId, UnityAds.FinishState result) {

	}

	public void onUnityAdsError(UnityAds.UnityAdsError error, String message) {

	}

}
