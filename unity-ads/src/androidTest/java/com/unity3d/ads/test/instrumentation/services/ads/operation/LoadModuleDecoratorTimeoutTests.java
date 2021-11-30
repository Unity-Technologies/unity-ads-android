package com.unity3d.ads.test.instrumentation.services.ads.operation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.services.ads.operation.load.ILoadModule;
import com.unity3d.services.ads.operation.load.LoadModuleDecoratorTimeout;
import com.unity3d.services.ads.operation.load.LoadOperationState;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LoadModuleDecoratorTimeoutTests {
	private static final String placementId = "TestPlacementId";
	private static final UnityAds.UnityAdsLoadError loadError = UnityAds.UnityAdsLoadError.INTERNAL_ERROR;
	private static final String loadErrorMessage = "LoadErrorMessage";
	private static final UnityAdsLoadOptions loadOptions = new UnityAdsLoadOptions();

	private static final int loadTimeout = 50;
	private static final int loadTimeoutExpireMs = loadTimeout * 10;
	private static final int maxWaitTime = 25000;

	private IUnityAdsLoadListener loadListenerMock;
	private ILoadModule loadModuleMock;

	@Before
	public void beforeEachTest() {
		loadListenerMock = mock(IUnityAdsLoadListener.class);
		loadModuleMock = mock(ILoadModule.class);
	}

	@Test
	public void onUnityAdsFailedToLoadIsCalledWhenTimeoutIsReached() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, loadOptions, OperationTestUtilities.createConfigurationWithLoadTimeout(loadTimeout));
		LoadModuleDecoratorTimeout timeoutDecorator = new LoadModuleDecoratorTimeout(loadModuleMock);

		timeoutDecorator.executeAdOperation(mock(IWebViewBridgeInvoker.class), loadOperationState);
		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsFailedToLoad(placementId, UnityAds.UnityAdsLoadError.TIMEOUT, "[UnityAds] Timeout while loading " + placementId);
	}

	@Test
	public void onUnityAdsAdLoadedAndOnUnityAdsAdFailedToLoadIsNotCalledAgainWhenTimeoutHasBeenReached() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, loadOptions, OperationTestUtilities.createConfigurationWithLoadTimeout(loadTimeout));
		LoadModuleDecoratorTimeout timeoutDecorator = new LoadModuleDecoratorTimeout(loadModuleMock);

		timeoutDecorator.executeAdOperation(mock(IWebViewBridgeInvoker.class), loadOperationState);
		TestUtilities.SleepCurrentThread(loadTimeoutExpireMs);
		timeoutDecorator.onUnityAdsAdLoaded(placementId);
		timeoutDecorator.onUnityAdsFailedToLoad(placementId, loadError, loadErrorMessage);

		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsFailedToLoad(placementId, UnityAds.UnityAdsLoadError.TIMEOUT, "[UnityAds] Timeout while loading " + placementId);
		Mockito.verify(loadListenerMock, times(0)).onUnityAdsAdLoaded(anyString());
	}

	@Test
	public void onUnityAdsAdFailedToLoadIsNotCalledWhenOnUnityAdsAdLoadedIsCalledBeforeTimeout() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, loadOptions, OperationTestUtilities.createConfigurationWithLoadTimeout(loadTimeout));
		LoadModuleDecoratorTimeout timeoutDecorator = new LoadModuleDecoratorTimeout(loadModuleMock);

		timeoutDecorator.executeAdOperation(mock(IWebViewBridgeInvoker.class), loadOperationState);
		TestUtilities.SleepCurrentThread(25);
		timeoutDecorator.onUnityAdsAdLoaded(placementId);

		Mockito.verify(loadModuleMock, times(1)).onUnityAdsAdLoaded(placementId);
		Mockito.verify(loadModuleMock, times(0)).onUnityAdsFailedToLoad(anyString(), any(UnityAds.UnityAdsLoadError.class), anyString());
	}

	@Test
	public void noNPEIsThrownWhenOnUnityAdsAdLoadedIsCalledWithoutCallingExecuteAdOperation() {
		LoadModuleDecoratorTimeout timeoutDecorator = new LoadModuleDecoratorTimeout(loadModuleMock);
		timeoutDecorator.onUnityAdsAdLoaded(placementId);
		Mockito.verify(loadModuleMock, times(0)).onUnityAdsFailedToLoad(anyString(), any(UnityAds.UnityAdsLoadError.class), anyString());
	}

	@Test
	public void noNPEIsThrownWhenOnUnityAdsAdFailedToLoadIsCalledWithoutCallingExecuteAdOperation() {
		LoadModuleDecoratorTimeout timeoutDecorator = new LoadModuleDecoratorTimeout(loadModuleMock);
		timeoutDecorator.onUnityAdsFailedToLoad(placementId, loadError, loadErrorMessage);
		Mockito.verify(loadModuleMock, times(0)).onUnityAdsAdLoaded(anyString());
	}
}
