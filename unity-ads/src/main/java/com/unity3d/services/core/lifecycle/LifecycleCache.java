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
	private int _numStarted = 0;

	private final Set<IAppActiveListener> _appActiveListeners = new HashSet<>();
	private final Set<IAppEventListener> _appStateListeners = new HashSet<>();

	@Override
	public void onActivityCreated(Activity activity, Bundle bundle) {
		_currentState = LifecycleEvent.CREATED;
	}

	@Override
	public void onActivityStarted(Activity activity) {
		_currentState = LifecycleEvent.STARTED;
		if (_numStarted == 0) {
			// app went to foreground
			_appActive = true;
			notifyActiveListeners();
		}
		_numStarted++;
	}

	@Override
	public void onActivityResumed(Activity activity) {
		_currentState = LifecycleEvent.RESUMED;
		notifyStateListeners(LifecycleEvent.RESUMED);
	}

	@Override
	public void onActivityPaused(Activity activity) {
		_currentState = LifecycleEvent.PAUSED;
		notifyStateListeners(LifecycleEvent.PAUSED);
	}

	@Override
	public void onActivityStopped(Activity activity) {
		_currentState = LifecycleEvent.STOPPED;
		_numStarted--;
		if (_numStarted <= 0) {
			_numStarted = 0;
			// app went to background
			_appActive = false;
			notifyActiveListeners();
		}
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

	public synchronized void notifyStateListeners(LifecycleEvent event) {
		for(IAppEventListener listener: _appStateListeners) {
			listener.onLifecycleEvent(event);
		}
	}
	public synchronized void notifyActiveListeners() {
		for(IAppActiveListener listener: _appActiveListeners) {
			listener.onAppStateChanged(_appActive);
		}
	}

	public synchronized void addActiveListener(IAppActiveListener listener) {
		_appActiveListeners.add(listener);
	}

	public synchronized void removeActiveListener(IAppActiveListener listener) {
		_appActiveListeners.remove(listener);
	}

	public synchronized void addStateListener(IAppEventListener listener) {
		_appStateListeners.add(listener);
	}

	public synchronized void removeStateListener(IAppEventListener listener) {
		_appStateListeners.remove(listener);
	}
}
