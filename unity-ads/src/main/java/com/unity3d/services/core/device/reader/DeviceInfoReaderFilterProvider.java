package com.unity3d.services.core.device.reader;

import com.unity3d.services.core.misc.IJsonStorageReader;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeviceInfoReaderFilterProvider {
	private static final String UNIFIED_CONFIG_KEY = "unifiedconfig";
	private static final String FILTER_EXCLUDE_KEY = "exclude";
	private IJsonStorageReader _storage;

	public DeviceInfoReaderFilterProvider(IJsonStorageReader jsonStorageReader) {
		_storage = jsonStorageReader;
	}

	public List<String> getFilterList() {
		List<String> filterList = new ArrayList<>();
		if (_storage != null && _storage.getData() != null) {
			Object unifiedConfigData = _storage.getData().opt(UNIFIED_CONFIG_KEY);
			if (unifiedConfigData != null) {
				if (unifiedConfigData instanceof JSONObject) {
					Object filterExcludeData = ((JSONObject) unifiedConfigData).opt(FILTER_EXCLUDE_KEY);
					if (filterExcludeData instanceof String) {
						filterList = (Arrays.asList(((String) filterExcludeData).split(",")));
						filterList = trimWhiteSpaces(filterList);
					}
				}
			}
		}
		return filterList;
	}

	private List<String> trimWhiteSpaces(List<String> original) {
		List<String> trimmedStrings = new ArrayList<>();
		for(String originalString : original) {
			trimmedStrings.add(originalString.trim());
		}
		return trimmedStrings;
	}
}
