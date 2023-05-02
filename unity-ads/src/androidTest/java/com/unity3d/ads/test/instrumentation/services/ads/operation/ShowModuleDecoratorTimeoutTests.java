package com.unity3d.ads.test.instrumentation.services.ads.operation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import androidx.test.rule.ActivityTestRule;

import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.ads.test.instrumentation.InstrumentationTestActivity;
import com.unity3d.services.ads.operation.show.IShowModule;
import com.unity3d.services.ads.operation.show.ShowModule;
import com.unity3d.services.ads.operation.show.ShowModuleDecoratorTimeout;
import com.unity3d.services.ads.operation.show.ShowOperationState;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;
import com.unity3d.services.core.webview.bridge.CallbackStatus;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.WebViewBridgeInvocationRunnable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

public class ShowModuleDecoratorTimeoutTests {
	private static final String placementId = "TestPlacementId";
	private static final UnityAdsShowOptions showOptions = new UnityAdsShowOptions();

	private static final int showTimeout = 50;
	private static final int maxWaitTime = 25000;

	private IUnityAdsShowListener showListenerMock;
	private IShowModule showModule;
	private SDKMetricsSender sdkMetricsMock;
	private ConfigurationReader configurationReaderMock;

	@Rule
	public final ActivityTestRule<InstrumentationTestActivity> _activityRule = new ActivityTestRule<>(InstrumentationTestActivity.class);

	@Before
	public void beforeEachTest() {
		showListenerMock = mock(IUnityAdsShowListener.class);
		sdkMetricsMock = mock(SDKMetricsSender.class);
		configurationReaderMock = mock(ConfigurationReader.class);
		// We need a real instance since ShowModule will create the Operation object (which holds the State with Listener ID)
		showModule = new ShowModule(sdkMetricsMock);

		Mockito.when(configurationReaderMock.getCurrentConfiguration()).thenReturn(new Configuration());
	}

	@Test
	public void testShowModuleDecoratorTimeout() {
		ShowModuleDecoratorTimeout showModuleDecoratorTimeout = new ShowModuleDecoratorTimeout(showModule, configurationReaderMock);
		ShowOperationState showOperationState = new ShowOperationState(placementId, showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		showModuleDecoratorTimeout.executeAdOperation(mock(IWebViewBridgeInvoker.class), showOperationState);

		Mockito.verify(showListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsShowFailure(placementId, UnityAds.UnityAdsShowError.TIMEOUT, "[UnityAds] Timeout while trying to show TestPlacementId");
	}

	@Test
	public void testShowModuleDecoratorShowConsentNoTimeout() {
		ShowModuleDecoratorTimeout showModuleDecoratorTimeout = new ShowModuleDecoratorTimeout(showModule, configurationReaderMock);
		ShowOperationState showOperationState = new ShowOperationState(placementId, showListenerMock, _activityRule.getActivity(), showOptions, OperationTestUtilities.createConfigurationWithShowTimeout(showTimeout));
		IWebViewBridgeInvoker webViewBridgeInvoker = mock(IWebViewBridgeInvoker.class);
		when(webViewBridgeInvoker.invokeMethod(anyString(), anyString(), any(Method.class), any())).thenReturn(true);

		showModuleDecoratorTimeout.executeAdOperation(webViewBridgeInvoker, showOperationState);
		WebViewBridgeInvocationRunnable.onInvocationComplete(CallbackStatus.OK);
		showModuleDecoratorTimeout.onUnityAdsShowConsent(showOperationState.getId());
		Mockito.verify(showListenerMock, times(0)).onUnityAdsShowFailure(anyString(), any(UnityAds.UnityAdsShowError.class), anyString());
	}
}
