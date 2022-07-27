package com.unity3d.ads.test.instrumentation.services.ads.operation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.services.ads.operation.load.LoadModule;
import com.unity3d.services.ads.operation.load.LoadOperation;
import com.unity3d.services.ads.operation.load.LoadOperationState;
import com.unity3d.services.core.request.metrics.AdOperationError;
import com.unity3d.services.core.request.metrics.AdOperationMetric;
import com.unity3d.services.core.request.metrics.ISDKMetrics;
import com.unity3d.services.core.request.metrics.Metric;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;

public class LoadModuleTests {
	private static final String placementId = "TestPlacementId";
	private static final UnityAds.UnityAdsLoadError loadError = UnityAds.UnityAdsLoadError.INTERNAL_ERROR;
	private static final String loadErrorMessage = "LoadErrorMessage";
	private static final int maxWaitTime = 25000;
	private static final int webViewTimeout = 100;
	private static final int loadTimeoutExpireMs = webViewTimeout + 10;
	private static final UnityAdsLoadOptions unityAdsLoadOptions = new UnityAdsLoadOptions();

	private IWebViewBridgeInvoker webViewBridgeInvokerMock;
	private IUnityAdsLoadListener loadListenerMock;
	private LoadModule loadModule;
	private ISDKMetrics sdkMetrics;

	@Before
	public void beforeEachTest() {
		webViewBridgeInvokerMock = mock(IWebViewBridgeInvoker.class);
		loadListenerMock = mock(IUnityAdsLoadListener.class);
		sdkMetrics = mock(ISDKMetrics.class);
		loadModule = new LoadModule(sdkMetrics);
	}

	@Test
	public void executeAdOperationCallsOnUnityAdsFailedToLoadWhenPlacementNotSet() {
		LoadOperationState loadOperationState = new LoadOperationState(null, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		loadModule.executeAdOperation(webViewBridgeInvokerMock, loadOperationState);
		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsFailedToLoad("", UnityAds.UnityAdsLoadError.INVALID_ARGUMENT,"[UnityAds] Placement ID cannot be null");
	}

	@Test
	public void executeAdOperationCallsOnUnityAdsFailedToLoadWhenWebViewBridgeInvocationFails() {
		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass(Metric.class);
		final Metric desiredMetric = AdOperationMetric.newAdLoadFailure(AdOperationError.callback_error, 0L);
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				//fail invocation
				return false;
			}
		}).when(webViewBridgeInvokerMock).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));

		loadModule.executeAdOperation(webViewBridgeInvokerMock, loadOperationState);

		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsFailedToLoad(placementId, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, "[UnityAds] Internal communication failure");
		Mockito.verify(sdkMetrics, timeout(maxWaitTime).times(1)).sendMetricWithInitState(metricsCaptor.capture());
		final Metric capturedMetric = metricsCaptor.getValue();
		Assert.assertEquals(desiredMetric.getName(), capturedMetric.getName());
	}

	@Test
	public void executeAdOperationCallsOnUnityAdsFailedToLoadWhenWebViewBridgeInvocationTimesOut() {
		final ArgumentCaptor<Metric> metricsCaptor = ArgumentCaptor.forClass((Class) Metric.class);
		final Metric desiredMetric = AdOperationMetric.newAdLoadFailure(AdOperationError.callback_timeout, 0L);
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(50));

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				//Succeed invocation but don't simulate any return call from WebView so the request acknowledgement times out.
				return true;
			}
		}).when(webViewBridgeInvokerMock).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));

		loadModule.executeAdOperation(webViewBridgeInvokerMock, loadOperationState);

		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsFailedToLoad(placementId, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, "[UnityAds] Internal communication timeout");
		Mockito.verify(sdkMetrics, timeout(maxWaitTime).times(1)).sendMetricWithInitState(metricsCaptor.capture());
		final Metric capturedMetric = metricsCaptor.getValue();
		Assert.assertEquals(desiredMetric.getName(), capturedMetric.getName());

	}

	@Test
	public void executeAdOperationCallsOnUnityAdsFailedToLoadWhenJSONObjectThrowsException() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, null, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		loadModule.executeAdOperation(webViewBridgeInvokerMock, loadOperationState);

		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsFailedToLoad(placementId, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, "[UnityAds] Failed to create load request");
	}

	@Test
	public void executeAdOperationCallsWebViewBridgeInvoker() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		loadModule.executeAdOperation(webViewBridgeInvokerMock, loadOperationState);
		Mockito.verify(webViewBridgeInvokerMock, timeout(maxWaitTime).times(1)).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));
	}

	@Test
	public void onUnityAdsLoadedCallsLoadEventStateOnUnityAdsAdLoadedWhenSet() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));
		loadModule.set(loadOperation);

		loadModule.onUnityAdsAdLoaded(loadOperation.getId());
		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsAdLoaded(placementId);
	}

	@Test
	public void onUnityAdsLoadedDoesNotCallLoadEventStateOnUnityAdsAdLoadedWhenLoadOperationIsNull() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));
		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));
		loadModule.set(null);

		loadModule.onUnityAdsAdLoaded(loadOperation.getId());
		//No Verification as there is nothing to mock against.  A null pointer exception is all that is being checked for here.
	}

	@Test
	public void onUnityAdsFailedToLoadCallsLoadEventStateOnUnityAdsFailedToLoadWhenSet() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));
		loadModule.set(loadOperation);

		loadModule.onUnityAdsFailedToLoad(loadOperation.getId(), loadError, loadErrorMessage);
		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsFailedToLoad(placementId, loadError, loadErrorMessage);
	}

	@Test
	public void onUnityAdsFailedToLoadDoesNotCallLoadEventStateOnUnityAdsFailedToLoadWhenLoadOperationIsNull() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));
		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));
		loadModule.set(null);

		loadModule.onUnityAdsFailedToLoad(loadOperation.getId(), loadError, loadErrorMessage);
		//No Verification as there is nothing to mock against.  A null pointer exception is all that is being checked for here.
	}
}
