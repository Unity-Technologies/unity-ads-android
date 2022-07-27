package com.unity3d.services.core.misc;

import com.unity3d.services.core.log.DeviceLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class JsonStorageAggregator implements IJsonStorageReader {
	private final List<IJsonStorageReader> _jsonStorageReaders;

	public JsonStorageAggregator(List<IJsonStorageReader> jsonStorageReaders) {
		_jsonStorageReaders = jsonStorageReaders;
	}

	@Override
	public JSONObject getData() {
		JSONObject mergedData = new JSONObject();
		for(IJsonStorageReader jsonStorageReader : _jsonStorageReaders) {
			try {
				if (jsonStorageReader != null) {
					mergedData = Utilities.mergeJsonObjects(mergedData, jsonStorageReader.getData());
				}
			} catch (JSONException e) {
				DeviceLog.error("Failed to merge storage: " + jsonStorageReader);
			}
		}
		return mergedData;
	}

	@Override
	public Object get(String key) {
		Object foundValue = null;
		for(IJsonStorageReader jsonStorageReader : _jsonStorageReaders) {
			if (jsonStorageReader == null) continue;
			foundValue = jsonStorageReader.get(key);
			if (foundValue != null) break;
		}
		return foundValue;
	}

}
