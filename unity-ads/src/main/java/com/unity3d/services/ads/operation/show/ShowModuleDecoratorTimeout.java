package com.unity3d.services.ads.operation.show;

import android.os.ConditionVariable;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.request.metrics.AdOperationError;
import com.unity3d.services.core.request.metrics.AdOperationMetric;
import com.unity3d.services.core.timer.BaseTimer;
import com.unity3d.services.core.timer.ITimerListener;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShowModuleDecoratorTimeout extends ShowModuleDecorator {
	private static final String errorMsgTimeout = "[UnityAds] Timeout while trying to show ";

	private final ExecutorService _executorService;
	private final boolean _useNewTimer;

	public ShowModuleDecoratorTimeout(IShowModule showModule, ConfigurationReader configurationReader) {
		super(showModule);
		_executorService = Executors.newSingleThreadExecutor();
		_useNewTimer = configurationReader.getCurrentConfiguration().getExperiments().isNewLifecycleTimer();
	}

	@Override
	public void executeAdOperation(IWebViewBridgeInvoker webViewBridgeInvoker, ShowOperationState state) {
		getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdShowStart());
		state.start();
		startShowTimeout(state);
		super.executeAdOperation(webViewBridgeInvoker, state);
	}

	private void startShowTimeout(final ShowOperationState showOperationState) {
		if (_useNewTimer) {
			if (showOperationState == null) return;
			showOperationState.timeoutTimer = new BaseTimer(showOperationState.configuration.getShowTimeout(), new ITimerListener() {
				@Override
				public void onTimerFinished() {
					onOperationTimeout(showOperationState, UnityAds.UnityAdsShowError.TIMEOUT, errorMsgTimeout + showOperationState.placementId);
				}
			});
			showOperationState.timeoutTimer.start(Executors.newSingleThreadScheduledExecutor());
		} else {
			_executorService.execute(new Runnable() {
				@Override
				public void run() {
					if (!showOperationState.timeoutCV.block(showOperationState.configuration.getShowTimeout())) {
						onOperationTimeout(showOperationState, UnityAds.UnityAdsShowError.TIMEOUT, errorMsgTimeout + showOperationState.placementId);
					}
				}
			});
		}
	}

	@Override
	public void onUnityAdsShowConsent(String id) {
		releaseOperationTimeoutLock(id);
		super.onUnityAdsShowConsent(id);
	}

	@Override
	public void onUnityAdsShowFailure(String id, UnityAds.UnityAdsShowError error, String message) {
		releaseOperationTimeoutLock(id);
		super.onUnityAdsShowFailure(id, error, message);
	}

	@Override
	public void onUnityAdsShowStart(String id) {
		releaseOperationTimeoutLock(id);
		super.onUnityAdsShowStart(id);
	}

	private void releaseOperationTimeoutLock(String operationId) {
		IShowOperation showOperation = get(operationId);
		if (showOperation == null) return;
		ShowOperationState showOperationState = showOperation.getShowOperationState();
		if (showOperationState == null) return;
		if (_useNewTimer) {
			BaseTimer timeoutTimer = showOperationState.timeoutTimer;
			if (timeoutTimer == null) return;
			timeoutTimer.kill();
		} else {
			ConditionVariable timeoutCV = showOperation.getShowOperationState().timeoutCV;
			if (timeoutCV == null) return;
			showOperation.getShowOperationState().timeoutCV.open();
		}
	}

	private void onOperationTimeout(final ShowOperationState state, final UnityAds.UnityAdsShowError error,final String message) {
		if (state != null) {
			getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdShowFailure(AdOperationError.timeout, state.duration()));
			remove(state.id);
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					state.onUnityAdsShowFailure(error, message);
				}
			});
		}
	}
}
