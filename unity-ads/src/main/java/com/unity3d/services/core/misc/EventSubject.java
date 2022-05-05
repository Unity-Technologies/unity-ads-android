package com.unity3d.services.core.misc;

import com.unity3d.services.core.timer.IIntervalTimer;
import com.unity3d.services.core.timer.IIntervalTimerFactory;
import com.unity3d.services.core.timer.IIntervalTimerListener;

import java.util.Queue;
import java.util.concurrent.Executors;

public class EventSubject<T> {

	IIntervalTimer _intervalTimer;
	Queue<T> _eventQueue;
	private IEventListener<T> _eventListener;

	public EventSubject(final String activityName, Queue<T> eventQueue, Integer durationMs, IIntervalTimerFactory timerFactory) {
		_eventQueue = eventQueue;
		_intervalTimer = timerFactory.createTimer(activityName, durationMs, eventQueue.size(), new IIntervalTimerListener() {
			@Override
			public void onNextIntervalTriggered() {
				sendNextEvent();
			}
		});
	}

	public void sendNextEvent() {
		if (_eventListener != null) {
			_eventListener.onNextEvent(_eventQueue.remove());
		}

		if (_eventQueue.size() <= 0) {
			unsubscribe();
		}
	}

	public void subscribe(final IEventListener<T> eventListener) {
		if (_eventQueue == null || _eventQueue.size() <= 0 || _intervalTimer == null || eventListener == null) {
			return;
		}

		_eventListener = eventListener;

		startTimer();
	}

	private void startTimer() {
		if (_intervalTimer != null) {
			_intervalTimer.start(Executors.newSingleThreadScheduledExecutor());
		}
	}

	private void killTimer() {
		if (_intervalTimer != null) {
			_intervalTimer.kill();
			_intervalTimer = null;
		}
	}

	public void unsubscribe() {
		killTimer();
		_eventListener = null;
	}

	public boolean eventQueueIsEmpty() {
		return _eventQueue.isEmpty();
	}
}
