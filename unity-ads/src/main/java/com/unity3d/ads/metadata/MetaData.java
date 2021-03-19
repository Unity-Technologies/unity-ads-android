package com.unity3d.ads.metadata;

import android.content.Context;

import com.unity3d.services.core.device.Storage;
import com.unity3d.services.core.device.StorageEvent;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.JsonStorage;
import com.unity3d.services.core.misc.Utilities;

import org.json.JSONObject;

import java.util.Iterator;

public class MetaData extends JsonStorage {
	protected Context _context;
	private String _category;

	public MetaData (Context context) {
		_context = context.getApplicationContext();
	}

	public void setCategory (String category) {
		_category = category;
	}

	public String getCategory () {
		return _category;
	}

	/**
	 * Used by C# layer for reflective metadata set calls
	 */
	private synchronized boolean set (String key, boolean value) {
		return set(key, (Object)value);
	}

	/**
	 * Used by C# layer for reflective metadata set calls
	 */
	private synchronized boolean set (String key, int value) {
		return set(key, (Object)value);
	}

	/**
	 * Used by C# layer for reflective metadata set calls
	 */
	private synchronized boolean set (String key, long value) {
		return set(key, (Object)value);
	}

	public synchronized boolean set (String key, Object value) {
		initData();

		boolean success = false;
		if (super.set(getActualKey(key) + ".value", value) && super.set(getActualKey(key) + ".ts", System.currentTimeMillis())) {
			success = true;
		}

		return success;
	}

	protected synchronized boolean setRaw (String key, Object value) {
		initData();

		boolean success = false;
		if (super.set(getActualKey(key), value)) {
			success = true;
		}

		return success;
	}

	public void commit () {
		if (StorageManager.init(_context)) {
			Storage storage = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

			if (getData() != null && storage != null) {
				JSONObject data = getData();

				Iterator<String> keys = data.keys();
				while (keys.hasNext()) {
					String key = keys.next();
					Object value = get(key);
					if (storage.get(key) != null && storage.get(key) instanceof JSONObject && get(key) instanceof JSONObject) {
						try {
							value = Utilities.mergeJsonObjects((JSONObject)value, (JSONObject)storage.get(key));
						}
						catch (Exception e) {
							DeviceLog.exception("Exception merging JSONs", e);
						}
					}
					storage.set(key, value);
				}

				storage.writeStorage();
				storage.sendEvent(StorageEvent.SET, getData());
			}
		}
		else {
			DeviceLog.error("Unity Ads could not commit metadata due to storage error");
		}
	}

	private String getActualKey (String key) {
		String finalKey = key;
		if (getCategory() != null) {
			finalKey = getCategory() + "." + key;
		}

		return finalKey;
	}
}
