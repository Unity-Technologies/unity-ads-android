package com.unity3d.services.ads.gmascar.bridges;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.reflection.GenericBridge;

import java.util.HashMap;

public class AdapterStatusBridge extends GenericBridge {

	private static final String initializeStateMethodName = "getInitializationState";

	// This class contains the AdapterState Enum
	private Class _adapterStateClass;

	public AdapterStatusBridge() {
		super(new HashMap<String, Class[]>() {{
			put(initializeStateMethodName, new Class[]{});
		}});
		AdapterStatusStateBridge adapterStatusStateBridge = new AdapterStatusStateBridge();
		try {
			_adapterStateClass = Class.forName(adapterStatusStateBridge.getClassName());
		} catch (ClassNotFoundException e) {
			DeviceLog.debug("ERROR: Could not find class %s %s", adapterStatusStateBridge.getClassName(), e.getLocalizedMessage());
		}
	}

	protected String getClassName() {
		return "com.google.android.gms.ads.initialization.AdapterStatus";
	}

	public boolean isGMAInitialized(Object adapterState) {
		Object[] states = getAdapterStatesEnum();
		if (states == null) {
			DeviceLog.debug("ERROR: Could not get adapter states enum from AdapterStatus.State");
			return false;
		}

		// States[0]: AdapterState.NOT_READY | States[1]: AdapterState.READY
		return (callNonVoidMethod(initializeStateMethodName, adapterState, new Object[]{}) == states[1]);
	}

	public Object[] getAdapterStatesEnum() {
		return _adapterStateClass.getEnumConstants();
	}
}
