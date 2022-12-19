package com.unity3d.services.ads.gmascar.bridges.mobileads;

import android.content.Context;

import com.unity3d.services.ads.gmascar.finder.ScarAdapterVersion;

public interface IMobileAdsBridge {
	void initialize(Context context, Object initializeListener);
	Object getInitializationStatus();
	String getVersionMethodName();
	int getVersionCodeIndex();
	ScarAdapterVersion getAdapterVersion(int versionCode);
	boolean shouldInitialize();
	String getVersionString();
}
