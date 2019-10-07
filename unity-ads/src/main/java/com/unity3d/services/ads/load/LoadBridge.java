package com.unity3d.services.ads.load;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.json.JSONObject;

import java.util.Map;


public class LoadBridge implements ILoadBridge {

	public enum LoadEvent {
		LOAD_PLACEMENTS
	}

	public void loadPlacements(Map<String, Integer> placements) {
		try {
			JSONObject jsonObject = new JSONObject();
			for (Map.Entry<String, Integer> entry : placements.entrySet()) {
				jsonObject.put(entry.getKey(), entry.getValue().intValue());
			}
			if (WebViewApp.getCurrentApp() != null) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.LOAD_API, LoadEvent.LOAD_PLACEMENTS, jsonObject);
			}
		} catch (Exception exception) {
			DeviceLog.error("An exception was thrown while loading placements " + exception.getLocalizedMessage());
		}
	}
}
