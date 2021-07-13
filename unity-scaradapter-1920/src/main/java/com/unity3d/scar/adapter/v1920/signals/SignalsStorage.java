package com.unity3d.scar.adapter.v1920.signals;

import java.util.HashMap;
import java.util.Map;

public class SignalsStorage {
	private Map<String, QueryInfoMetadata> _placementQueryInfoMap = new HashMap<>();

	public Map<String, QueryInfoMetadata> getPlacementQueryInfoMap() {
		return _placementQueryInfoMap;
	}

	public QueryInfoMetadata getQueryInfoMetadata(String placementId) {
		return _placementQueryInfoMap.get(placementId);
	}

	public void put(String key, QueryInfoMetadata value) {
		_placementQueryInfoMap.put(key, value);
	}

}
