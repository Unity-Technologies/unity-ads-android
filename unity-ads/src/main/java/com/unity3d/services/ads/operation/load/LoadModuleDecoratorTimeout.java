package com.unity3d.services.ads.operation.load;

import android.os.ConditionVariable;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.request.metrics.SDKMetricEvents;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadModuleDecoratorTimeout extends LoadModuleDecorator {
	private static String errorMsgTimeoutLoading = "[UnityAds] Timeout while loading ";

	private ExecutorService _executorService;

	public LoadModuleDecoratorTimeout(ILoadModule loadModule) {
		super(loadModule);
		_executorService = Executors.newCachedThreadPool();
	}

	@Override
	public void executeAdOperation(IWebViewBridgeInvoker webViewBridgeInvoker, LoadOperationState state) {
		startLoadTimeout(state);
		super.executeAdOperation(webViewBridgeInvoker, state);
	}

	private void startLoadTimeout(final LoadOperationState loadOperationState) {
		_executorService.submit(new Runnable() {
			@Override
			public void run() {
				if (!loadOperationState.timeoutCV.block(loadOperationState.configuration.getLoadTimeout())) {
					onOperationTimeout(loadOperationState);
				}
			}
		});
	}

	@Override
	public void onUnityAdsAdLoaded(String operationId) {
		releaseOperationTimeoutLock(operationId);
		super.onUnityAdsAdLoaded(operationId);
	}

	@Override
	public void onUnityAdsFailedToLoad(String operationId, UnityAds.UnityAdsLoadError error, String message) {
		releaseOperationTimeoutLock(operationId);
		super.onUnityAdsFailedToLoad(operationId, error, message);
	}

	private void releaseOperationTimeoutLock(String operationId) {
		ILoadOperation loadOperation = get(operationId);
		if (loadOperation == null) return;
		LoadOperationState loadOperationState = loadOperation.getLoadOperationState();
		if (loadOperationState == null) return;
		ConditionVariable timeoutCV = loadOperation.getLoadOperationState().timeoutCV;
		if (timeoutCV == null) return;
		loadOperation.getLoadOperationState().timeoutCV.open();
	}

	private void onOperationTimeout(final LoadOperationState state) {
		remove(state.id);
		state.listener.onUnityAdsFailedToLoad(state.placementId, UnityAds.UnityAdsLoadError.TIMEOUT, errorMsgTimeoutLoading + state.placementId);
		getMetricSender().sendSDKMetricEvent(SDKMetricEvents.native_load_timeout_error);
	}
}
