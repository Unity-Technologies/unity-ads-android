package com.unity3d.services.ads.gmascar.bridges;

import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class GenericBridge {

	private String _className;
	private Map<String, Class[]> _functionAndParameters;
	private Map<String, Method> _methodMap;
	private boolean _methodMapBuilt = false;

	protected abstract String getClassName();

	public GenericBridge(Map<String, Class[]> functionAndParameters) {
		_className = getClassName();
		_functionAndParameters = functionAndParameters;
		_methodMap = new HashMap<>();
		buildMethodMap();
	}

	public Map<String,Class[]> getFunctionMap() {
		return _functionAndParameters;
	}

	public Class classForName() {
		try {
			Class getClass = Class.forName(_className);
			if (getClass != null) {
				return getClass;
			} else {
				return null;
			}
		} catch (ClassNotFoundException e) {
			DeviceLog.debug("ERROR: Could not find GMA SDK Class %s %s", _className, e.getLocalizedMessage());
			return null;
		}
	}

	public boolean exists() {
		if (classForName() == null) {
			DeviceLog.debug("ERROR: Could not find class %s", _className);
			return false;
		}

		if (!_methodMapBuilt) {
			buildMethodMap();
		}
		if (_methodMap.size() != getFunctionMap().size()) {
			return false;
		}

		return true;
	}

	private void buildMethodMap() {
		boolean methodMapNoErrors = true;
		for (Map.Entry<String, Class[]> entry : getFunctionMap().entrySet()) {
			Class[] parameterClasses = entry.getValue();
			try {
				Method method = getReflectiveMethod(classForName(), entry.getKey(), parameterClasses);
				if (method != null) {
					_methodMap.put(entry.getKey(), method);
				}
			} catch (Exception e) {
				DeviceLog.debug("ERROR: Could not find %s class with method %s and parameters : %s", _className, entry.getKey(), parameterClasses);
				methodMapNoErrors = false;
			}
		}
		_methodMapBuilt = methodMapNoErrors;
	}

	private Method getMethod(String methodName) {
		return _methodMap.get(methodName);
	}

	private Method getReflectiveMethod(Class methodClass, String methodName, Class... parameterClasses) {
		Method method = null;
		try {
			method = methodClass.getDeclaredMethod(methodName, parameterClasses);
		} catch (Exception e) {
			DeviceLog.debug("ERROR: Could not find method %s in %s", methodName, methodClass.getName() +
				" " + e.getLocalizedMessage());
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.GMA, GMAEvent.METHOD_ERROR);
		} finally {
			return method;
		}
	}

	public void callVoidMethod(String methodName, Object callingObj, Object... parameters) {
		Method method = getMethod(methodName);
		if (method == null) {
			DeviceLog.debug("ERROR: Could not find method %s", methodName);
			return;
		}

		try {
			method.invoke(callingObj, parameters);
		} catch (Exception e) {
			DeviceLog.debug("ERROR: Could not invoke method %s : %s", methodName, e.getLocalizedMessage());
		}
	}

	public Object callNonVoidMethod(String methodName, Object callingObj, Object... parameters) {
		Method method = getMethod(methodName);
		if (method == null) {
			DeviceLog.debug("ERROR: Could not find method %s", methodName);
			return null;
		}

		try {
			return method.invoke(callingObj, parameters);
		} catch (Exception e) {
			DeviceLog.debug("ERROR: Could not invoke method %s : %s", methodName, e.getLocalizedMessage());
		}
		return null;

	}

}
