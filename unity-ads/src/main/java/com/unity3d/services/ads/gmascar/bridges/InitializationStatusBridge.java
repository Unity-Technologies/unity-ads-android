package com.unity3d.services.ads.gmascar.bridges;

import com.unity3d.services.core.reflection.GenericBridge;

import java.util.HashMap;
import java.util.Map;

public class InitializationStatusBridge extends GenericBridge {
	private static final String adapterStatusMapMethodName = "getAdapterStatusMap";

	public InitializationStatusBridge() {
		super(new HashMap<String, Class[]>() {{
			put(adapterStatusMapMethodName, new Class[]{});
		}});
	}

	public String getClassName() {
		return "com.google.android.gms.ads.initialization.InitializationStatus";
	}

	public Map<String, Object> getAdapterStatusMap(Object initStatusObj) {
		return (Map<String, Object>) callNonVoidMethod(adapterStatusMapMethodName,
			initStatusObj, new Object[]{});
	}
}
