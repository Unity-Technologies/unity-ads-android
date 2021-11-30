package com.unity3d.ads.test.instrumentation.services.ads.operation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;

import androidx.test.rule.ActivityTestRule;

import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.ads.test.instrumentation.InstrumentationTestActivity;
import com.unity3d.services.ads.operation.show.IShowModule;
import com.unity3d.services.ads.operation.show.ShowModule;
import com.unity3d.services.ads.operation.show.ShowOperation;
import com.unity3d.services.ads.operation.show.ShowOperationState;
import com.unity3d.services.core.request.ISDKMetricSender;
import com.unity3d.services.core.request.SDKMetricEvents;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Method;
import java.util.HashMap;

public class ShowModuleTests {
	private static final String placementId = "TestPlacementId";
	private static final UnityAdsShowOptions showOptions = new UnityAdsShowOptions();

	private static final int showTimeout = 100;
	private static final int maxWaitTime = 25000;

	private IUnityAdsShowListener _showListenerMock;
	private IShowModule _showModule;
	private ISDKMetricSender _sdkMetricSender;
	private IWebViewBridgeInvoker _webViewBridgeInvokerMock;

	@Rule
	public final ActivityTestRule<InstrumentationTestActivity> _activityRule = new ActivityTestRule<>(InstrumentationTestActivity.class);

	@Before
	public void beforeEachTest() {
		_showListenerMock = mock(IUnityAdsShowListener.class);
		_webViewBridgeInvokerMock = mock(IWebViewBridgeInvoker.class);
		_sdkMetricSender = mock(ISDKMetricSender.class);
		_showModule = new ShowModule(_sdkMetricSender);
	}

