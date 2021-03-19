package com.unity3d.ads.test.instrumentation.services.ads.operation;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.services.ads.operation.load.LoadOperationState;
import com.unity3d.services.ads.operation.load.LoadModule;
import com.unity3d.services.ads.operation.load.LoadOperation;
import com.unity3d.services.core.request.ISDKMetricSender;
import com.unity3d.services.core.request.SDKMetricEvents;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class LoadModuleTests {
	private static String placementId = "TestPlacementId";
	private static UnityAds.UnityAdsLoadError loadError = UnityAds.UnityAdsLoadError.INTERNAL_ERROR;
	private static String loadErrorMessage = "LoadErrorMessage";
	private static int uiThreadDelay = 50;
	private static int webViewTimeout = 150;
	private static UnityAdsLoadOptions unityAdsLoadOptions = new UnityAdsLoadOptions();

	private IWebViewBridgeInvoker webViewBridgeInvokerMock;
	private IUnityAdsLoadListener loadListenerMock;
	private ISDKMetricSender sdkMetricSender;
	private LoadModule loadModule;

	@Before
	public void beforeEachTest() {
		webViewBridgeInvokerMock = mock(IWebViewBridgeInvoker.class);
		loadListenerMock = mock(IUnityAdsLoadListener.class);
		sdkMetricSender = mock(ISDKMetricSender.class);
		loadModule = new LoadModule(sdkMetricSender);
	}

	@After
	public void afterEachTest() {
		//Allow for any timeout threads to complete before starting the next test to prevent inaccurate mock counts
		TestUtilities.SleepCurrentThread(webViewTimeout);
	}

	@Test
	public void executeAdOperationCallsOnUnityAdsFailedToLoadWhenPlacementNotSet() {
		LoadOperationState loadOperationState = new LoadOperationState(null, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		loadModule.executeAdOperation(webViewBridgeInvokerMock, loadOperationState);
		TestUtilities.SleepCurrentThread(uiThreadDelay);
		Mockito.verify(loadListenerMock, times(1)).onUnityAdsFailedToLoad(null, UnityAds.UnityAdsLoadError.INVALID_ARGUMENT,"[UnityAds] Placement ID cannot be null");
	}

	@Test
	public void executeAdOperationCallsOnUnityAdsFailedToLoadWhenWebViewBridgeInvocationFails() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				//fail invocation
				return false;
			}
		}).when(webViewBridgeInvokerMock).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));

		loadModule.executeAdOperation(webViewBridgeInvokerMock, loadOperationState);
		TestUtilities.SleepCurrentThread(uiThreadDelay);

		Mockito.verify(loadListenerMock, times(1)).onUnityAdsFailedToLoad(placementId, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, "[UnityAds] Internal communication failure");
		Mockito.verify(sdkMetricSender, times(1)).SendSDKMetricEventWithTag(SDKMetricEvents.native_load_callback_error, new HashMap<String, String> (){{
			put("cbs", "invocationFailure");
		}});
	}

	@Test
	public void executeAdOperationCallsOnUnityAdsFailedToLoadWhenWebViewBridgeInvocationTimesOut() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(50));

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				//Succeed invocation but don't simulate any return call from WebView so the request acknowledgement times out.
				return true;
			}
		}).when(webViewBridgeInvokerMock).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));

		loadModule.executeAdOperation(webViewBridgeInvokerMock, loadOperationState);
		TestUtilities.SleepCurrentThread(100);
		TestUtilities.SleepCurrentThread(150);
		Mockito.verify(loadListenerMock, times(1)).onUnityAdsFailedToLoad(placementId, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, "[UnityAds] Internal communication timeout");
		Mockito.verify(sdkMetricSender, times(1)).SendSDKMetricEvent(eq(SDKMetricEvents.native_load_callback_timeout));
	}

	@Test
	public void executeAdOperationCallsOnUnityAdsFailedToLoadWhenJSONObjectThrowsException() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, null, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		loadModule.executeAdOperation(webViewBridgeInvokerMock, loadOperationState);
		TestUtilities.SleepCurrentThread(uiThreadDelay);

		Mockito.verify(loadListenerMock, times(1)).onUnityAdsFailedToLoad(placementId, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, "[UnityAds] Failed to create load request");
	}

	@Test
	public void executeAdOperationCallsWebViewBridgeInvoker() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		loadModule.executeAdOperation(webViewBridgeInvokerMock, loadOperationState);
		TestUtilities.SleepCurrentThread(uiThreadDelay);
		Mockito.verify(webViewBridgeInvokerMock, times(1)).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));
	}

	@Test
	public void onUnityAdsLoadedCallsLoadEventStateOnUnityAdsAdLoadedWhenSet() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));
		loadModule.set(loadOperation);

		loadModule.onUnityAdsAdLoaded(loadOperation.getId());
		TestUtilities.SleepCurrentThread(uiThreadDelay);
		Mockito.verify(loadListenerMock, times(1)).onUnityAdsAdLoaded(placementId);
	}

	@Test
	public void onUnityAdsLoadedDoesNotCallLoadEventStateOnUnityAdsAdLoadedWhenLoadOperationIsNull() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));
		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));
		loadModule.set(null);

		loadModule.onUnityAdsAdLoaded(loadOperation.getId());
		TestUtilities.SleepCurrentThread(uiThreadDelay);
		//No Verification as there is nothing to mock against.  A null pointer exception is all that is being checked for here.
	}

	@Test
	public void onUnityAdsFailedToLoadCallsLoadEventStateOnUnityAdsFailedToLoadWhenSet() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));

		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));
		loadModule.set(loadOperation);

		loadModule.onUnityAdsFailedToLoad(loadOperation.getId(), loadError, loadErrorMessage);
		TestUtilities.SleepCurrentThread(uiThreadDelay);
		Mockito.verify(loadListenerMock, times(1)).onUnityAdsFailedToLoad(placementId, loadError, loadErrorMessage);
	}

	@Test
	public void onUnityAdsFailedToLoadDoesNotCallLoadEventStateOnUnityAdsFailedToLoadWhenLoadOperationIsNull() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, unityAdsLoadOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(webViewTimeout));
		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));
		loadModule.set(null);

		loadModule.onUnityAdsFailedToLoad(loadOperation.getId(), loadError, loadErrorMessage);
		TestUtilities.SleepCurrentThread(uiThreadDelay);
		//No Verification as there is nothing to mock against.  A null pointer exception is all that is being checked for here.
	}
}
