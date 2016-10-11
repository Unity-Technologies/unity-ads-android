package com.unity3d.ads.metadata;

import android.content.Context;

import com.unity3d.ads.device.Storage;
import com.unity3d.ads.device.StorageEvent;
import com.unity3d.ads.device.StorageManager;
import com.unity3d.ads.log.DeviceLog;

import java.util.HashMap;
import java.util.Map;

public class MetaData {
	private Context _context;
	private Map<String, Object> _metaData;
	private String _category;

	public MetaData (Context context) {
		_context = context.getApplicationContext();
	}

	public String getCategory () {
		return _category;
	}

	public void setCategory(String category) {
		_category = category;
	}

	public void set (String key, Object value) {
		if (_metaData == null) {
			_metaData = new HashMap<>();
		}

		String finalKey = key;
		if (getCategory() != null) {
			finalKey = getCategory() + "." + key;
		}

		_metaData.put(finalKey + ".value", value);
		_metaData.put(finalKey + ".ts", System.currentTimeMillis());
	}

	public Map<String, Object> getEntries () {
		return _metaData;
	}

	public void commit () {
		if (StorageManager.init(_context)) {
			Storage storage = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

			for (String key : _metaData.keySet()) {
				if (storage != null) {
					storage.set(key, _metaData.get(key));
				}
			}

			if (storage != null) {
				storage.writeStorage();
				storage.sendEvent(StorageEvent.SET, _metaData);
			}
		} else {
			DeviceLog.error("Unity Ads could not commit metadata due to storage error");
		}
	}
}
