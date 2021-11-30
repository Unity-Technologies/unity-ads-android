package com.unity3d.ads.test.instrumentation.services.ads.operation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;

import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.services.ads.operation.load.LoadOperation;
import com.unity3d.services.ads.operation.load.LoadOperationState;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocation;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class LoadOperationTests {
	private static final String placementId = "TestPlacementId";
	private static final UnityAds.UnityAdsLoadError loadError = UnityAds.UnityAdsLoadError.INTERNAL_ERROR;
	private static final String loadErrorMessage = "LoadErrorMessage";
	private static final UnityAdsLoadOptions loadOptions = new UnityAdsLoadOptions();

	private static final int loadTimeout = 150;
	private static final int maxWaitTime = 25000;

	private IUnityAdsLoadListener loadListenerMock;

	@Before
	public void beforeEachTest() {
		loadListenerMock = mock(IUnityAdsLoadListener.class);
	}

	@Test
	public void onUnityAdsAdLoadedCallsListenerOnUnityAdsAdLoadedWhenSet() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, loadOptions, OperationTestUtilities.createConfigurationWithLoadTimeout(loadTimeout));
		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));

		loadOperation.onUnityAdsAdLoaded(placementId);

		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsAdLoaded(placementId);
	}

	@Test
	public void onUnityAdsAdLoadedDoesNotCallListenerWhenListenerNotSet() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, null, loadOptions, OperationTestUtilities.createConfigurationWithLoadTimeout(loadTimeout));
		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));

		loadOperation.onUnityAdsAdLoaded(placementId);
		//No Verification as there is nothing to mock against.  A null pointer exception is all that is being checked for here.
	}

	@Test
	public void onUnityAdsAdLoadedDoesNotCallListenerWhenLoadEventStateNotSet() {
		LoadOperation loadOperation = new LoadOperation(null, mock(IWebViewBridgeInvocation.class));

		loadOperation.onUnityAdsAdLoaded(null);
		//No Verification as there is nothing to mock against.  A null pointer exception is all that is being checked for here.
	}

	@Test
	public void onUnityAdsAdLoadedDoesNotCallListenerWhenPlacementIdNotSet() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, loadOptions, OperationTestUtilities.createConfigurationWithLoadTimeout(loadTimeout));
		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));

		loadOperation.onUnityAdsAdLoaded(null);

		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(0)).onUnityAdsAdLoaded(anyString());
	}

	@Test
	public void onUnityAdsFailedToLoadCallsListenerOnUnityAdsFailedToLoadWhenSet() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, loadOptions, OperationTestUtilities.createConfigurationWithLoadTimeout(loadTimeout));
		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));

		loadOperation.onUnityAdsFailedToLoad(placementId, loadError, loadErrorMessage);

		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(1)).onUnityAdsFailedToLoad(placementId, loadError, loadErrorMessage);
	}

	@Test
	public void onUnityAdsFailedToLoadDoesNotCallListenerWhenListenerNotSet() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, null, loadOptions, OperationTestUtilities.createConfigurationWithLoadTimeout(loadTimeout));
		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));

		loadOperation.onUnityAdsFailedToLoad(placementId, loadError, loadErrorMessage);
		//No Verification as there is nothing to mock against.  A null pointer exception is all that is being checked for here.
	}

	@Test
	public void onUnityAdsFailedToLoadDoesNotCallListenerWhenLoadEventStateNotSet() {
		LoadOperation loadOperation = new LoadOperation(null, mock(IWebViewBridgeInvocation.class));

		loadOperation.onUnityAdsFailedToLoad(null, loadError, loadErrorMessage);
		//No Verification as there is nothing to mock against.  A null pointer exception is all that is being checked for here.
	}

	@Test
	public void onUnityAdsFailedToLoadDoesNotCallListenerWhenPlacementIdNotSet() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, loadOptions, OperationTestUtilities.createConfigurationWithLoadTimeout(loadTimeout));
		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));

		loadOperation.onUnityAdsFailedToLoad(null, loadError, loadErrorMessage);

		Mockito.verify(loadListenerMock, timeout(maxWaitTime).times(0)).onUnityAdsFailedToLoad(null, loadError, loadErrorMessage);
	}

	@Test
	public void getLoadEventState() {
		LoadOperationState loadOperationState = new LoadOperationState(placementId, loadListenerMock, loadOptions, OperationTestUtilities.createConfigurationWithLoadTimeout(loadTimeout));
		LoadOperation loadOperation = new LoadOperation(loadOperationState, mock(IWebViewBridgeInvocation.class));
		Assert.assertEquals("LoadEventState object should match", loadOperationState, loadOperation.getLoadOperationState());
	}
}
