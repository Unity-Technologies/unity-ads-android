package com.unity3d.services.ads.operation;

import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocation;

import java.util.UUID;

public abstract class AdOperation implements IAdOperation {
	private static String invocationClassName = "webview";

	private IWebViewBridgeInvocation _webViewBridgeInvocation;
	private String _invocationMethodName;

	protected AdOperation(IWebViewBridgeInvocation webViewBridgeInvocation, String invocationMethodName) throws NullPointerException {
		_invocationMethodName = invocationMethodName;
		if (_invocationMethodName == null || _invocationMethodName == "") {
			throw new IllegalArgumentException("invocationMethodName cannot be null");
		}

		_webViewBridgeInvocation = webViewBridgeInvocation;
		if (_webViewBridgeInvocation == null) {
			throw new IllegalArgumentException("webViewBridgeInvocation cannot be null");
		}
	}

	@Override
	public void invoke(final int timeout, final Object... invocationParameters) {
		_webViewBridgeInvocation.invoke(invocationClassName, _invocationMethodName, timeout, invocationParameters);
	}
}
