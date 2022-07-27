package com.unity3d.services.ads.operation.load;

import android.os.ConditionVariable;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.request.metrics.AdOperationError;
import com.unity3d.services.core.request.metrics.AdOperationMetric;
import com.unity3d.services.core.timer.BaseTimer;
import com.unity3d.services.core.timer.ITimerListener;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadModuleDecoratorTimeout extends LoadModuleDecorator {
	private static final String errorMsgTimeoutLoading = "[UnityAds] Timeout while loading ";

	private final ExecutorService _executorService;
	private final boolean _useNewTimer;

	public LoadModuleDecoratorTimeout(ILoadModule loadModule, ConfigurationReader configurationReader) {
		super(loadModule);
		_executorService = Executors.newCachedThreadPool();
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
		if (_useNewTimer) {
			if (loadOperationState == null) return;
			loadOperationState.timeoutTimer = new BaseTimer(loadOperationState.configuration.getLoadTimeout(), new ITimerListener() {
				@Override
				public void onTimerFinished() {
					onOperationTimeout(loadOperationState);
				}
			});
			loadOperationState.timeoutTimer.start(Executors.newSingleThreadScheduledExecutor());
		} else {
			_executorService.submit(new Runnable() {
				@Override
				public void run() {
					if (!loadOperationState.timeoutCV.block(loadOperationState.configuration.getLoadTimeout())) {
						onOperationTimeout(loadOperationState);
					}
				}
			});
		}
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
		if (_useNewTimer) {
			BaseTimer timeoutTimer = loadOperationState.timeoutTimer;
			if (timeoutTimer == null) return;
			timeoutTimer.kill();
		} else {
			ConditionVariable timeoutCV = loadOperation.getLoadOperationState().timeoutCV;
			if (timeoutCV == null) return;
			loadOperation.getLoadOperationState().timeoutCV.open();
		}
	}

	private void onOperationTimeout(final LoadOperationState state) {
		if (state != null) {
			getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdLoadFailure(AdOperationError.timeout, state.duration()));
			remove(state.id);
			state.onUnityAdsFailedToLoad(UnityAds.UnityAdsLoadError.TIMEOUT, errorMsgTimeoutLoading + state.placementId);
		}
	}
}
