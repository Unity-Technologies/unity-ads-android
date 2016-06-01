package com.unity3d.ads.api;

import com.unity3d.ads.device.StorageError;
import com.unity3d.ads.device.StorageManager;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;

public class Storage {
	@WebViewExposed
	public static void set (String type, String key, Integer value, WebViewCallback callback) {
		set(type, key, (Object)value, callback);
	}

	@WebViewExposed
	public static void set (String type, String key, Boolean value, WebViewCallback callback) {
		set(type, key, (Object)value, callback);
	}

	@WebViewExposed
	public static void set (String type, String key, Long value, WebViewCallback callback) {
		set(type, key, (Object)value, callback);
	}

	@WebViewExposed
	public static void set (String type, String key, Double value, WebViewCallback callback) {
		set(type, key, (Object)value, callback);
	}

	@WebViewExposed
	public static void set (String type, String key, String value, WebViewCallback callback) {
		set(type, key, (Object)value, callback);
	}

	@WebViewExposed
	public static void set (String type, String key, JSONObject value, WebViewCallback callback) {
		set(type, key, (Object)value, callback);
	}

	@WebViewExposed
	public static void set (String type, String key, JSONArray value, WebViewCallback callback) {
		set(type, key, (Object)value, callback);
	}

	private static void set (String type, String key, Object value, WebViewCallback callback) {
		com.unity3d.ads.device.Storage s = getStorage(type);

		if (s != null) {
			boolean success = s.set(key, value);
			if (success) {
				callback.invoke(key, value);
			} else {
				callback.error(StorageError.COULDNT_SET_VALUE, key, value);
			}
		}
		else {
			callback.error(StorageError.COULDNT_GET_STORAGE, type, key, value);
		}
	}

	@WebViewExposed
	public static void get (String type, String key, WebViewCallback callback) {
		com.unity3d.ads.device.Storage s = getStorage(type);

		if (s != null) {
			Object retObj = s.get(key);

			if (retObj == null) {
				callback.error(StorageError.COULDNT_GET_VALUE, key);
			}
			else {
				callback.invoke(retObj);
			}
		}
		else {
			callback.error(StorageError.COULDNT_GET_STORAGE, type, key);
		}
	}

	@WebViewExposed
	public static void getKeys (String type, String key, Boolean recursive, WebViewCallback callback) {
		com.unity3d.ads.device.Storage s = getStorage(type);

		if (s != null) {
			List<String> keys = s.getKeys(key, recursive);
			if (keys != null) {
				callback.invoke(new JSONArray(keys));
			} else {
				callback.invoke(new JSONArray());
			}
		}
		else {
			callback.error(StorageError.COULDNT_GET_STORAGE, type, key);
		}
	}

	@WebViewExposed
	public static void read (String type, WebViewCallback callback) {
		com.unity3d.ads.device.Storage s = getStorage(type);

		if (s != null) {
			s.readStorage();
			callback.invoke(type);
		}
		else {
			callback.error(StorageError.COULDNT_GET_STORAGE, type);
		}
	}

	@WebViewExposed
	public static void write (String type, WebViewCallback callback) {
		com.unity3d.ads.device.Storage s = getStorage(type);

		if (s != null) {
			boolean success = s.writeStorage();
			if (success) {
				callback.invoke(type);
			}
			else {
				callback.error(StorageError.COULDNT_WRITE_STORAGE_TO_CACHE, type);
			}
		}
		else {
			callback.error(StorageError.COULDNT_GET_STORAGE, type);
		}
	}

	@WebViewExposed
	public static void clear (String type, WebViewCallback callback) {
		com.unity3d.ads.device.Storage s = getStorage(type);

		if (s != null) {
			boolean success = s.clearStorage();
			if (success) {
				callback.invoke(type);
			}
			else {
				callback.error(StorageError.COULDNT_CLEAR_STORAGE, type);
			}
		}
		else {
			callback.error(StorageError.COULDNT_GET_STORAGE, type);
		}
	}

	@WebViewExposed
	public static void delete (String type, String key, WebViewCallback callback) {
		com.unity3d.ads.device.Storage s = getStorage(type);

		if (s != null) {
			boolean success = s.delete(key);
			if (success) {
				callback.invoke(type);
			}
			else {
				callback.error(StorageError.COULDNT_DELETE_VALUE, type);
			}
		}
		else {
			callback.error(StorageError.COULDNT_GET_STORAGE, type);
		}
	}

	private static com.unity3d.ads.device.Storage getStorage (String type) {
		StorageManager.StorageType storageType = StorageManager.StorageType.valueOf(type);

		return StorageManager.getStorage(storageType);
	}
}
