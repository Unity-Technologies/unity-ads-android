package com.unity3d.services.ads.gmascar.bridges.mobileads;

import android.content.Context;

import com.unity3d.services.ads.gmascar.finder.ScarAdapterVersion;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.log.DeviceLog;

import java.util.HashMap;

public class MobileAdsBridge extends MobileAdsBridgeBase {

	// Introduced in V21 - does not require initialization and returns external version (e.g., "21.0.0")
	public static final String versionMethodName = "getVersion";
	public static final int CODE_21 = 21;
	
	private ConfigurationReader _configurationReader = new ConfigurationReader();

	public MobileAdsBridge() {
		super(new HashMap<String, Class<?>[]>() {{
			try {
				put(initializeMethodName, new Class[]{Context.class, Class.forName("com.google.android.gms.ads.initialization.OnInitializationCompleteListener")});
			} catch (ClassNotFoundException e) {
				DeviceLog.debug("Could not find class \"com.google.android.gms.ads.initialization.OnInitializationCompleteListener\" %s", e.getLocalizedMessage());
			}
			put(initializationStatusMethodName,  new Class[]{});
			put(versionMethodName, new Class[]{});
		}});
	}

	@Override
	public String getVersionMethodName() {
		return versionMethodName;
	}

	@Override
	public int getVersionCodeIndex() {
		return 0;
	}

	@Override
	// versionCode will be used when there are multiple adapters options using the new MobileAdsBridge APIs
	public ScarAdapterVersion getAdapterVersion(int versionCode) {
		if (versionCode == -1) {
			return ScarAdapterVersion.NA;
		}
		return ScarAdapterVersion.V21;
	}

	@Override
	public boolean shouldInitialize() {
		return _configurationReader.getCurrentConfiguration().getExperiments().isScarInitEnabled();
	}
}
