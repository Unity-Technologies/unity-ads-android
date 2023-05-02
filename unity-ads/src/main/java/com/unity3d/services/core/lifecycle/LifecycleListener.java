package com.unity3d.services.core.lifecycle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.unity3d.services.core.webview.WebViewEventCategory;
import com.unity3d.services.core.webview.bridge.IEventSender;
import com.unity3d.services.core.webview.bridge.SharedInstances;

import java.util.ArrayList;

public class LifecycleListener implements Application.ActivityLifecycleCallbacks {
	private final ArrayList<String> _events;
	private final IEventSender _eventSender;

	public LifecycleListener (ArrayList<String> events) {
		this(events, SharedInstances.INSTANCE.getWebViewEventSender());
	}

	public LifecycleListener (ArrayList<String> events, IEventSender eventSender) {
		_events = events;
		_eventSender = eventSender;
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle bundle) {
		if (_events.contains("onActivityCreated")) {
			if (_eventSender.canSend()) {
				_eventSender.sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.CREATED, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivityStarted(Activity activity) {
		if (_events.contains("onActivityStarted")) {
			if (_eventSender.canSend()) {
				_eventSender.sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.STARTED, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivityResumed(Activity activity) {
		if (_events.contains("onActivityResumed")) {
			if (_eventSender.canSend()) {
				_eventSender.sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.RESUMED, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivityPaused(Activity activity) {
		if (_events.contains("onActivityPaused")) {
			if (_eventSender.canSend()) {
				_eventSender.sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.PAUSED, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivityStopped(Activity activity) {
		if (_events.contains("onActivityStopped")) {
			if (_eventSender.canSend()) {
				_eventSender.sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.STOPPED, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
		if (_events.contains("onActivitySaveInstanceState")) {
			if (_eventSender.canSend()) {
				_eventSender.sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.SAVE_INSTANCE_STATE, activity.getClass().getName());
			}
		}
	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		if (_events.contains("onActivityDestroyed")) {
			if (_eventSender.canSend()) {
				_eventSender.sendEvent(WebViewEventCategory.LIFECYCLE, LifecycleEvent.DESTROYED, activity.getClass().getName());
			}
		}
	}
}
