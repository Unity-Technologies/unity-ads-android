package com.unity3d.services.ads.operation.load;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.configuration.ErrorState;
import com.unity3d.services.core.configuration.IInitializationListener;
import com.unity3d.services.core.configuration.IInitializationNotificationCenter;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.metrics.AdOperationMetric;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadModuleDecoratorInitializationBuffer extends LoadModuleDecorator implements IInitializationListener {
	private static final String errorMsgInitializationFailed = "[UnityAds] SDK Initialization Failed";
	private static final String errorMsgInitializationFailure = "[UnityAds] SDK Initialization Failure";

	private ConcurrentHashMap<LoadOperationState, IWebViewBridgeInvoker> _queuedLoadEvents;

	public LoadModuleDecoratorInitializationBuffer(ILoadModule loadModule, IInitializationNotificationCenter initializationNotificationCenter) {
		super(loadModule);
		initializationNotificationCenter.addListener(this);
		_queuedLoadEvents = new ConcurrentHashMap<>();

	}

	@Override
	public void executeAdOperation(IWebViewBridgeInvoker webViewBridgeInvoker, LoadOperationState state) {
		if (state == null) return;

		switch (SdkProperties.getCurrentInitializationState()) {
			case INITIALIZED_SUCCESSFULLY:
				super.executeAdOperation(webViewBridgeInvoker, state);
				break;
			case INITIALIZED_FAILED:
				sendOnUnityAdsFailedToLoad(state, UnityAds.UnityAdsLoadError.INITIALIZE_FAILED, errorMsgInitializationFailed);
				break;
			default:
				_queuedLoadEvents.put(state, webViewBridgeInvoker);
				break;
		}
	}

	@Override
	public synchronized void onSdkInitialized() {
		for (Map.Entry<LoadOperationState, IWebViewBridgeInvoker> queuedLoadEvent : _queuedLoadEvents.entrySet()) {
			super.executeAdOperation(queuedLoadEvent.getValue(), queuedLoadEvent.getKey());
		}
		_queuedLoadEvents.clear();
	}

	@Override
	public synchronized void onSdkInitializationFailed(String message, ErrorState errorState, int code) {
		for (LoadOperationState queuedLoadOperationState : _queuedLoadEvents.keySet()) {
			sendOnUnityAdsFailedToLoad(queuedLoadOperationState, UnityAds.UnityAdsLoadError.INITIALIZE_FAILED, errorMsgInitializationFailure);
		}
		_queuedLoadEvents.clear();
	}

	private void sendOnUnityAdsFailedToLoad(final LoadOperationState state, final UnityAds.UnityAdsLoadError error, final String message) {
		if (state == null || state.listener == null) return;
		getMetricSender().sendMetricWithInitState(AdOperationMetric.newAdLoadFailure(error, state.duration()));
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				state.listener.onUnityAdsFailedToLoad(state.placementId, error, message);
			}
		});
	}
}
