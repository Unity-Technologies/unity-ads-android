package com.unity3d.ads.webview.bridge;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.webview.WebViewApp;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Invocation {
	private static AtomicInteger _idCount = new AtomicInteger(0);
	private static Map<Integer, Invocation> _invocationSets;
	private ArrayList<ArrayList<Object>> _invocations;
	private ArrayList<ArrayList<Object>> _responses;
	private int _invocationId;

	public Invocation() {
		_invocationId = _idCount.getAndIncrement();

		if (_invocationSets == null) {
			_invocationSets = new HashMap<>();
		}

		_invocationSets.put(_invocationId, this);
	}

	public void addInvocation (String className, String methodName, Object[] parameters, WebViewCallback callback) {
		if (_invocations == null) _invocations = new ArrayList<>();

		ArrayList<Object> invocation = new ArrayList<>();
		invocation.add(className);
		invocation.add(methodName);
		invocation.add(parameters);
		invocation.add(callback);

		_invocations.add(invocation);

	}

	public boolean nextInvocation () {
		if (_invocations != null && _invocations.size() > 0) {
			ArrayList<Object> invocation = _invocations.remove(0);

			try {
				WebViewBridge.handleInvocation((String)invocation.get(0), (String)invocation.get(1), (Object[])invocation.get(2), (WebViewCallback)invocation.get(3));
			}
			catch (Exception e) {
				DeviceLog.exception("Error handling invocation", e);
			}

			return true;
		}

		return false;
	}

	public void setInvocationResponse (CallbackStatus status, Enum error, Object... params) {
		if (_responses == null) _responses = new ArrayList<>();
		ArrayList<Object> response = new ArrayList<>();
		response.add(status);
		response.add(error);
		response.add(params);
		_responses.add(response);
	}

	public void sendInvocationCallback() {
		_invocationSets.remove(getId());
		WebViewApp.getCurrentApp().invokeCallback(this);
	}

	public int getId () {
		return _invocationId;
	}

	public ArrayList<ArrayList<Object>> getResponses () {
		return _responses;
	}

	public static synchronized Invocation getInvocationById(int id) {
		if (_invocationSets != null && _invocationSets.containsKey(id)) {
			return _invocationSets.get(id);
		}

		return null;
	}
}
