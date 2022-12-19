package com.unity3d.scar.adapter.common.signals;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SignalsStorage<T> {

	private final Map<String, T> _queryInfoMap = new ConcurrentHashMap<>();

	public T getQueryInfo(String placementId) {
		return _queryInfoMap.get(placementId);
	}

	public void put(String key, T value) {
		_queryInfoMap.put(key, value);
	}
}
