package com.unity3d.services.core.lifecycle;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Set;

@TargetApi(14)
public class LifecycleCache implements Application.ActivityLifecycleCallbacks {

	private LifecycleEvent _currentState = LifecycleEvent.RESUMED;
	private boolean _appActive = true;

	private Set<IAppActiveListener> _appActiveListeners = new HashSet<>();

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
		notifyListeners();
	}

	@Override
	public void onActivityPaused(Activity activity) {
		_currentState = LifecycleEvent.PAUSED;
		_appActive = false;
		notifyListeners();
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

	public synchronized void notifyListeners() {
		LifecycleEvent event = _appActive ? LifecycleEvent.RESUMED : LifecycleEvent.PAUSED;
		for(IAppActiveListener listener: _appActiveListeners) {
			listener.onAppStateChanged(event);
		}
	}

	public synchronized void addListener(IAppActiveListener listener) {
		_appActiveListeners.add(listener);
	}

	public synchronized void removeListener(IAppActiveListener listener) {
		_appActiveListeners.remove(listener);
	}
}