	@Test
	public void executeAdOperationCallsOnUnityAdsFailedToShowWhenPlacementNotSet() {
		ShowOperationState showOperationState = new ShowOperationState(null, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		_showModule.executeAdOperation(_webViewBridgeInvokerMock, showOperationState);

		Mockito.verify((_showListenerMock), timeout(maxWaitTime).times(1)).onUnityAdsShowFailure("", UnityAds.UnityAdsShowError.INVALID_ARGUMENT, "[UnityAds] Placement ID cannot be null");
	}

	@Test
	public void showModuleCallsOnUnityAdsFailedToShowWhenInvocationCallbackFails() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));

		doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				return false;
			}
		}).when(_webViewBridgeInvokerMock).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));
		_showModule.executeAdOperation(_webViewBridgeInvokerMock, showOperationState);

		Mockito.verify((_showListenerMock), timeout(maxWaitTime).times(1)).onUnityAdsShowFailure(placementId, UnityAds.UnityAdsShowError.INTERNAL_ERROR, "WebViewBridgeInvocationRunnable:run: invokeMethod failure");
		Mockito.verify(_sdkMetricSender, timeout(maxWaitTime).times(1)).SendSDKMetricEventWithTag(SDKMetricEvents.native_show_callback_error, new HashMap<String, String> (){{
			put("cbs", "invocationFailure");
		}});
	}

	@Test
	public void showModuleCallsOnUnityAdsFailedToShowWhenInvocationCallbackTimesOut() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithWebviewTimeout(50));

		doAnswer(new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				//Succeed invocation but don't simulate any return call from WebView so the request acknowledgement times out.
				return true;
			}
		}).when(_webViewBridgeInvokerMock).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));

		_showModule.executeAdOperation(_webViewBridgeInvokerMock, showOperationState);

		Mockito.verify((_showListenerMock), timeout(maxWaitTime).times(1)).onUnityAdsShowFailure(placementId, UnityAds.UnityAdsShowError.INTERNAL_ERROR, "[UnityAds] Show Invocation Timeout");
		Mockito.verify(_sdkMetricSender, timeout(maxWaitTime).times(1)).SendSDKMetricEvent(eq(SDKMetricEvents.native_show_callback_timeout));
	}

	@Test
	public void showModuleCallsOnUnityAdsFailedToShowWhenShowOptionsThrowsNPE() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), null, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		_showModule.executeAdOperation(_webViewBridgeInvokerMock, showOperationState);

		Mockito.verify((_showListenerMock), timeout(maxWaitTime).times(1)).onUnityAdsShowFailure(placementId, UnityAds.UnityAdsShowError.INTERNAL_ERROR, "[UnityAds] Error creating show options");
	}

	@Test
	public void showModuleCallsWebViewBridgeInvoker() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));

		_showModule.executeAdOperation(_webViewBridgeInvokerMock, showOperationState);

		Mockito.verify((_webViewBridgeInvokerMock), timeout(maxWaitTime).times(1)).invokeMethod(anyString(), anyString(), any(Method.class), any(Object.class));
	}

	@Test
	public void onUnityAdsShowFailureCallsListenerOnUnityAdsShowFailure() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		ShowOperation showOperation = new ShowOperation(showOperationState, mock(IWebViewBridgeInvocation.class));

		_showModule.set(showOperation);
		_showModule.onUnityAdsShowFailure(showOperation.getId(), UnityAds.UnityAdsShowError.INTERNAL_ERROR, "ErrorMessage");

		Mockito.verify(_showListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsShowFailure(placementId, UnityAds.UnityAdsShowError.INTERNAL_ERROR, "ErrorMessage");
	}

	@Test
	public void onUnityAdsShowFailureDoesNothingWhenOperationCannotBeFound() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		ShowOperation showOperation = new ShowOperation(showOperationState, mock(IWebViewBridgeInvocation.class));

		_showModule.onUnityAdsShowFailure(showOperation.getId(), UnityAds.UnityAdsShowError.INTERNAL_ERROR, "ErrorMessage");
	}

	@Test
	public void onUnityAdsShowStartCallsListenerOnUnityAdsShowStart() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		ShowOperation showOperation = new ShowOperation(showOperationState, mock(IWebViewBridgeInvocation.class));

		_showModule.set(showOperation);
		_showModule.onUnityAdsShowStart(showOperation.getId());

		Mockito.verify(_showListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsShowStart(placementId);
	}

	@Test
	public void onUnityAdsShowStartDoesNothingWhenOperationCannotBeFound() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		ShowOperation showOperation = new ShowOperation(showOperationState, mock(IWebViewBridgeInvocation.class));

		_showModule.onUnityAdsShowStart(showOperation.getId());
	}

	@Test
	public void onUnityAdsShowClickCallsListenerOnUnityAdsShowClick() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		ShowOperation showOperation = new ShowOperation(showOperationState, mock(IWebViewBridgeInvocation.class));

		_showModule.set(showOperation);
		_showModule.onUnityAdsShowClick(showOperation.getId());

		Mockito.verify(_showListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsShowClick(placementId);
	}

	@Test
	public void onUnityAdsShowClickDoesNothingWhenOperationCannotBeFound() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		ShowOperation showOperation = new ShowOperation(showOperationState, mock(IWebViewBridgeInvocation.class));

		_showModule.onUnityAdsShowClick(showOperation.getId());
	}

	@Test
	public void onUnityAdsShowCompleteCallsListenerOnUnityAdsShowComplete() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		ShowOperation showOperation = new ShowOperation(showOperationState, mock(IWebViewBridgeInvocation.class));

		_showModule.set(showOperation);
		_showModule.onUnityAdsShowComplete(showOperation.getId(), UnityAds.UnityAdsShowCompletionState.COMPLETED);

		Mockito.verify(_showListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsShowComplete(placementId, UnityAds.UnityAdsShowCompletionState.COMPLETED);
	}

	@Test
	public void onUnityAdsShowCompleteDoesNothingWhenOperationCannotBeFound() {
		ShowOperationState showOperationState = new ShowOperationState(placementId, _showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		ShowOperation showOperation = new ShowOperation(showOperationState, mock(IWebViewBridgeInvocation.class));

		_showModule.onUnityAdsShowComplete(showOperation.getId(), UnityAds.UnityAdsShowCompletionState.COMPLETED);
	}
}
