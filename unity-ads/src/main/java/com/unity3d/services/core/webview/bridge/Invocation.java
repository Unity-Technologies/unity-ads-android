package com.unity3d.services.core.webview.bridge;

import com.unity3d.services.core.log.DeviceLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Invocation {
	private static AtomicInteger _idCount = new AtomicInteger(0);
	private static Map<Integer, Invocation> _invocationSets;
	private ArrayList<ArrayList<Object>> _invocations;
	private ArrayList<ArrayList<Object>> _responses;
	private int _invocationId;
	private IInvocationCallbackInvoker _invocationCallbackInvoker;
  private final IWebViewBridge _webViewBridge;

	public Invocation() {
		this(SharedInstances.INSTANCE.getWebViewAppInvocationCallbackInvoker(), SharedInstances.INSTANCE.getWebViewBridge());
	}

	public Invocation(IWebViewBridge webViewBridge) {
		this(SharedInstances.INSTANCE.getWebViewAppInvocationCallbackInvoker(), webViewBridge);
	}

	public Invocation(IInvocationCallbackInvoker invocationHandler, IWebViewBridge webViewBridge) {
		_invocationCallbackInvoker = invocationHandler;
    _webViewBridge = webViewBridge;
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

			String className = (String) invocation.get(0);
			String methodName = (String) invocation.get(1);
			Object[] params = (Object[]) invocation.get(2);
			WebViewCallback callback = (WebViewCallback)invocation.get(3);

			try {
				_webViewBridge.handleInvocation(className, methodName, params, callback);
			}
			catch (Exception e) {
				DeviceLog.exception(String.format("Error handling invocation %s.%s(%s)", className, methodName, Arrays.toString(params)), e);
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
		_invocationCallbackInvoker.invokeCallback(this);
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
