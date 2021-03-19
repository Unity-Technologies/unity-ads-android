package com.unity3d.services.ads.operation.show;

import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.operation.AdModule;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.request.ISDKMetricSender;
import com.unity3d.services.core.request.SDKMetricEvents;
import com.unity3d.services.core.request.SDKMetricSender;
import com.unity3d.services.core.webview.bridge.CallbackStatus;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocationCallback;
import com.unity3d.services.core.webview.bridge.invocation.WebViewBridgeInvocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ShowModule extends AdModule<IShowOperation, ShowOperationState> implements IShowModule {
	private static IShowModule instance;

	public static String errorMsgPlacementIdNull = "[UnityAds] Placement ID cannot be null";

	public static IShowModule getInstance() {
		if (instance == null) {
			instance = new ShowModuleDecoratorTimeout(new ShowModule(new SDKMetricSender()));
		}
		return instance;
	}

	public ShowModule(ISDKMetricSender sdkMetricSender) {
		super(sdkMetricSender);
	}

	@Override
	public void executeAdOperation(IWebViewBridgeInvoker webViewBridgeInvoker, final ShowOperationState state) {
		if (TextUtils.isEmpty(state.placementId)) {
			sendOnUnityAdsFailedToShow(state, errorMsgPlacementIdNull, UnityAds.UnityAdsShowError.INVALID_ARGUMENT);
			return;
		}
		IShowOperation showOperation = new ShowOperation(state, new WebViewBridgeInvocation(webViewBridgeInvoker, new IWebViewBridgeInvocationCallback() {
			@Override
			public void onSuccess() {
			}
			@Override
			public void onFailure(String message, CallbackStatus callbackStatus) {
				sendOnUnityAdsFailedToShow(state, message, UnityAds.UnityAdsShowError.INTERNAL_ERROR);

				final String cbs = callbackStatus == null ? "invocationFailure" : callbackStatus.toString();
				_sdkMetricSender.SendSDKMetricEventWithTag(SDKMetricEvents.native_show_callback_error, new HashMap<String, String>(){{
					put("cbs", cbs);
				}});

				remove(state.id);
			}
			@Override
			public void onTimeout() {
				sendOnUnityAdsFailedToShow(state, "[UnityAds] Show Invocation Timeout", UnityAds.UnityAdsShowError.INTERNAL_ERROR);
				_sdkMetricSender.SendSDKMetricEvent(SDKMetricEvents.native_show_callback_timeout);
				remove(state.id);
			}
		}));

		ClientProperties.setActivity(state.activity);

		Display defaultDisplay = ((WindowManager)state.activity.getSystemService(state.activity.WINDOW_SERVICE)).getDefaultDisplay();
		JSONObject options = new JSONObject();
		JSONObject display = new JSONObject();

		try {
			options.put("requestedOrientation", state.activity.getRequestedOrientation());
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
			options.put("options", state.showOptions.getData());
			options.put("listenerId", showOperation.getId());
			options.put("placementId", state.placementId);
			options.put("time", Device.getElapsedRealtime());
		} catch (JSONException e) {
			sendOnUnityAdsFailedToShow(state, "[UnityAds] Error creating show options", UnityAds.UnityAdsShowError.INTERNAL_ERROR);
			return;
		} catch (NullPointerException e) {
			sendOnUnityAdsFailedToShow(state, "[UnityAds] Error creating show options", UnityAds.UnityAdsShowError.INTERNAL_ERROR);
			return;
		}

		set(showOperation);
		showOperation.invoke(state.configuration.getWebViewBridgeTimeout(), options);
	}

	public void onUnityAdsShowFailure(String id, UnityAds.UnityAdsShowError error, String message) {
		final IShowOperation showOperation = get(id);
		if (showOperation == null || showOperation.getShowOperationState() == null) return;
		showOperation.onUnityAdsShowFailure(showOperation.getShowOperationState().placementId, error, message);
		remove(id);
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
		showOperation.onUnityAdsShowComplete(showOperation.getShowOperationState().placementId, state);
		remove(id);
	}

	private void sendOnUnityAdsFailedToShow(final ShowOperationState showOperationState, final String errorMessage, final UnityAds.UnityAdsShowError errorCode) {
		if (showOperationState == null || showOperationState.listener == null) return;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				showOperationState.listener.onUnityAdsShowFailure(showOperationState.placementId, errorCode, errorMessage);
			}
		});
	}
}
