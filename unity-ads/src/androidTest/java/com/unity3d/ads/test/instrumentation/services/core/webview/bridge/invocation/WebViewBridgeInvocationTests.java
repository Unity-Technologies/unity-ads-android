package com.unity3d.ads.test.instrumentation.services.core.webview.bridge.invocation;

import com.unity3d.ads.test.TestUtilities;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocationCallback;
import com.unity3d.services.core.webview.bridge.invocation.WebViewBridgeInvocation;
import com.unity3d.services.core.webview.bridge.invocation.WebViewBridgeInvocationRunnable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

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
		TestUtilities.SleepCurrentThread(invocationTimeout);
	}

	@Test
	public void invokeCallsWebViewBridgeInvocationSingleThreadedExecutorSubmit() {
		ExecutorService executorMock = mock(ExecutorService.class);
		final WebViewBridgeInvocation webViewBridgeInvocation = new WebViewBridgeInvocation(executorMock, webViewBridgeInvokerMock, invocationCallbackMock);
		webViewBridgeInvocation.invoke(className, methodName, invocationTimeout, params);

		Mockito.verify(executorMock, times(1)).submit(any(WebViewBridgeInvocationRunnable.class));
	}
}
