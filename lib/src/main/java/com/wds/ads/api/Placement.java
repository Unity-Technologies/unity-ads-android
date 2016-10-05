package com.wds.ads.api;

import com.wds.ads.webview.bridge.WebViewCallback;
import com.wds.ads.webview.bridge.WebViewExposed;

public class Placement {

	@WebViewExposed
	public static void setDefaultPlacement(String placement, WebViewCallback callback) {
		com.wds.ads.placement.Placement.setDefaultPlacement(placement);

		callback.invoke();
	}

	@WebViewExposed
	public static void setPlacementState(String placement, String placementState, WebViewCallback callback) {
		com.wds.ads.placement.Placement.setPlacementState(placement, placementState);

		callback.invoke();
	}
}