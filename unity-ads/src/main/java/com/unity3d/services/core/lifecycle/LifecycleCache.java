package com.unity3d.services.core.lifecycle;

import static androidx.lifecycle.Lifecycle.*;

import static com.unity3d.scar.adapter.common.Utils.runOnUiThread;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.unity3d.services.core.configuration.ConfigurationReader;

import java.util.HashSet;
import java.util.Set;

@TargetApi(14)
public class LifecycleCache implements Application.ActivityLifecycleCallbacks, LifecycleEventObserver {

	private LifecycleEvent _currentState = LifecycleEvent.RESUMED;
	private boolean _appActive = true;
	private boolean _lifecycleAppActive = true;
	private int _numStarted = 0;
	private boolean _newLifecycle = false;

	private final Set<IAppActiveListener> _appActiveListeners = new HashSet<>();
	private final Set<IAppEventListener> _appStateListeners = new HashSet<>();

	LifecycleCache(ConfigurationReader configurationReader) {
		_newLifecycle = configurationReader.getCurrentConfiguration().getExperiments().isJetpackLifecycle();
		startProcessLifecycleObserving();
	}

	private void startProcessLifecycleObserving(){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				registerLifecycleObserver();
			}
		});
	}

	private void registerLifecycleObserver() {
		ProcessLifecycleOwner
			.get()
			.getLifecycle()
			.addObserver(this);
	}

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
			if (!_newLifecycle) {
				notifyActiveListeners();
			}

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
			if (!_newLifecycle) {
				notifyActiveListeners();
			}
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
		return _newLifecycle ? _lifecycleAppActive : _appActive;
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

	@Override
	public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Event event) {
		switch (event) {
			case ON_STOP:
				_lifecycleAppActive = false;
				if (_newLifecycle) {
					notifyActiveListeners();
				}
				break;
			case ON_START:
				_lifecycleAppActive = true;
				if (_newLifecycle) {
					notifyActiveListeners();
				}
		}
	}
}
