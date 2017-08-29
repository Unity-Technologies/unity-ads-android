package com.unity3d.ads.device;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.JsonStorage;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

import org.json.JSONObject;

import java.io.File;

public class Storage extends JsonStorage {
	private String _targetFileName;
	private StorageManager.StorageType _type;

	public Storage (String targetFileName, StorageManager.StorageType type) {
		_targetFileName = targetFileName;
		_type = type;
	}

	public StorageManager.StorageType getType () {
		return _type;
	}

	public synchronized boolean readStorage () {
		File f = new File(_targetFileName);
		String fileData = Utilities.readFile(f);

		if (fileData != null) {
			try {
				setData(new JSONObject(Utilities.readFile(f)));
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
		super.initData();
		return true;
	}

	public synchronized boolean writeStorage () {
		File f = new File(_targetFileName);

		if (getData() != null) {
			boolean success;
			success = Utilities.writeFile(f, getData().toString());
			return success;
		}

		return false;
	}

	public synchronized boolean clearStorage () {
		clearData();
		File f = new File(_targetFileName);
		return f.delete();
	}

	public synchronized boolean storageFileExists () {
		File f = new File(_targetFileName);
		return f.exists();
	}

	public synchronized void sendEvent (StorageEvent eventType, Object value) {
		boolean success = false;

		if (WebViewApp.getCurrentApp() != null) {
			success = WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORAGE, eventType, _type.name(), value);
		}

		if (!success) {
			DeviceLog.debug("Couldn't send storage event to WebApp");
		}
	}
}
