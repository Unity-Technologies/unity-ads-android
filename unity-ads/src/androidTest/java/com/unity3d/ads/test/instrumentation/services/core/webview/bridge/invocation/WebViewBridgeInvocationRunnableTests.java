package com.unity3d.ads.test.instrumentation.services.core.webview.bridge.invocation;

import com.unity3d.ads.test.TestUtilities;
import com.unity3d.services.core.webview.bridge.CallbackStatus;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocationCallback;
import com.unity3d.services.core.webview.bridge.invocation.WebViewBridgeInvocationRunnable;

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

public class WebViewBridgeInvocationRunnableTests {
	private static String className = "ClassName";
	private static String methodName = "MethodName";
	private static int invocationTimeout = 100;
	private static Object params = new Object();
	private static int uiThreadDelay = 10;

	private IWebViewBridgeInvoker webViewBridgeInvokerMock;
	private IWebViewBridgeInvocationCallback invocationCallbackMock;
	private WebViewBridgeInvocationRunnable runnable;

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
	public void runCallsInvocationFailureWhenWebViewBridgeInvokerFails() {
		runnable = new WebViewBridgeInvocationRunnable(invocationCallbackMock, webViewBridgeInvokerMock, className, methodName, invocationTimeout, params);
		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				return false;
			}}).when(webViewBridgeInvokerMock).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));

		runnable.run();
		TestUtilities.SleepCurrentThread(uiThreadDelay);

		VerifyInvocationCallbackMockCalls(invocationCallbackMock, 0, 1, 0, "WebViewBridgeInvocationRunnable:run: invokeMethod failure", null);
	}

	@Test
	public void runCallsInvocationSuccessWhenCallbackStatusOk() {
		runnable = new WebViewBridgeInvocationRunnable(invocationCallbackMock, webViewBridgeInvokerMock, className, methodName, invocationTimeout, params);
		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				runnable.onInvocationComplete(CallbackStatus.OK);
				return true;
			}}).when(webViewBridgeInvokerMock).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));

		runnable.run();
		TestUtilities.SleepCurrentThread(uiThreadDelay);

		VerifyInvocationCallbackMockCalls(invocationCallbackMock, 1, 0, 0);
	}

	@Test
	public void runCallsInvocationFailureWhenCallbackStatusNotOk() {
		runnable = new WebViewBridgeInvocationRunnable(invocationCallbackMock, webViewBridgeInvokerMock, className, methodName, invocationTimeout, params);
		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				runnable.onInvocationComplete(CallbackStatus.ERROR);
				return true;
			}}).when(webViewBridgeInvokerMock).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));

		runnable.run();
		TestUtilities.SleepCurrentThread(uiThreadDelay);

		VerifyInvocationCallbackMockCalls(invocationCallbackMock, 0, 1, 0, "WebViewBridgeInvocationRunnable:run CallbackStatus.Error", CallbackStatus.ERROR);
	}

	@Test
	public void runCallsInvocationTimeoutWhenResponseTimeoutIsReached() {
		runnable = new WebViewBridgeInvocationRunnable(invocationCallbackMock, webViewBridgeInvokerMock, className, methodName, 10, params);
		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				return true;
			}}).when(webViewBridgeInvokerMock).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));

		runnable.run();
		TestUtilities.SleepCurrentThread(100);

		VerifyInvocationCallbackMockCalls(invocationCallbackMock, 0, 0, 1);
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
