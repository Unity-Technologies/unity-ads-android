package com.unity3d.services.ads.gmascar.managers;

import com.unity3d.ads.IUnityAdsTokenListener;

public interface IBiddingManager extends IUnityAdsTokenListener {
	String getTokenIdentifier();
	String getFormattedToken(String unityToken);
}
