package com.unity3d.ads.test.instrumentation.services.ads.operation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.services.ads.operation.load.LoadBannerModule;
import com.unity3d.services.ads.operation.load.LoadBannerOperationState;
import com.unity3d.services.ads.operation.load.LoadModule;
import com.unity3d.services.ads.operation.load.LoadOperation;
import com.unity3d.services.ads.operation.load.LoadOperationState;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.core.request.metrics.AdOperationError;
import com.unity3d.services.core.request.metrics.AdOperationMetric;
import com.unity3d.services.core.request.metrics.ISDKMetrics;
import com.unity3d.services.core.request.metrics.Metric;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocation;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Method;

public class LoadModuleTests {
	private static final String placementId = "TestPlacementId";
	private static final String bannerAdId = "TestBannerAdId";
	private static final int maxWaitTime = 25000;
	private static final int webViewTimeout = 100;
	private static final int testWidth = 11;
	private static final int testHeight = 22;
	private static final UnityBannerSize testBannerSize = new UnityBannerSize(testWidth, testHeight);
	private static final UnityAdsLoadOptions unityAdsLoadOptions = new UnityAdsLoadOptions();

	private IWebViewBridgeInvoker webViewBridgeInvokerMock;
	private IUnityAdsLoadListener loadListenerMock;
	private LoadModule loadModule;
	private ISDKMetrics sdkMetrics;

	final ArgumentCaptor<JSONObject> parametersCaptor = ArgumentCaptor.forClass(JSONObject.class);


	@Before
	public void beforeEachTest() {
		webViewBridgeInvokerMock = mock(IWebViewBridgeInvoker.class);
		loadListenerMock = mock(IUnityAdsLoadListener.class);
		sdkMetrics = mock(ISDKMetrics.class);
		loadModule = new LoadModule(sdkMetrics);
	}

	@Test
	public void executeAdOperationDoesNotContainBannerParameters() throws JSONException {
		LoadBannerOperationState loadBannerOperationState = new LoadBannerOperationState(placementId, bannerAdId, testBannerSize, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		loadModule.executeAdOperation(webViewBridgeInvokerMock, loadBannerOperationState);
		Mockito.verify(webViewBridgeInvokerMock, timeout(maxWaitTime).times(1)).invokeMethod(anyString(), anyString(), any(Method.class), parametersCaptor.capture());
		final JSONObject capturedParameters = parametersCaptor.getValue();
		Assert.assertNull(capturedParameters.opt("width"));
		Assert.assertNull(capturedParameters.opt("height"));
	}
}
