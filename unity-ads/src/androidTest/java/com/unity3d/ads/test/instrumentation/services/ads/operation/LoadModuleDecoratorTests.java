package com.unity3d.ads.test.instrumentation.services.ads.operation;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.operation.load.ILoadModule;
import com.unity3d.services.ads.operation.load.LoadOperationState;
import com.unity3d.services.ads.operation.load.LoadModuleDecorator;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class LoadModuleDecoratorTests {
	private static String testId = "TestId";

	private ILoadModule loadModuleMock;

	@Before
	public void beforeEachTest() {
		loadModuleMock = mock(ILoadModule.class);
	}

	@Test
	public void executeAdOperationCallsLoadModuleExecuteAdOperation() {
		LoadOperationState loadOperationStateMock = mock(LoadOperationState.class);
		IWebViewBridgeInvoker webViewBridgeInvokerMock = mock(IWebViewBridgeInvoker.class);
		LoadModuleDecorator loadModuleDecorator = new LoadModuleDecorator(loadModuleMock);

		loadModuleDecorator.executeAdOperation(webViewBridgeInvokerMock, loadOperationStateMock);
		Mockito.verify(loadModuleMock, times(1)).executeAdOperation(webViewBridgeInvokerMock, loadOperationStateMock);
	}

	@Test
	public void onUnityAdsLoadedCallsLoadModuleOnUnityAdsLoaded() {
		LoadModuleDecorator loadModuleDecorator = new LoadModuleDecorator(loadModuleMock);
		loadModuleDecorator.onUnityAdsAdLoaded(testId);
		Mockito.verify(loadModuleMock, times(1)).onUnityAdsAdLoaded(testId);
	}

	@Test
	public void onUnityAdsFailedToLoadCallsLoadModuleOnUnityAdsFailedToLoad() {
		LoadModuleDecorator loadModuleDecorator = new LoadModuleDecorator(loadModuleMock);
		loadModuleDecorator.onUnityAdsFailedToLoad(testId, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, "ErrorMessage");
		Mockito.verify(loadModuleMock, times(1)).onUnityAdsFailedToLoad(testId, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, "ErrorMessage");
	}
}
