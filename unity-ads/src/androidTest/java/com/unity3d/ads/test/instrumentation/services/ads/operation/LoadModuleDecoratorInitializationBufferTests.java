package com.unity3d.ads.test.instrumentation.services.ads.operation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.services.ads.operation.load.ILoadModule;
import com.unity3d.services.ads.operation.load.LoadModuleDecoratorInitializationBuffer;
import com.unity3d.services.ads.operation.load.LoadOperationState;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ErrorState;
import com.unity3d.services.core.configuration.IInitializationNotificationCenter;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.metrics.ISDKMetrics;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LoadModuleDecoratorInitializationBufferTests {
	private static final String testPlacementId = "TestPlacementId";
	private static final int maxWaitTime = 25000;

	private ILoadModule loadModuleMock = mock(ILoadModule.class);
	private IInitializationNotificationCenter initializationNotificationCenterMock;
	private IWebViewBridgeInvoker webViewBridgeInvokerMock;
	private LoadOperationState loadOperationStateMock;
	private ISDKMetrics sdkMetrics;

	@Before
	public void beforeEachTest() {
		loadModuleMock = mock(ILoadModule.class);
		initializationNotificationCenterMock = mock(IInitializationNotificationCenter.class);
		webViewBridgeInvokerMock = mock(IWebViewBridgeInvoker.class);
		loadOperationStateMock = mock(LoadOperationState.class);
		sdkMetrics = mock(ISDKMetrics.class);
	}

	@Test
	public void executeAdOperationCallsLoadModuleExecuteAdOperationWhenSdkIsInitialized() {
		LoadModuleDecoratorInitializationBuffer loadModuleInitBuffer = new LoadModuleDecoratorInitializationBuffer(loadModuleMock, initializationNotificationCenterMock);
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);

		loadModuleInitBuffer.executeAdOperation(webViewBridgeInvokerMock, loadOperationStateMock);

		Mockito.verify(loadModuleMock, times(1)).executeAdOperation(webViewBridgeInvokerMock, loadOperationStateMock);
	}

	@Test
	public void executeAdOperationCallsListenerOnUnityAdsFailedToLoadWhenInitHasFailed() {
		Mockito.when(loadModuleMock.getMetricSender()).thenReturn(sdkMetrics);
		IUnityAdsLoadListener loadListenerMock = mock(IUnityAdsLoadListener.class);
		LoadOperationState loadOperationState = new LoadOperationState(testPlacementId, loadListenerMock, new UnityAdsLoadOptions(), new Configuration());
		LoadModuleDecoratorInitializationBuffer loadModuleInitBuffer = new LoadModuleDecoratorInitializationBuffer(loadModuleMock, initializationNotificationCenterMock);
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_FAILED);

		loadModuleInitBuffer.executeAdOperation(webViewBridgeInvokerMock, loadOperationState);

		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsFailedToLoad(testPlacementId, UnityAds.UnityAdsLoadError.INITIALIZE_FAILED, "[UnityAds] SDK Initialization Failed");
	}

	@Test
	public void executeAdOperationCallsInitNotificationCenterWhenSdkIsInitializing() {
		LoadModuleDecoratorInitializationBuffer loadModuleInitBuffer = new LoadModuleDecoratorInitializationBuffer(loadModuleMock, initializationNotificationCenterMock);
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);

		loadModuleInitBuffer.executeAdOperation(webViewBridgeInvokerMock, loadOperationStateMock);

		Mockito.verify(initializationNotificationCenterMock, times(1)).addListener(loadModuleInitBuffer);
	}

	@Test
	public void loadModuleExecuteAdOperationIsCalledWhenOnSdkInitializedIsCalledAfterCallingExecuteAdOperation() {
		LoadModuleDecoratorInitializationBuffer loadModuleInitBuffer = new LoadModuleDecoratorInitializationBuffer(loadModuleMock, initializationNotificationCenterMock);
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);

		loadModuleInitBuffer.executeAdOperation(webViewBridgeInvokerMock, loadOperationStateMock);
		Mockito.verify(initializationNotificationCenterMock, times(1)).addListener(loadModuleInitBuffer);

		loadModuleInitBuffer.onSdkInitialized();
		Mockito.verify(loadModuleMock, times(1)).executeAdOperation(webViewBridgeInvokerMock, loadOperationStateMock);
	}

	@Test
	public void loadModuleExecuteAdOperationIsNotCalledWhenOnSdkInitializedIsCalledAfterCallingExecuteAdOperationAndLoadEventStateNull() {
		LoadModuleDecoratorInitializationBuffer loadModuleInitBuffer = new LoadModuleDecoratorInitializationBuffer(loadModuleMock, initializationNotificationCenterMock);
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);

		loadModuleInitBuffer.executeAdOperation(webViewBridgeInvokerMock, null);
		Mockito.verify(initializationNotificationCenterMock, times(1)).addListener(loadModuleInitBuffer);

		loadModuleInitBuffer.onSdkInitialized();
		Mockito.verify(loadModuleMock, times(0)).executeAdOperation(webViewBridgeInvokerMock, loadOperationStateMock);
	}

	@Test
	public void onUnityAdsFailedToLoadIsCalledWhenOnSdkInitializationFailedIsCalledAfterCallingExecuteAdOperation() {
		Mockito.when(loadModuleMock.getMetricSender()).thenReturn(sdkMetrics);
		IUnityAdsLoadListener loadListenerMock = mock(IUnityAdsLoadListener.class);
		LoadOperationState loadOperationStateMock = new LoadOperationState(testPlacementId, loadListenerMock, new UnityAdsLoadOptions(), new Configuration());
		LoadModuleDecoratorInitializationBuffer loadModuleInitBuffer = new LoadModuleDecoratorInitializationBuffer(loadModuleMock, initializationNotificationCenterMock);
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);

		loadModuleInitBuffer.executeAdOperation(webViewBridgeInvokerMock, loadOperationStateMock);
		Mockito.verify(initializationNotificationCenterMock, times(1)).addListener(loadModuleInitBuffer);

		loadModuleInitBuffer.onSdkInitializationFailed("UntestableMessage", ErrorState.InitModules, 0);
		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsFailedToLoad(testPlacementId, UnityAds.UnityAdsLoadError.INITIALIZE_FAILED, "[UnityAds] SDK Initialization Failure");
	}
}
