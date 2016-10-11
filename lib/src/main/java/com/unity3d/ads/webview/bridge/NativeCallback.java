package com.unity3d.ads.webview.bridge;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.webview.WebViewApp;

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

	public NativeCallback (Method callback) {
		_callback = callback;
		_id = _callback.getName().toUpperCase(Locale.US) + "_" + _callbackCount.getAndIncrement();
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
			WebViewApp.getCurrentApp().removeCallback(this);
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
		WebViewApp.getCurrentApp().removeCallback(this);
	}
}
