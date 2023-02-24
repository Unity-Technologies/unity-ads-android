package com.unity3d.services.core.webview.bridge.invocation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutorService;

public class WebViewBridgeInvocationTests {
	private static String className = "ClassName";
	private static String methodName = "MethodName";
	private static int invocationTimeout = 100;
	private static Object params = new Object();

	private IWebViewBridgeInvoker webViewBridgeInvokerMock;
	private IWebViewBridgeInvocationCallback invocationCallbackMock;

	@Before
	public void beforeEachTest() {
		webViewBridgeInvokerMock = mock(IWebViewBridgeInvoker.class);
		invocationCallbackMock = mock(IWebViewBridgeInvocationCallback.class);
	}

	@After
	public void afterEachTest() {
		try {
			Thread.sleep(invocationTimeout);
		} catch (InterruptedException ignored) { }
	}

	@Test
	public void invokeCallsWebViewBridgeInvocationSingleThreadedExecutorSubmit() {
		ExecutorService executorMock = mock(ExecutorService.class);
		final WebViewBridgeInvocation webViewBridgeInvocation = new WebViewBridgeInvocation(executorMock, webViewBridgeInvokerMock, invocationCallbackMock);
		webViewBridgeInvocation.invoke(className, methodName, invocationTimeout, params);

		Mockito.verify(executorMock, times(1)).submit(any(WebViewBridgeInvocationRunnable.class));
	}
}
