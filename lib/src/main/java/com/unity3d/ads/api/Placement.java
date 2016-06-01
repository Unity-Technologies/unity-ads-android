package com.unity3d.ads.api;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

import java.util.HashMap;
import java.util.HashSet;

public class Placement {
	private static HashMap<String,UnityAds.PlacementState> _placementReadyMap;
	private static String _defaultPlacement;
	private static HashSet<String> _placementAnalyticsSent;
	private static boolean _sendAnalytics = false;

	@WebViewExposed
	public static void setDefaultPlacement(String placement, WebViewCallback callback) {
		_defaultPlacement = placement;
		callback.invoke();
	}

	@WebViewExposed
	public static void setPlacementState(String placement, String placementState, WebViewCallback callback) {
		if(_placementReadyMap == null) {
			_placementReadyMap = new HashMap<>();
		}

		if(_placementAnalyticsSent == null) {
			_placementAnalyticsSent = new HashSet<>();
		}

		_placementReadyMap.put(placement, UnityAds.PlacementState.valueOf(placementState));

		if(_placementAnalyticsSent.contains(placement)) {
			_placementAnalyticsSent.remove(placement);
		}

		callback.invoke();
	}

	@WebViewExposed
	public static void setPlacementAnalytics(Boolean sendAnalytics, WebViewCallback callback) {
		_sendAnalytics = sendAnalytics;
		callback.invoke();
	}

	public static boolean isReady(String placement) {
		return getPlacementState(placement) == UnityAds.PlacementState.READY;
	}

	public static boolean isReady() {
		return getPlacementState() == UnityAds.PlacementState.READY;
	}

	public static UnityAds.PlacementState getPlacementState(String placement) {
		UnityAds.PlacementState placementState = currentState(placement);

		if(_sendAnalytics && _placementAnalyticsSent != null && !_placementAnalyticsSent.contains(placement) && WebViewApp.getCurrentApp() != null) {
			_placementAnalyticsSent.add(placement);

			WebViewApp.getCurrentApp().invokeMethod("webview", "placementAnalytics", null, placement, placementState.name());
		}

		return placementState;
	}

	public static UnityAds.PlacementState getPlacementState() {
		if(_defaultPlacement == null) {
			return UnityAds.PlacementState.NOT_AVAILABLE;
		}

		return getPlacementState(_defaultPlacement);
	}

	// When SDK reinitializes all placement info is wiped out and has to be reinitialized by webview
	public static void reset() {
		_placementReadyMap = null;
		_defaultPlacement = null;
		_placementAnalyticsSent = null;
		_sendAnalytics = false;
	}

	public static String getDefaultPlacement() {
		return _defaultPlacement;
	}

	private static UnityAds.PlacementState currentState(String placement) {
		if(_placementReadyMap == null || !_placementReadyMap.containsKey(placement)) {
			return UnityAds.PlacementState.NOT_AVAILABLE;
		}

		return _placementReadyMap.get(placement);
	}
}