package com.unity3d.ads.misc;

import android.text.TextUtils;

import com.unity3d.ads.log.DeviceLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class JsonStorage {
	private JSONObject _data;

	public synchronized boolean initData () {
		if (_data == null) {
			_data = new JSONObject();
			return true;
		}

		return false;
	}

	public synchronized void setData (JSONObject data) {
		_data = data;
	}

	public synchronized JSONObject getData () {
		return _data;
	}

	public synchronized boolean hasData () {
		if (_data != null && _data.length() > 0) {
			return true;
		}
		return false;
	}

	public synchronized void clearData () {
		_data = null;
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
