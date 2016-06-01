package com.unity3d.ads.webview.bridge;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.webview.WebViewApp;

import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;

public class WebViewBridge {
	private static HashMap<String, HashMap<String, HashMap<Integer, Method>>> _classTable;
	public static void setClassTable(Class[] apiClassList) {
		if (apiClassList == null)
			return;

		_classTable = new HashMap<>();

		for (Class cls : apiClassList) {
			if (cls == null || !cls.getPackage().getName().startsWith("com.unity3d.ads")) {
				continue;
			}

			HashMap<String, HashMap<Integer, Method>> methodTable = new HashMap<>();
			for (Method method : cls.getMethods()) {
				if (method.getAnnotation(WebViewExposed.class) != null) {
					String methodName = method.getName();

					HashMap<Integer, Method> overrideTable;
					if(methodTable.containsKey(methodName)) {
						overrideTable = methodTable.get(methodName);
					} else {
						overrideTable = new HashMap<>();
					}

					overrideTable.put(Arrays.deepHashCode(method.getParameterTypes()), method);
					methodTable.put(methodName, overrideTable);
				}
			}
			_classTable.put(cls.getName(), methodTable);
		}
	}

	private static Method findMethod(String className, String methodName, Object[] parameters) throws JSONException, NoSuchMethodException {
		HashMap<String, HashMap<Integer, Method>> methodTable;

		if (!_classTable.containsKey(className)) {
			throw new NoSuchMethodException();
		}
		else {
			methodTable = _classTable.get(className);
		}

		HashMap<Integer, Method> overrideTable;
		if(!methodTable.containsKey(methodName)) {
			throw new NoSuchMethodException();
		} else {
			overrideTable = methodTable.get(methodName);
		}

		Class<?>[] types = getTypes(parameters);

		return overrideTable.get(Arrays.deepHashCode(types));
	}

	private static Class<?>[] getTypes(Object[] parameters) throws JSONException {
		Class<?>[] types;
		if(parameters == null) {
			types = new Class[1];
		} else {
			types = new Class[parameters.length + 1];
		}

		if(parameters != null) {
			for (int i = 0; i < parameters.length; i++) {
				types[i] = parameters[i].getClass();
			}
		}

		types[types.length - 1] = WebViewCallback.class;

		return types;
	}

	private static Object[] getValues(Object[] parameters, WebViewCallback callback) throws JSONException {
		Object[] values;
		if(parameters == null) {
			if(callback == null) {
				return null;
			}
			values = new Object[1];
		} else {
			values = new Object[parameters.length + (callback != null ? 1 : 0)];
		}

		if(parameters != null) {
			System.arraycopy(parameters, 0, values, 0, parameters.length);
		}

		if(callback != null) {
			values[values.length - 1] = callback;
		}

		return values;
	}

	public static void handleInvocation(String className, String methodName, Object[] parameters, WebViewCallback callback)
			throws Exception {
		Method method;
		try {
			method = findMethod(className, methodName, parameters);
		} catch(JSONException | NoSuchMethodException e) {
			callback.error(WebViewBridgeError.METHOD_NOT_FOUND, className, methodName, parameters);
			throw e;
		}

		try {
			Object[] values = getValues(parameters, callback);
			method.invoke(null, values);
		} catch (JSONException | InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
			callback.error(WebViewBridgeError.INVOCATION_FAILED, className, methodName, parameters, e.getMessage());
			throw e;
		}
	}

	public static void handleCallback(String callbackId, String callbackStatus, Object[] parameters)
			throws Exception {

		NativeCallback callback = WebViewApp.getCurrentApp().getCallback(callbackId);
		try {
			callback.invoke(callbackStatus, getValues(parameters, null));
		} catch (InvocationTargetException | IllegalAccessException | JSONException | IllegalArgumentException e) {
			DeviceLog.error("Error while invoking method");
			throw e;
		}
	}
}