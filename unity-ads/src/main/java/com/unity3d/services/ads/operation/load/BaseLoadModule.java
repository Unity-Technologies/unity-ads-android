package com.unity3d.services.ads.operation.load;

import android.text.TextUtils;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.operation.AdModule;

import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.request.metrics.AdOperationError;
import com.unity3d.services.core.request.metrics.AdOperationMetric;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;
import com.unity3d.services.core.webview.bridge.CallbackStatus;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocationCallback;
import com.unity3d.services.core.webview.bridge.invocation.WebViewBridgeInvocation;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseLoadModule extends AdModule<ILoadOperation, LoadOperationState> implements ILoadModule {

	static final String errorMsgInternalCommunicationFailure = "[UnityAds] Internal communication failure";
	static final String errorMsgInternalCommunicationTimeout = "[UnityAds] Internal communication timeout";
	static final String errorMsgPlacementIdNull = "[UnityAds] Placement ID cannot be null";
	static final String errorMsgFailedToCreateLoadRequest = "[UnityAds] Failed to create load request";

	public BaseLoadModule(SDKMetricsSender sdkMetrics) {
		super(sdkMetrics);
	}

	@Override
	public void executeAdOperation(final IWebViewBridgeInvoker webViewBridgeInvoker, final LoadOperationState state) {
		if (TextUtils.isEmpty(state.placementId)) {
			sendOnUnityAdsFailedToLoad(state, UnityAds.UnityAdsLoadError.INVALID_ARGUMENT, errorMsgPlacementIdNull, true);
			return;
		}

		LoadOperation loadOperation = new LoadOperation(state, new WebViewBridgeInvocation(_executorService, webViewBridgeInvoker, new IWebViewBridgeInvocationCallback() {
			@Override
			public void onSuccess() {
			}

			@Override
			public void onFailure(String message, CallbackStatus callbackStatus) {
				getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdLoadFailure(AdOperationError.callback_error, state.duration(), state.isBanner()));
				sendOnUnityAdsFailedToLoad(state, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, errorMsgInternalCommunicationFailure, false);
				remove(state.getId());
			}

			@Override
			public void onTimeout() {
				getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdLoadFailure(AdOperationError.callback_timeout, state.duration(), state.isBanner()));
				sendOnUnityAdsFailedToLoad(state, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, errorMsgInternalCommunicationTimeout, false);
				remove(state.getId());
			}
		}));

		JSONObject parameters;

		try {
			parameters = buildBaseParameters(state, loadOperation);
		} catch (JSONException | NullPointerException e) {
			sendOnUnityAdsFailedToLoad(state, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, errorMsgFailedToCreateLoadRequest, true);
			return;
		}

		set(loadOperation);
		loadOperation.invoke(state.configuration.getWebViewBridgeTimeout(), parameters);
	}

	JSONObject buildBaseParameters(LoadOperationState state, LoadOperation loadOperation) throws JSONException {
		JSONObject parameters = new JSONObject();
		JSONObject options = buildBaseOptions(state);

		options.put("headerBiddingOptions", state.loadOptions.getData());
		parameters.put("options", options);
		parameters.put("listenerId", loadOperation.getId());
		parameters.put("placementId", state.placementId);
		parameters.put("time", Device.getElapsedRealtime());

		addOptionalParameters(state, parameters);

		return parameters;
	}

	JSONObject buildBaseOptions(LoadOperationState state) throws JSONException {
		JSONObject options = new JSONObject();
		options.put("headerBiddingOptions", state.loadOptions.getData());
		return options;
	}

	abstract void addOptionalParameters(LoadOperationState state, JSONObject parameters) throws JSONException;

	@Override
	public void onUnityAdsAdLoaded(String operationId) {
		final ILoadOperation loadOperation = get(operationId);
		if (loadOperation == null || loadOperation.getLoadOperationState() == null) return;
		final LoadOperationState state = loadOperation.getLoadOperationState();
		getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdLoadSuccess(state.duration(), state.isBanner()));
		loadOperation.onUnityAdsAdLoaded(state.placementId);
		remove(operationId);
	}

	@Override
	public void onUnityAdsFailedToLoad(String operationId, UnityAds.UnityAdsLoadError error, String message) {
		final ILoadOperation loadOperation = get(operationId);
		if (loadOperation == null || loadOperation.getLoadOperationState() == null) return;
		final LoadOperationState state = loadOperation.getLoadOperationState();
		getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdLoadFailure(error, state.duration(), state.isBanner()));
		loadOperation.onUnityAdsFailedToLoad(state.placementId, error, message);
		remove(operationId);
	}

	private void sendOnUnityAdsFailedToLoad(final LoadOperationState state, final UnityAds.UnityAdsLoadError error, final String message, final boolean sendMetrics) {
		if (state == null || state.listener == null) return;
		if (sendMetrics) {
			getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdLoadFailure(error, state.duration(), state.isBanner()));
		}
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				state.onUnityAdsFailedToLoad(error, message);
			}
		});
	}
}
