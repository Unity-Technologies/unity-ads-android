package com.unity3d.services.ads.operation.show;

import android.os.ConditionVariable;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.request.metrics.SDKMetricEvents;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShowModuleDecoratorTimeout extends ShowModuleDecorator {
	private static String errorMsgTimeout = "[UnityAds] Timeout while trying to show ";

	private ExecutorService _executorService;

	public ShowModuleDecoratorTimeout(IShowModule showModule) {
		super(showModule);
		_executorService = Executors.newSingleThreadExecutor();
	}

	@Override
	public void executeAdOperation(IWebViewBridgeInvoker webViewBridgeInvoker, ShowOperationState state) {
		startShowTimeout(state);
		super.executeAdOperation(webViewBridgeInvoker, state);
	}

	private void startShowTimeout(final ShowOperationState showOperationState) {
		_executorService.submit(new Runnable() {
			@Override
			public void run() {
				if (!showOperationState.timeoutCV.block(showOperationState.configuration.getShowTimeout())) {
					onOperationTimeout(showOperationState, UnityAds.UnityAdsShowError.INTERNAL_ERROR, errorMsgTimeout + showOperationState.placementId);
				}
			}
		});
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
		ConditionVariable timeoutCV = showOperation.getShowOperationState().timeoutCV;
		if (timeoutCV == null) return;
		showOperation.getShowOperationState().timeoutCV.open();
	}

	private void onOperationTimeout(final ShowOperationState state, UnityAds.UnityAdsShowError error, String message) {
		if (state != null) {
			remove(state.id);
			state.onUnityAdsShowFailure(error, message);
		}
		getMetricSender().sendSDKMetricEvent(SDKMetricEvents.native_show_timeout_error);
	}
}
