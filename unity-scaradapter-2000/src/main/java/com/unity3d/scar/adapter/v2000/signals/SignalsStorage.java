package com.unity3d.scar.adapter.v2000.signals;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SignalsStorage {
	private final Map<String, QueryInfoMetadata> _placementQueryInfoMap = new ConcurrentHashMap<>();

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
