package com.unity3d.services.ads.operation.load;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.operation.AdOperation;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocation;

public class LoadOperation extends AdOperation implements ILoadOperation {
	private LoadOperationState _loadOperationState;

	public LoadOperation(LoadOperationState loadOperationState, IWebViewBridgeInvocation webViewBridgeInvocation) {
		super(webViewBridgeInvocation, "load");
		_loadOperationState = loadOperationState;
	}

	@Override
	public LoadOperationState getLoadOperationState() {
		return _loadOperationState;
	}

	@Override
	public void onUnityAdsAdLoaded(final String placementId) {
		if (_loadOperationState == null || _loadOperationState.listener == null || placementId == null)
			return;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (_loadOperationState != null) {
					_loadOperationState.onUnityAdsAdLoaded();
				}
			}
		});
	}

	@Override
	public void onUnityAdsFailedToLoad(final String placementId, final UnityAds.UnityAdsLoadError error, final String message) {
		if (_loadOperationState == null || _loadOperationState.listener == null || placementId == null)
			return;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (_loadOperationState != null) {
					_loadOperationState.onUnityAdsFailedToLoad(error, message);
				}
			}
		});
	}

	@Override
	public String getId() {
		return _loadOperationState.id;
	}
}
