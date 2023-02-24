package com.unity3d.services.core.webview.bridge;

import com.unity3d.services.core.log.DeviceLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class NativeCallback {
	private static AtomicInteger _callbackCount = new AtomicInteger(0);
	private Method _callback;
	private String _id;
	private final INativeCallbackSubject _nativeCallbackSubject;

	public NativeCallback (Method callback) {
		this(callback, SharedInstances.INSTANCE.getWebViewAppNativeCallbackSubject());
	}

	public NativeCallback(Method callback, INativeCallbackSubject nativeCallbackSubject) {
		_callback = callback;
		_id = _callback.getName().toUpperCase(Locale.US) + "_" + _callbackCount.getAndIncrement();

		_nativeCallbackSubject = nativeCallbackSubject;
	}

	public String getId () {
		return _id;
	}

	public void invoke (String status, Object... values) throws InvocationTargetException, IllegalAccessException, IllegalArgumentException {
		CallbackStatus cbs;

		try {
			cbs = CallbackStatus.valueOf(status);
		}
		catch (Exception e) {
			DeviceLog.error("Illegal status");
			_nativeCallbackSubject.remove(this);
			throw e;
		}

		if (values == null) {
			values = new Object[]{cbs};
		}
		else {
			ArrayList<Object> tmpAr = new ArrayList(Arrays.asList(values));
			tmpAr.add(0, cbs);
			values = tmpAr.toArray();
		}

		_callback.invoke(null, values);
		_nativeCallbackSubject.remove(this);
	}
}
