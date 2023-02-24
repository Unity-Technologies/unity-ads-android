package com.unity3d.services.ads.gmascar.bridges.mobileads;

import android.content.Context;

import com.unity3d.services.ads.gmascar.finder.ScarAdapterVersion;
import com.unity3d.services.core.reflection.GenericBridge;

import java.util.Map;

public abstract class MobileAdsBridgeBase extends GenericBridge implements IMobileAdsBridge {

	/**
	 * Determines whether implementation supports SCAR bidding or not.
	 *
	 * Bidding is only supported in the v21 adapter due to optimizations with initialization and
	 * signal collection
	 *
	 * @return true if supported, false otherwise.
	 */
	public abstract boolean hasSCARBiddingSupport();

	public static final String initializeMethodName = "initialize";
	public static final String initializationStatusMethodName = "getInitializationStatus";

	public MobileAdsBridgeBase(Map<String, Class<?>[]> functionAndParameters) {
		super(functionAndParameters);
	}

	public String getClassName() {
		return "com.google.android.gms.ads.MobileAds";
	}

	public void initialize(Context context, Object initializeListener) {
		callVoidMethod(initializeMethodName, null, context, initializeListener);
	}

	public Object getInitializationStatus() {
		return callNonVoidMethod(initializationStatusMethodName, null);
	}

	public String getVersionString() {
		Object versionString = callNonVoidMethod(getVersionMethodName(), null);
		if (versionString == null) {
			return "0.0.0";
		}
		return versionString.toString();
	}
}
