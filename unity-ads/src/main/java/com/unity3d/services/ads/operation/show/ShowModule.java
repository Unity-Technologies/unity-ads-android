package com.unity3d.services.ads.operation.show;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.operation.AdModule;
import com.unity3d.services.core.configuration.ExperimentsReader;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.device.reader.HdrInfoReader;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.request.metrics.AdOperationError;
import com.unity3d.services.core.request.metrics.AdOperationMetric;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;
import com.unity3d.services.core.webview.bridge.CallbackStatus;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocationCallback;
import com.unity3d.services.core.webview.bridge.invocation.WebViewBridgeInvocation;

import org.json.JSONException;
import org.json.JSONObject;


public class ShowModule extends AdModule<IShowOperation, ShowOperationState> implements IShowModule {
	private static IShowModule instance;

	public static String errorMsgPlacementIdNull = "[UnityAds] Placement ID cannot be null";

	public static IShowModule getInstance() {
		if (instance == null) {
			instance = new ShowModuleDecoratorTimeout(new ShowModule(Utilities.getService(SDKMetricsSender.class)), new ExperimentsReader());
		}
		return instance;
	}

	public ShowModule(SDKMetricsSender sdkMetrics) {
		super(sdkMetrics);
	}

	@Override
	public void executeAdOperation(IWebViewBridgeInvoker webViewBridgeInvoker, final ShowOperationState state) {
		if (TextUtils.isEmpty(state.placementId)) {
			sendOnUnityAdsFailedToShow(state, errorMsgPlacementIdNull, UnityAds.UnityAdsShowError.INVALID_ARGUMENT, true);
			return;
		}

		IShowOperation showOperation = new ShowOperation(state, new WebViewBridgeInvocation(_executorService, webViewBridgeInvoker, new IWebViewBridgeInvocationCallback() {
			@Override
			public void onSuccess() {
			}

			@Override
			public void onFailure(String message, CallbackStatus callbackStatus) {
				getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdShowFailure(AdOperationError.callback_error, state.duration()));
				sendOnUnityAdsFailedToShow(state, message, UnityAds.UnityAdsShowError.INTERNAL_ERROR, false);
				remove(state.id);
			}

			@Override
			public void onTimeout() {
				getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdShowFailure(AdOperationError.callback_timeout, state.duration()));
				sendOnUnityAdsFailedToShow(state, "[UnityAds] Show Invocation Timeout", UnityAds.UnityAdsShowError.INTERNAL_ERROR, false);
				remove(state.id);
			}
		}));
		Activity activity = state.activity.get();

		ClientProperties.setActivity(activity);

		Display defaultDisplay = ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		JSONObject parameters = new JSONObject();
		JSONObject options = new JSONObject();
		JSONObject display = new JSONObject();

		try {
			display.put("requestedOrientation", activity.getRequestedOrientation());
			display.put("rotation", defaultDisplay.getRotation());
			if (Build.VERSION.SDK_INT >= 13) {
				Point displaySize = new Point();
				defaultDisplay.getSize(displaySize);
				display.put("width", displaySize.x);
				display.put("height", displaySize.y);
			} else {
				display.put("width", defaultDisplay.getWidth());
				display.put("height", defaultDisplay.getHeight());
			}
			options.put("display", display);
			options.put("headerBiddingOptions", state.showOptions.getData());
			parameters.put("options", options);
			parameters.put("listenerId", showOperation.getId());
			parameters.put("placementId", state.placementId);
			parameters.put("time", Device.getElapsedRealtime());
		} catch (JSONException e) {
			sendOnUnityAdsFailedToShow(state, "[UnityAds] Error creating show options", UnityAds.UnityAdsShowError.INTERNAL_ERROR, true);
			return;
		} catch (NullPointerException e) {
			sendOnUnityAdsFailedToShow(state, "[UnityAds] Error creating show options", UnityAds.UnityAdsShowError.INTERNAL_ERROR, true);
			return;
		}

		set(showOperation);
		showOperation.invoke(state.configuration.getWebViewBridgeTimeout(), parameters);
		HdrInfoReader.getInstance().captureHDRCapabilityMetrics(activity, new ExperimentsReader());
	}

	public void onUnityAdsShowFailure(String id, UnityAds.UnityAdsShowError error, String message) {
		final IShowOperation showOperation = get(id);
		if (showOperation == null || showOperation.getShowOperationState() == null) return;
		final ShowOperationState state = showOperation.getShowOperationState();
		getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdShowFailure(error, state.duration()));
		showOperation.onUnityAdsShowFailure(state.placementId, error, message);
		remove(id);
	}

	public void onUnityAdsShowConsent(String id) {
		final IShowOperation showOperation = get(id);
		if (showOperation == null || showOperation.getShowOperationState() == null) return;
		// We do nothing for now since we don't report back to the user API
	}

	public void onUnityAdsShowStart(String id) {
		final IShowOperation showOperation = get(id);
		if (showOperation == null || showOperation.getShowOperationState() == null) return;
		showOperation.onUnityAdsShowStart(showOperation.getShowOperationState().placementId);
	}

	public void onUnityAdsShowClick(String id) {
		final IShowOperation showOperation = get(id);
		if (showOperation == null || showOperation.getShowOperationState() == null) return;
		showOperation.onUnityAdsShowClick(showOperation.getShowOperationState().placementId);
	}

	public void onUnityAdsShowComplete(String id, UnityAds.UnityAdsShowCompletionState state) {
		final IShowOperation showOperation = get(id);
		if (showOperation == null || showOperation.getShowOperationState() == null) return;
		final ShowOperationState showState = showOperation.getShowOperationState();
		getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdShowSuccess(showState.duration()));
		showOperation.onUnityAdsShowComplete(showState.placementId, state);
		remove(id);
	}

	private void sendOnUnityAdsFailedToShow(final ShowOperationState state, final String message, final UnityAds.UnityAdsShowError error, final boolean sendMetrics) {
		if (state == null || state.listener == null) return;
		if (sendMetrics) {
			getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdShowFailure(error, state.duration()));
		}
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				state.onUnityAdsShowFailure(error, message);
			}
		});
	}
}
