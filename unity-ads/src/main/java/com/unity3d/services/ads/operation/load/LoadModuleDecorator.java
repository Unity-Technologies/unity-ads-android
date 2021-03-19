package com.unity3d.services.ads.operation.load;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.request.ISDKMetricSender;
import com.unity3d.services.core.webview.bridge.IWebViewBridgeInvoker;

public class LoadModuleDecorator implements ILoadModule {
	private final ILoadModule _loadModule;

	public LoadModuleDecorator(ILoadModule loadModule) {
		_loadModule = loadModule;
	}

	@Override
	public void executeAdOperation(IWebViewBridgeInvoker webViewBridgeInvoker, LoadOperationState state) {
		_loadModule.executeAdOperation(webViewBridgeInvoker, state);
	}

	@Override
	public ISDKMetricSender getMetricSender() {
		return _loadModule.getMetricSender();
	}

	@Override
	public void onUnityAdsAdLoaded(String operationId) {
		_loadModule.onUnityAdsAdLoaded(operationId);
	}

	@Override
	public void onUnityAdsFailedToLoad(String operationId, UnityAds.UnityAdsLoadError error, String message) {
		_loadModule.onUnityAdsFailedToLoad(operationId, error, message);
	}

	@Override
	public ILoadOperation get(String id) {
		return _loadModule.get(id);
	}

	@Override
	public void set(ILoadOperation sharedObject) {
		_loadModule.set(sharedObject);
	}

	@Override
	public void remove(String id) {
		_loadModule.remove(id);
	}
}
