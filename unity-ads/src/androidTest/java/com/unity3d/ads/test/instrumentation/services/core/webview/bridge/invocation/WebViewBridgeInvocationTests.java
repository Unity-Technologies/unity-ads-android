package com.unity3d.ads.test.instrumentation.services.core.webview.bridge.invocation;

import com.unity3d.ads.test.TestUtilities;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocationCallback;
import com.unity3d.services.core.webview.bridge.invocation.WebViewBridgeInvocation;
import com.unity3d.services.core.webview.bridge.CallbackStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class WebViewBridgeInvocationTests {
	private static String className = "ClassName";
	private static String methodName = "MethodName";
	private static int invocationTimeout = 100;
	private static Object params = new Object();
	private static int uiThreadDelay = 10;

	private IWebViewBridgeInvoker webViewBridgeInvokerMock;
	private IWebViewBridgeInvocationCallback invocationCallbackMock;

	@Before
	public void beforeEachTest() {
		webViewBridgeInvokerMock = mock(IWebViewBridgeInvoker.class);
		invocationCallbackMock = mock(IWebViewBridgeInvocationCallback.class);
	}

	@After
	public void afterEachTest() {
		TestUtilities.SleepCurrentThread(invocationTimeout);
	}

	@Test
	public void invokeCallsOnSuccessCallbackWhenInvocationCompleteOk() {
		final WebViewBridgeInvocation webViewBridgeInvocation = new WebViewBridgeInvocation(webViewBridgeInvokerMock, invocationCallbackMock);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				webViewBridgeInvocation.onInvocationComplete(CallbackStatus.OK);
				return true;
			}}).when(webViewBridgeInvokerMock).invokeMethod(anyString(),anyString(), any(Method.class),any(Object.class));
		webViewBridgeInvocation.invoke(className, methodName, invocationTimeout, params);
		TestUtilities.SleepCurrentThread(uiThreadDelay);

		VerifyInvocationCallbackMockCalls(invocationCallbackMock, 1, 0, 0);
	}

	@Test
	public void invokeCallsOnFailureCallbackWhenInvocationCompleteError() {
		final WebViewBridgeInvocation webViewBridgeInvocation = new WebViewBridgeInvocation(webViewBridgeInvokerMock, invocationCallbackMock);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				webViewBridgeInvocation.onInvocationComplete(CallbackStatus.ERROR);
				return true;
			}}).when(webViewBridgeInvokerMock).invokeMethod(anyString(),anyString(), any(Method.class),any(Object.class));
		webViewBridgeInvocation.invoke(className, methodName, invocationTimeout, params);
		TestUtilities.SleepCurrentThread(uiThreadDelay);

		VerifyInvocationCallbackMockCalls(invocationCallbackMock, 0,1,0, "WebViewBridgeInvocation:OnInvocationComplete: CallbackStatus.Error", CallbackStatus.ERROR);
	}

	@Test
	public void invokeCallsOnTimeoutCallbackWhenNoInvocationResponse() {
		WebViewBridgeInvocation webViewBridgeInvocation = new WebViewBridgeInvocation(webViewBridgeInvokerMock, invocationCallbackMock);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}}).when(webViewBridgeInvokerMock).invokeMethod(anyString(),anyString(), any(Method.class),any(Object.class));
		webViewBridgeInvocation.invoke(className, methodName, 100, params);
		TestUtilities.SleepCurrentThread(250);

		VerifyInvocationCallbackMockCalls(invocationCallbackMock, 0,0,1);
	}

	@Test(expected = IllegalArgumentException.class)
	public void WebViewBridgeInvocationThrowsNPEWhenInvokedWithNullWebViewBridgeInvoker() {
		new WebViewBridgeInvocation(null, mock(IWebViewBridgeInvocationCallback.class));
	}

	@Test
	public void NoErrorIsThrownWhenWebViewBridgeInvocationIsInitializedWithNullCallbackAndInvokeMethodIsCalledAndFails() {
		final WebViewBridgeInvocation webViewBridgeInvocation = new WebViewBridgeInvocation(webViewBridgeInvokerMock, null);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				webViewBridgeInvocation.onInvocationComplete(CallbackStatus.ERROR);
				return false;
			}}).when(webViewBridgeInvokerMock).invokeMethod(anyString(),anyString(), any(Method.class),any(Object.class));
		webViewBridgeInvocation.invoke(className, methodName, invocationTimeout, params);
		TestUtilities.SleepCurrentThread(uiThreadDelay);
	}

	@Test
	public void invokeCallsOnFailureCallbackWhenInvokerFails() {
		IWebViewBridgeInvocationCallback invocationCallbackMock = mock(IWebViewBridgeInvocationCallback.class);
		final WebViewBridgeInvocation webViewBridgeInvocation = new WebViewBridgeInvocation(webViewBridgeInvokerMock, invocationCallbackMock);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				return false;
			}}).when(webViewBridgeInvokerMock).invokeMethod(anyString(),anyString(), any(Method.class),any(Object.class));
		webViewBridgeInvocation.invoke(className, methodName, invocationTimeout, params);
		TestUtilities.SleepCurrentThread(uiThreadDelay);

		VerifyInvocationCallbackMockCalls(invocationCallbackMock, 0,1,0, "WebViewBridgeInvocation:execute: invokeMethod failure", null);
	}

	private void VerifyInvocationCallbackMockCalls(IWebViewBridgeInvocationCallback webViewBridgeInvocationCallback, int onSuccessCalls, int onFailureCalls, int onTimeoutCalls, String onFailureMessage, CallbackStatus callbackStatus) {
		Mockito.verify(webViewBridgeInvocationCallback, times(onSuccessCalls)).onSuccess();
		Mockito.verify(webViewBridgeInvocationCallback, times(onFailureCalls)).onFailure(onFailureMessage, callbackStatus);
		Mockito.verify(webViewBridgeInvocationCallback, times(onTimeoutCalls)).onTimeout();
	}

	private void VerifyInvocationCallbackMockCalls(IWebViewBridgeInvocationCallback webViewBridgeInvocationCallback, int onSuccessCalls, int onFailureCalls, int onTimeoutCalls) {
		Mockito.verify(webViewBridgeInvocationCallback, times(onSuccessCalls)).onSuccess();
		Mockito.verify(webViewBridgeInvocationCallback, times(onFailureCalls)).onFailure(anyString(), any(CallbackStatus.class));
		Mockito.verify(webViewBridgeInvocationCallback, times(onTimeoutCalls)).onTimeout();
	}
}