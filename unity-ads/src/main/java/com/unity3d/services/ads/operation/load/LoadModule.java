package com.unity3d.services.ads.operation.load;

import android.text.TextUtils;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.operation.AdModule;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.request.metrics.ISDKMetricSender;
import com.unity3d.services.core.request.metrics.SDKMetricEvents;
import com.unity3d.services.core.request.metrics.SDKMetricSender;
import com.unity3d.services.core.webview.bridge.CallbackStatus;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocationCallback;
import com.unity3d.services.core.webview.bridge.invocation.WebViewBridgeInvocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoadModule extends AdModule<ILoadOperation, LoadOperationState> implements ILoadModule {
	private static ILoadModule _instance;

	private static String errorMsgInternalCommunicationFailure = "[UnityAds] Internal communication failure";
	private static String errorMsgInternalCommunicationTimeout = "[UnityAds] Internal communication timeout";
	private static String errorMsgPlacementIdNull = "[UnityAds] Placement ID cannot be null";
	private static String errorMsgFailedToCreateLoadRequest = "[UnityAds] Failed to create load request";

	public static ILoadModule getInstance() {
		if (_instance == null) {
			LoadModule loadModule = new LoadModule(new SDKMetricSender());
			LoadModuleDecoratorInitializationBuffer bufferedLoadModule = new LoadModuleDecoratorInitializationBuffer(loadModule, InitializationNotificationCenter.getInstance());
			LoadModuleDecoratorTimeout timedLoadModule = new LoadModuleDecoratorTimeout(bufferedLoadModule);
			_instance = timedLoadModule;
		}
		return _instance;
	}

	public LoadModule(ISDKMetricSender sdkMetricSender) {
		super(sdkMetricSender);
	}

	@Override
	public void executeAdOperation(final IWebViewBridgeInvoker webViewBridgeInvoker, final LoadOperationState state) {
		if (TextUtils.isEmpty(state.placementId)) {
			sendOnUnityAdsFailedToLoad(state, UnityAds.UnityAdsLoadError.INVALID_ARGUMENT, errorMsgPlacementIdNull);
			return;
		}

		LoadOperation loadOperation = new LoadOperation(state, new WebViewBridgeInvocation(_executorService, webViewBridgeInvoker, new IWebViewBridgeInvocationCallback() {
			@Override
			public void onSuccess() {

			}

			@Override
			public void onFailure(String message, CallbackStatus callbackStatus) {
				sendOnUnityAdsFailedToLoad(state, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, errorMsgInternalCommunicationFailure);

				final String cbs = callbackStatus == null ? "invocationFailure" : callbackStatus.toString();
				_sdkMetricSender.sendSDKMetricEventWithTag(SDKMetricEvents.native_load_callback_error, new HashMap<String, String>() {{
					put("cbs", cbs);
				}});

				remove(state.id);
			}

			@Override
			public void onTimeout() {
				sendOnUnityAdsFailedToLoad(state, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, errorMsgInternalCommunicationTimeout);
				getMetricSender().sendSDKMetricEvent(SDKMetricEvents.native_load_callback_timeout);
				remove(state.id);
			}
		}));

		JSONObject parameters = new JSONObject();
		JSONObject options = new JSONObject();
		try {
			options.put("headerBiddingOptions", state.loadOptions.getData());
			parameters.put("options", options);
			parameters.put("listenerId", loadOperation.getId());
			parameters.put("placementId", state.placementId);
			parameters.put("time", Device.getElapsedRealtime());
		} catch (JSONException e) {
			sendOnUnityAdsFailedToLoad(state, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, errorMsgFailedToCreateLoadRequest);
			return;
		} catch (NullPointerException e) {
			sendOnUnityAdsFailedToLoad(state, UnityAds.UnityAdsLoadError.INTERNAL_ERROR, errorMsgFailedToCreateLoadRequest);
			return;
		}

		set(loadOperation);
		loadOperation.invoke(state.configuration.getWebViewBridgeTimeout(), parameters);
	}

	public void onUnityAdsAdLoaded(String operationId) {
		final ILoadOperation loadOperation = get(operationId);
		if (loadOperation == null || loadOperation.getLoadOperationState() == null) return;
		loadOperation.onUnityAdsAdLoaded(loadOperation.getLoadOperationState().placementId);
		remove(operationId);
	}

	@Override
	public void onUnityAdsFailedToLoad(String operationId, UnityAds.UnityAdsLoadError error, String message) {
		final ILoadOperation loadOperation = get(operationId);
		if (loadOperation == null || loadOperation.getLoadOperationState() == null) return;
		loadOperation.onUnityAdsFailedToLoad(loadOperation.getLoadOperationState().placementId, error, message);
		remove(operationId);
	}

	private void sendOnUnityAdsFailedToLoad(final LoadOperationState state, final UnityAds.UnityAdsLoadError error, final String message) {
		if (state == null || state.listener == null) return;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				state.onUnityAdsFailedToLoad(error, message);
			}
		});
	}
}
