package com.unity3d.services.ads.gmascar.bridges;

import android.content.Context;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.reflection.GenericBridge;

import java.util.HashMap;

public class MobileAdsBridge extends GenericBridge {
	private static final String initializeMethodName = "initialize";
	private static final String initializationStatusMethodName = "getInitializationStatus";
	private static final String versionStringMethodName = "getVersionString";

	public MobileAdsBridge() {
		super(new HashMap<String, Class[]>() {{
			try {
				put(initializeMethodName, new Class[]{Context.class, Class.forName("com.google.android.gms.ads.initialization.OnInitializationCompleteListener")});
			} catch (ClassNotFoundException e) {
				DeviceLog.debug("Could not find class \"com.google.android.gms.ads.initialization.OnInitializationCompleteListener\" %s", e.getLocalizedMessage());
			}
			put(initializationStatusMethodName,  new Class[]{});
			put(versionStringMethodName, new Class[]{});
		}});
	}

	public String getClassName() {
		return "com.google.android.gms.ads.MobileAds";
	}

	public void initialize(Context context, Object initializeListener) {
		callVoidMethod(initializeMethodName, null, new Object[]{context, initializeListener});
	}

	public String getVersionString() {
		Object versionString = callNonVoidMethod(versionStringMethodName, null, new Object[]{});
		if (versionString == null) {
			return "0.0.0";
		}
		return versionString.toString();
	}

	public Object getInitializationStatus () {
		return callNonVoidMethod(initializationStatusMethodName, null, new Object[]{});
	}

}
