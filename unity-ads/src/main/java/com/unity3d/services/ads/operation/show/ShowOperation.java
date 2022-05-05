package com.unity3d.services.ads.operation.show;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.operation.AdOperation;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.webview.bridge.invocation.IWebViewBridgeInvocation;

public class ShowOperation extends AdOperation implements IShowOperation  {
	private ShowOperationState showOperationState;

	public ShowOperation(ShowOperationState showOperationState, IWebViewBridgeInvocation webViewBridgeInvocation) {
		super(webViewBridgeInvocation, "show");
		this.showOperationState = showOperationState;
	}

	@Override
	public ShowOperationState getShowOperationState() {
		return showOperationState;
	}


	@Override
	public void onUnityAdsShowFailure(final String placementId, final UnityAds.UnityAdsShowError error, final String message) {
		if (showOperationState == null || showOperationState.listener == null) return;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (showOperationState != null) {
					showOperationState.onUnityAdsShowFailure( error, message);
				}
			}
		});
	}

	@Override
	public void onUnityAdsShowStart(final String placementId) {
		if (showOperationState == null) return;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (showOperationState != null) {
					showOperationState.onUnityAdsShowStart(placementId);
				}
			}
		});
	}

	@Override
	public void onUnityAdsShowClick(final String placementId) {
		if (showOperationState == null) return;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (showOperationState != null) {
					showOperationState.onUnityAdsShowClick();
				}
			}
		});
	}

	@Override
	public void onUnityAdsShowComplete(final String placementId, final UnityAds.UnityAdsShowCompletionState state) {
		if (showOperationState == null) return;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (showOperationState != null) {
					showOperationState.onUnityAdsShowComplete(state);
				}
			}
		});
	}

	@Override
	public String getId() {
		return showOperationState.id;
	}
}
