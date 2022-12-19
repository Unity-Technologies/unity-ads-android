package com.unity3d.services.ads.operation.load;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.request.metrics.AdOperationError;
import com.unity3d.services.core.request.metrics.AdOperationMetric;
import com.unity3d.services.core.timer.BaseTimer;
import com.unity3d.services.core.timer.ITimerListener;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import java.util.concurrent.Executors;

public class LoadModuleDecoratorTimeout extends LoadModuleDecorator {
	private static final String errorMsgTimeoutLoading = "[UnityAds] Timeout while loading ";

	private final boolean _useNewTimer;

	public LoadModuleDecoratorTimeout(ILoadModule loadModule, ConfigurationReader configurationReader) {
		super(loadModule);
		_useNewTimer = configurationReader.getCurrentConfiguration().getExperiments().isNewLifecycleTimer();
	}

	@Override
	public void executeAdOperation(IWebViewBridgeInvoker webViewBridgeInvoker, LoadOperationState state) {
		getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdLoadStart());
		state.start();
		startLoadTimeout(state);
		super.executeAdOperation(webViewBridgeInvoker, state);
	}

	private void startLoadTimeout(final LoadOperationState loadOperationState) {
		if (loadOperationState == null) return;
		loadOperationState.timeoutTimer = new BaseTimer(loadOperationState.configuration.getLoadTimeout(), _useNewTimer, new ITimerListener() {
			@Override
			public void onTimerFinished() {
				onOperationTimeout(loadOperationState);
			}
		});
		loadOperationState.timeoutTimer.start(Executors.newSingleThreadScheduledExecutor());
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
		BaseTimer timeoutTimer = loadOperationState.timeoutTimer;
		if (timeoutTimer == null) return;
		timeoutTimer.kill();
	}

	private void onOperationTimeout(final LoadOperationState state) {
		if (state != null) {
			getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdLoadFailure(AdOperationError.timeout, state.duration()));
			remove(state.id);
			state.onUnityAdsFailedToLoad(UnityAds.UnityAdsLoadError.TIMEOUT, errorMsgTimeoutLoading + state.placementId);
		}
	}
}
