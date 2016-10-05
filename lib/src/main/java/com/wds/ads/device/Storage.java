package com.wds.ads.device;

import android.text.TextUtils;

import com.wds.ads.log.DeviceLog;
import com.wds.ads.misc.Utilities;
import com.wds.ads.webview.WebViewApp;
import com.wds.ads.webview.WebViewEventCategory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Storage {
	private JSONObject _data;

	private String _targetFileName;
	private StorageManager.StorageType _type;

	public Storage (String targetFileName, StorageManager.StorageType type) {
		_targetFileName = targetFileName;
		_type = type;
	}

	public StorageManager.StorageType getType () {
		return _type;
	}

	public synchronized boolean set (String key, Object value) {
		if (_data == null || key == null || key.length() == 0 || value == null) {
			DeviceLog.error("Storage not properly initialized or incorrect parameters:" + _data + ", " + key + ", " + value);
			return false;
		}

		createObjectTree(getParentObjectTreeFor(key));

		if (findObject(getParentObjectTreeFor(key)) instanceof JSONObject) {
			JSONObject parentObject = (JSONObject)findObject(getParentObjectTreeFor(key));
			String[] objects = key.split("\\.");

			if (parentObject != null) {
				try {
					parentObject.put(objects[objects.length - 1], value);
				} catch (JSONException e) {
					DeviceLog.exception("Couldn't set value", e);
					return false;
				}
			}
		}
		else {
			DeviceLog.debug("Cannot set subvalue to an object that is not JSONObject");
			return false;
		}

		return true;
	}

	public synchronized Object get (String key) {
		if (_data == null) {
			DeviceLog.error("Data is NULL, readStorage probably not called");
			return null;
		}

		String[] objects = key.split("\\.");
		if (findObject(getParentObjectTreeFor(key)) instanceof JSONObject) {
			JSONObject parentObject = (JSONObject)findObject(getParentObjectTreeFor(key));

			if (parentObject != null) {
				Object o = null;
				try {
					if(parentObject.has(objects[objects.length - 1])) {
						o = parentObject.get(objects[objects.length - 1]);
					}
				}
				catch (Exception e) {
					DeviceLog.exception("Error getting data", e);
				}

				return o;
			}
		}

		return null;
	}

	public synchronized List<String> getKeys (String key, boolean recursive) {
		if (get(key) instanceof  JSONObject) {
			JSONObject parentObject = (JSONObject)get(key);

			List<String> keys = new ArrayList<>();
			String currentKey;

			if (parentObject != null) {
				Iterator<String> i = parentObject.keys();

				while (i.hasNext()) {
					currentKey = i.next();
					List<String> subkeys = null;

					if (recursive) {
						subkeys = getKeys(key + "." + currentKey, recursive);
					}

					keys.add(currentKey);

					if (subkeys != null) {
						for (String subkey : subkeys) {
							keys.add(currentKey + "." + subkey);
						}
					}
				}
			}

			return keys;
		}

		return null;
	}

	public synchronized boolean delete (String key) {
		if (_data == null) {
			DeviceLog.error("Data is NULL, readStorage probably not called");
			return false;
		}

		String[] objects = key.split("\\.");
		if (findObject(getParentObjectTreeFor(key)) instanceof JSONObject) {
			JSONObject parentObject = (JSONObject)findObject(getParentObjectTreeFor(key));

			if (parentObject != null && parentObject.remove(objects[objects.length - 1]) != null) {
				return true;
			}
		}
		return false;
	}

	public synchronized boolean readStorage () {
		File f = new File(_targetFileName);
		String fileData = Utilities.readFile(f);

		if (fileData != null) {
			try {
				_data = new JSONObject(Utilities.readFile(f));
			}
			catch (Exception e) {
				DeviceLog.exception("Error creating storage JSON", e);
				return false;
			}

			return true;
		}

		return false;
	}

	public synchronized boolean initStorage () {
		readStorage();
		if (_data == null) {
			_data = new JSONObject();
		}

		return true;
	}

	public synchronized boolean writeStorage () {
		File f = new File(_targetFileName);

		if (_data != null) {
			boolean success;
			success = Utilities.writeFile(f, _data.toString());
			return success;
		}

		return false;
	}

	public synchronized boolean clearStorage () {
		_data = null;
		File f = new File(_targetFileName);
		return f.delete();
	}

	public synchronized void clearData () {
		_data = null;
	}

	public synchronized boolean hasData () {
		if (_data != null && _data.length() > 0) {
			return true;
		}
		return false;
	}

	public synchronized boolean storageFileExists () {
		File f = new File(_targetFileName);
		return f.exists();
	}

	public synchronized void sendEvent (StorageEvent eventType, Object... params) {
		boolean success = false;

		if (WebViewApp.getCurrentApp() != null) {
			ArrayList<Object> par = new ArrayList<>();
			par.addAll(Arrays.asList(params));
			par.add(0, _type.name());
			Object[] paramsArray = par.toArray();
			success = WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORAGE, eventType, paramsArray);
		}

		if (!success) {
			DeviceLog.debug("Couldn't send storage event to WebApp");
		}
	}

	private synchronized Object findObject (String key) {
		String[] objects = key.split("\\.");
		JSONObject parentObject = _data;

		if (key.length() == 0) {
			return parentObject;
		}

		for (int idx = 0; idx < objects.length; idx++) {
			if (parentObject.has(objects[idx])) {
				try {
					parentObject = parentObject.getJSONObject(objects[idx]);
				}
				catch (Exception e) {
					DeviceLog.exception("Couldn't read JSONObject: " + objects[idx], e);
					return null;
				}
			}
			else {
				return null;
			}
		}

		return parentObject;
	}

	private synchronized void createObjectTree (String tree) {
		String[] objects = tree.split("\\.");
		JSONObject parentObject = _data;

		if (tree.length() == 0) {
			return;
		}

		for (int idx = 0; idx < objects.length; idx++) {
			if (!parentObject.has(objects[idx])) {
				try {
					parentObject = parentObject.put(objects[idx], new JSONObject());
					parentObject = parentObject.getJSONObject(objects[idx]);
				}
				catch (Exception e) {
					DeviceLog.exception("Couldn't create new JSONObject", e);
				}
			}
			else {
				try {
					parentObject = parentObject.getJSONObject(objects[idx]);
				}
				catch (Exception e) {
					DeviceLog.exception("Couldn't get existing JSONObject", e);
				}
			}
		}
	}

	private synchronized String getParentObjectTreeFor (String tree) {
		String parentObject;
		Object[] objects = tree.split("\\.");
		ArrayList<String> tmpObs = new ArrayList(Arrays.asList(objects));
		tmpObs.remove(tmpObs.size() - 1);
		objects = tmpObs.toArray();
		parentObject = TextUtils.join(".", objects);

		return parentObject;
	}
}
