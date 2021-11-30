package com.unity3d.services.ads.gmascar.bridges;

import com.unity3d.services.ads.gmascar.listeners.IInitializationStatusListener;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.reflection.GenericBridge;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;

public class InitializeListenerBridge extends GenericBridge {
	private static final String initializationCompleteMethodName = "onInitializationComplete";
	private IInitializationStatusListener _initializationStatusListener;

	public InitializeListenerBridge() {
		super(new HashMap<String, Class[]>(){{
			try {
				put(initializationCompleteMethodName, new Class[]{Class.forName("com.google.android.gms.ads.initialization.InitializationStatus")});
			} catch (ClassNotFoundException e) {
				DeviceLog.debug("Could not find class \"com.google.android.gms.ads.initialization.InitializationStatus\" %s", e.getLocalizedMessage());
			}
		}});
	}

	public String getClassName() {
		return "com.google.android.gms.ads.initialization.OnInitializationCompleteListener";
	}

	public void setStatusListener(IInitializationStatusListener initializationStatusListener) {
		_initializationStatusListener = initializationStatusListener;
	}

	public Object createInitializeListenerProxy() {
		try {
			Object initProxy = Proxy.newProxyInstance(classForName().getClassLoader(),
				new Class<?>[]{classForName()}, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						if (method.getName().equals(initializationCompleteMethodName)) {
							if (_initializationStatusListener != null) {
								// args[0] contains an InitializationStatus object
								_initializationStatusListener.onInitializationComplete(args[0]);
							}
						}
						return null;
					}
				});
			return initProxy;
		} catch (Exception e) {
			DeviceLog.debug("ERROR: Could not create InitializeCompletionListener");
		}
		return null;
	}

}
