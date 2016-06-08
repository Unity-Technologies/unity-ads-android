package com.unity3d.ads.api;

import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

public class Placement {

	@WebViewExposed
	public static void setDefaultPlacement(String placement, WebViewCallback callback) {
		com.unity3d.ads.placement.Placement.setDefaultPlacement(placement);

		callback.invoke();
	}

	@WebViewExposed
	public static void setPlacementState(String placement, String placementState, WebViewCallback callback) {
		com.unity3d.ads.placement.Placement.setPlacementState(placement, placementState);

		callback.invoke();
	}
}