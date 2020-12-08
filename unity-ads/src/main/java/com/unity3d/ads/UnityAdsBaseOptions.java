package com.unity3d.ads;

import com.unity3d.services.core.log.DeviceLog;

import org.json.JSONException;
import org.json.JSONObject;

public class UnityAdsBaseOptions {
	private JSONObject _data;
	private String OBJECT_ID = "objectId";

	public UnityAdsBaseOptions() {
		_data = new JSONObject();
	}

	public void set(String key, String value) {
		if(key != null && value != null) {
			try {
				_data.put(key, value);
			} catch (JSONException e) {
				DeviceLog.exception("Failed to set Unity Ads options", e);
			}
		}
	}

	public void setObjectId(String objectId) {
		set(OBJECT_ID, objectId);
	}

	public JSONObject getData() {
		return _data;
	}
}
