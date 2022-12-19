package com.unity3d.services.ads;

import android.app.Activity;
import android.content.Context;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.IUnityAdsTokenListener;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.ads.UnityAdsShowOptions;

public interface IUnityAds {
	void initialize(Context context, String gameId, boolean testMode, IUnityAdsInitializationListener initializationListener);

	boolean isInitialized();

	boolean isSupported();

	String getVersion();

	void show(Activity activity, String placementId, UnityAdsShowOptions showOptions, IUnityAdsShowListener showListener);

	void setDebugMode(boolean debugMode);

	boolean getDebugMode();

	void load(String placementId, UnityAdsLoadOptions loadOptions, IUnityAdsLoadListener listener);

	String getToken();

	void getToken(IUnityAdsTokenListener listener);
}
