package com.unity3d.ads.api;

import android.annotation.TargetApi;

import com.unity3d.ads.lifecycle.LifecycleError;
import com.unity3d.ads.lifecycle.LifecycleListener;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

@TargetApi(14)
public class Lifecycle {
	private static LifecycleListener _listener;

	@WebViewExposed
	public static void register (JSONArray events, WebViewCallback callback) {
		if (ClientProperties.getApplication() != null) {
			if (getLifecycleListener() == null) {
				ArrayList<String> eventList = new ArrayList<>();

				for (int i = 0; i < events.length(); i++) {
					try {
						eventList.add((String)events.get(i));
					}
					catch (JSONException e) {
						callback.error(LifecycleError.JSON_ERROR);
						return;
					}
				}

				setLifecycleListener(new LifecycleListener(eventList));
				ClientProperties.getApplication().registerActivityLifecycleCallbacks(getLifecycleListener());
				callback.invoke();
			}
			else {
				callback.error(LifecycleError.LISTENER_NOT_NULL);
			}
		}
		else {
			callback.error(LifecycleError.APPLICATION_NULL);
		}
	}

	@WebViewExposed
	public static void unregister (WebViewCallback callback) {
		if (ClientProperties.getApplication() != null) {
			if (getLifecycleListener() != null) {
				ClientProperties.getApplication().unregisterActivityLifecycleCallbacks(getLifecycleListener());
				setLifecycleListener(null);
			}

			callback.invoke();
		}
		else {
			callback.error(LifecycleError.APPLICATION_NULL);
		}
	}

	public static LifecycleListener getLifecycleListener () {
		return _listener;
	}

	public static void setLifecycleListener (LifecycleListener listener) {
		_listener = listener;
	}
}
