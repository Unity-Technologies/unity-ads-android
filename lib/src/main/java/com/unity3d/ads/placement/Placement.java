package com.unity3d.ads.placement;

import com.unity3d.ads.UnityAds;

import java.util.HashMap;

public class Placement {
	private static HashMap<String,UnityAds.PlacementState> _placementReadyMap;
	private static String _defaultPlacement;

	public static void setDefaultPlacement(String placement) {
		_defaultPlacement = placement;
	}

	public static void setPlacementState(String placement, String placementState) {
		if (_placementReadyMap == null) {
			_placementReadyMap = new HashMap<>();
		}

		_placementReadyMap.put(placement, UnityAds.PlacementState.valueOf(placementState));;
	}

	public static boolean isReady(String placement) {
		return getPlacementState(placement) == UnityAds.PlacementState.READY;
	}

	public static boolean isReady() {
		return getPlacementState() == UnityAds.PlacementState.READY;
	}

	public static UnityAds.PlacementState getPlacementState(String placement) {
		return currentState(placement);
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