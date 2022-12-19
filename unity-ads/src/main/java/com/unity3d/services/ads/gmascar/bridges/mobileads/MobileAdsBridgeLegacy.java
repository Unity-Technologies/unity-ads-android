package com.unity3d.services.ads.gmascar.bridges.mobileads;

import android.content.Context;

import com.unity3d.services.ads.gmascar.finder.ScarAdapterVersion;
import com.unity3d.services.core.log.DeviceLog;

import java.util.HashMap;

public class MobileAdsBridgeLegacy extends MobileAdsBridgeBase {

	// Codes returned by getVersionString in V20 and below
	public static final int CODE_21_0 = 221310000;
	public static final int CODE_20_0 = 210402000;
	public static final int CODE_19_8 = 204890000;
	public static final int CODE_19_5 = 203404000;
	public static final int CODE_19_2 = 201604000;

	// Deprecated in V21 - requires initialization and returns internal version (e.g., "afma-sdk-a-v<OTA services>.<SCAR version>.X")
	public static final String versionStringMethodName = "getVersionString";

	public MobileAdsBridgeLegacy() {
		super(new HashMap<String, Class<?>[]>() {{
			try {
				put(initializeMethodName, new Class[]{Context.class, Class.forName("com.google.android.gms.ads.initialization.OnInitializationCompleteListener")});
			} catch (ClassNotFoundException e) {
				DeviceLog.debug("Could not find class \"com.google.android.gms.ads.initialization.OnInitializationCompleteListener\" %s", e.getLocalizedMessage());
			}
			put(initializationStatusMethodName,  new Class[]{});
			put(versionStringMethodName, new Class[]{});
		}});
	}

	@Override
	public String getVersionMethodName() {
		return versionStringMethodName;
	}

	@Override
	public int getVersionCodeIndex() {
		return 1;
	}

	@Override
	public ScarAdapterVersion getAdapterVersion(int versionCode) {
		// Version codes in V20 and below are returned as internal nine digit numbers (e.g., "afma-sdk-a-vX.210402000.X)
		if (versionCode >= CODE_19_2 && versionCode < CODE_19_5) {
			return ScarAdapterVersion.V192;
		} else if (versionCode >= CODE_19_5 && versionCode <= CODE_19_8) {
			return ScarAdapterVersion.V195;
		} else if (versionCode >= CODE_20_0 && versionCode < CODE_21_0) {
			return ScarAdapterVersion.V20;
		}

		return ScarAdapterVersion.NA;
	}

	@Override
	public boolean shouldInitialize() {
		return true;
	}
}
