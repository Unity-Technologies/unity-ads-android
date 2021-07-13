package com.unity3d.scar.adapter.common;

/**
 * Utility class to {@link #enter} multiple thread and await for all of them to {@link #leave} to run a specific task.
 */
public class DispatchGroup {

	private int _threadCount = 0;
	private Runnable _runnable;

	public DispatchGroup() {
		_threadCount = 0;
	}

	public synchronized void enter() {
		_threadCount++;
	}

	public synchronized void leave() {
		_threadCount--;
		notifyGroup();
	}

	public void notify(Runnable r) {
		_runnable = r;
		notifyGroup();
	}

	private void notifyGroup() {
		if (_threadCount <=0 && _runnable !=null) {
			_runnable.run();
		}
	}
}
