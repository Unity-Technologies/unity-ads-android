package com.unity3d.services.core.misc;

import com.unity3d.services.core.log.DeviceLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class JsonFlattener {
	private final JSONObject _jsonData;

	public JsonFlattener(JSONObject jsonData) {
		_jsonData = jsonData;
	}

	public JSONObject flattenJson(String separator, List<String> topLevelToInclude, List<String> reduceKeys, List<String> skipKeys) {
		JSONObject flattenedJson = new JSONObject();
		try {
			for (Iterator<String> keyItor = _jsonData.keys(); keyItor.hasNext(); ) {
				String key = keyItor.next();
				if (!shouldIncludeKey(key, topLevelToInclude, skipKeys)) {
					continue;
				}
				Object value = _jsonData.opt(key);
				if (value instanceof JSONObject) {
					// Flatten from this JSONObject
					JsonFlattener jsonFlattener = new JsonFlattener((JSONObject) value);
					jsonFlattener.flattenJson(separator, key, flattenedJson, reduceKeys, skipKeys);
				} else {
					flattenedJson.put(key, value);
				}
			}
		} catch (JSONException e) {
			DeviceLog.error("Could not flatten JSON: %s", e.getMessage());
		}
		return flattenedJson;
	}


	public boolean shouldIncludeKey(String keyToInclude, List<String> includeList, List<String> skipKeys) {
		if (skipKeys.contains(keyToInclude)) {
			return false;
		}

		if (includeList.size() <= 0) {
			return false;
		}

		return includeList.contains(keyToInclude);
	}

	public void flattenJson(String separator, String parentName, JSONObject outputDictionary, List<String> reduceKeys, List<String> skipKeys) throws JSONException {
		for (Iterator<String> keyItor = _jsonData.keys(); keyItor.hasNext(); ) {
			String key = keyItor.next();
			if (skipKeys.contains(key)) {
				continue;
			}

			Object value = _jsonData.get(key);
			String newKey;
			if (reduceKeys.contains(key)) {
				newKey = parentName;
			} else {
				newKey = String.format("%s%s%s", parentName, separator, key);
			}

			if (value instanceof JSONObject) {
				// Flatten from this JSONObject
				JsonFlattener jsonFlattener = new JsonFlattener((JSONObject) value);
				jsonFlattener.flattenJson(separator, newKey, outputDictionary, reduceKeys, skipKeys);
			} else {
				outputDictionary.put(newKey, value);
			}
		}
	}
}
