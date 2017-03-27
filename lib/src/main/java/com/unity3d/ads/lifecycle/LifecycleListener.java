package com.unity3d.ads.lifecycle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;

import org.json.JSONObject;

import java.util.ArrayList;

@TargetApi(14)
public class LifecycleListener implements Application.ActivityLifecycleCallbacks {
	private ArrayList<String> _events;

	public LifecycleListener (ArrayList<String> events) {
		_events = events;
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle bundle) {
		if (_events.contains("onActivityCreated")) {
			if (WebViewApp.getCurrentApp() != null) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.CREATED, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivityStarted(Activity activity) {
		if (_events.contains("onActivityStarted")) {
			if (WebViewApp.getCurrentApp() != null) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.STARTED, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivityResumed(Activity activity) {
		if (_events.contains("onActivityResumed")) {
			if (WebViewApp.getCurrentApp() != null) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.RESUMED, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivityPaused(Activity activity) {
		if (_events.contains("onActivityPaused")) {
			if (WebViewApp.getCurrentApp() != null) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.PAUSED, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivityStopped(Activity activity) {
		if (_events.contains("onActivityStopped")) {
			if (WebViewApp.getCurrentApp() != null) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.STOPPED, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
		if (_events.contains("onActivitySaveInstanceState")) {
			if (WebViewApp.getCurrentApp() != null) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.SAVE_INSTANCE_STATE, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		if (_events.contains("onActivityDestroyed")) {
			if (WebViewApp.getCurrentApp() != null) {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.DESTROYED, activity.getClass().getName());
			}
		}
	}
}
