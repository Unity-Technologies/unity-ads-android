package com.unity3d.ads.test.instrumentation.services.ads.operation;

import com.unity3d.services.ads.operation.AdOperation;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocation;
import com.unity3d.services.core.webview.bridge.invocation.WebViewBridgeInvocation;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class AdOperationTests {
	//Test Implementation of AdOperation
	private class AdOperationTestImplementation extends AdOperation {
		protected AdOperationTestImplementation(IWebViewBridgeInvocation webViewBridgeInvocation, String invocationMethodName) {
			super(webViewBridgeInvocation, invocationMethodName);
		}

		@Override
		public String getId() {
			return UUID.randomUUID().toString();
		}
	}

	private static String invocationMethodName = "testMethodName";
	private static int timeoutLength = 1000;
	private static Object[] testData = new Object[]{"RandomTestDataObjectAsString", 12};

	@Test
	public void invokeCallsWebViewBridgeInvocation() {
		WebViewBridgeInvocation webViewBridgeInvocationMock = mock(WebViewBridgeInvocation.class);
		AdOperationTestImplementation adOperation = new AdOperationTestImplementation(webViewBridgeInvocationMock, invocationMethodName);

		adOperation.invoke(timeoutLength, testData);

		Mockito.verify(webViewBridgeInvocationMock, times(1)).invoke("webview", invocationMethodName, timeoutLength, testData);
	}

	@Test(expected = IllegalArgumentException.class)
	public void AdOperationCallsIllegalArgumentExceptionWhenInvocationIsNull() {
		AdOperationTestImplementation adOperation = new AdOperationTestImplementation(null, invocationMethodName);
		adOperation.invoke(timeoutLength, testData);
	}

	@Test(expected = IllegalArgumentException.class)
	public void AdOperationCallsIllegalArgumentExceptionWhenMethodNameIsNull() {
		AdOperationTestImplementation adOperation = new AdOperationTestImplementation(mock(WebViewBridgeInvocation.class), null);
		adOperation.invoke(timeoutLength, testData);
	}

	@Test
	public void getIdIsNotNull() {
		AdOperationTestImplementation adOperation = new AdOperationTestImplementation(mock(WebViewBridgeInvocation.class), invocationMethodName);
		Assert.assertNotNull(adOperation.getId());
	}
}
