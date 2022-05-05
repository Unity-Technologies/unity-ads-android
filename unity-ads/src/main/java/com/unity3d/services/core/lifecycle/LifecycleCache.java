package com.unity3d.services.core.lifecycle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@TargetApi(14)
public class LifecycleCache implements Application.ActivityLifecycleCallbacks {

	private LifecycleEvent _currentState = LifecycleEvent.RESUMED;
	private boolean _appActive = true;

	private Map<String, IAppActiveListener> _appActiveListeners = new ConcurrentHashMap<>();

	@Override
	public void onActivityCreated(Activity activity, Bundle bundle) {
		_currentState = LifecycleEvent.CREATED;
	}

	@Override
	public void onActivityStarted(Activity activity) {
		_currentState = LifecycleEvent.STARTED;
	}

	@Override
	public void onActivityResumed(Activity activity) {
		_currentState = LifecycleEvent.RESUMED;
		_appActive = true;
		if (_appActiveListeners.containsKey(activity.getClass().getName())) {
			notifyListeners(activity.getClass().getName());
		}
	}

	@Override
	public void onActivityPaused(Activity activity) {
		_currentState = LifecycleEvent.PAUSED;
		_appActive = false;
		if (_appActiveListeners.containsKey(activity.getClass().getName())) {
			notifyListeners(activity.getClass().getName());
		}
	}

	@Override
	public void onActivityStopped(Activity activity) {
		_currentState = LifecycleEvent.STOPPED;
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
		_currentState = LifecycleEvent.SAVE_INSTANCE_STATE;
	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		_currentState = LifecycleEvent.DESTROYED;
	}

	public LifecycleEvent getCurrentState() {
		return _currentState;
	}

	public boolean isAppActive() {
		return _appActive;
	}

	public void notifyListeners(String activityName) {
		if (_appActiveListeners.get(activityName) != null) {
			LifecycleEvent event = _appActive ? LifecycleEvent.RESUMED : LifecycleEvent.PAUSED;
			_appActiveListeners.get(activityName).onAppStateChanged(event);
		}
	}

	public void addListener(String activityName, IAppActiveListener activeListener) {
		_appActiveListeners.put(activityName, activeListener);
	}

	public void removeListener(String activityName) {
		_appActiveListeners.remove(activityName);
	}
}
