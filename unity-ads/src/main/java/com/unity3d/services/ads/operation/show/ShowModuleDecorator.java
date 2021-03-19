package com.unity3d.services.ads.operation.show;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.request.ISDKMetricSender;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

public class ShowModuleDecorator implements IShowModule {
	private final IShowModule _showModule;

	public ShowModuleDecorator(IShowModule showModule) {
		_showModule = showModule;
	}

	@Override
	public void executeAdOperation(IWebViewBridgeInvoker webViewBridgeInvoker, ShowOperationState state) {
		_showModule.executeAdOperation(webViewBridgeInvoker, state);
	}

	@Override
	public ISDKMetricSender getMetricSender() {
		return _showModule.getMetricSender();
	}

	@Override
	public void onUnityAdsShowFailure(String id, UnityAds.UnityAdsShowError error, String message) {
		_showModule.onUnityAdsShowFailure(id, error, message);
	}

	@Override
	public void onUnityAdsShowStart(String id) {
		_showModule.onUnityAdsShowStart(id);
	}

	@Override
	public void onUnityAdsShowClick(String id) {
		_showModule.onUnityAdsShowClick(id);
	}

	@Override
	public void onUnityAdsShowComplete(String id, UnityAds.UnityAdsShowCompletionState state) {
		_showModule.onUnityAdsShowComplete(id, state);
	}

	@Override
	public IShowOperation get(String id) {
		return _showModule.get(id);
	}

	@Override
	public void set(IShowOperation sharedObject) {
		_showModule.set(sharedObject);
	}

	@Override
	public void remove(String id) {
		_showModule.remove(id);
	}
}
