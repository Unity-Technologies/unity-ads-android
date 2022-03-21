package com.unity3d.services.core.misc;

import org.json.JSONObject;

public interface IJsonStorageReader {
	JSONObject getData();
	Object get(String key);
}
