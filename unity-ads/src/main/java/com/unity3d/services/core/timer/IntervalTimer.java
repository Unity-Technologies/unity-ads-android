package com.unity3d.services.core.timer;

import com.unity3d.services.core.lifecycle.IAppActiveListener;
import com.unity3d.services.core.lifecycle.LifecycleCache;
import com.unity3d.services.core.lifecycle.LifecycleEvent;
import com.unity3d.services.core.log.DeviceLog;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IntervalTimer implements IIntervalTimer, IAppActiveListener {

	private final String _timedActivityName;
	private final Integer _totalDurationMs;
	private final Integer _totalIntervals;
	private final Integer _intervalDuration;
	private IIntervalTimerListener _timerListener;
	private LifecycleCache _lifecycleCache;

	private Integer _nextInterval;

	private ScheduledExecutorService _timerService;
	private final AtomicBoolean _isRunning = new AtomicBoolean(false);
	private final AtomicBoolean _hasPaused = new AtomicBoolean(false);
	private final AtomicInteger _currentPosition = new AtomicInteger(0);
	private final Integer _delayMs = 1000;

	public IntervalTimer(final String timedActivityName, final Integer totalDurationMs, final Integer totalIntervals, IIntervalTimerListener timerListener, LifecycleCache lifecycleCache) {
		_timedActivityName = timedActivityName;
		_totalDurationMs = totalDurationMs;
		_totalIntervals = totalIntervals;
		_timerListener = timerListener;
		_lifecycleCache = lifecycleCache;

		_intervalDuration = _totalIntervals == 0 ? _totalDurationMs : _totalDurationMs / _totalIntervals;
		_nextInterval = _intervalDuration;
		_lifecycleCache.addListener(_timedActivityName, this);
	}

	public void start(ScheduledExecutorService timerService) {
		if (_isRunning.compareAndSet(false, true)) {
			_timerService = timerService;
			schedule();
		}
	}

	private void schedule() {
		try {
			_timerService.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					onNextMs();
				}
			}, _delayMs, _delayMs, TimeUnit.MILLISECONDS);
		} catch (IllegalStateException | IllegalArgumentException | NullPointerException | RejectedExecutionException e) {
			DeviceLog.debug("ERROR: IntervalTimer failed to start due to exception " + e.getLocalizedMessage());
		}
	}

	public void onNextMs() {
		if (_currentPosition.addAndGet(_delayMs) >= _nextInterval) {
			onNextInterval();
		}
	}

	public void onNextInterval() {
		if (_timerListener != null) {
			_timerListener.onNextIntervalTriggered();
		}
		killIfCompleted();

		_nextInterval += _intervalDuration;
	}

	public void killIfCompleted() {
		if (_nextInterval.compareTo(_totalDurationMs) >= 0) {
			kill();
		}
	}

	public void stopTimer() {
		if (_timerService != null && !_timerService.isShutdown()) {
			_timerService.shutdown();
		}
		_isRunning.getAndSet(false);
	}

	public void kill() {
		stopTimer();
		_lifecycleCache.removeListener(_timedActivityName);
		_timerListener = null;
	}

	@Override
	public void onAppStateChanged(LifecycleEvent event) {
		switch (event) {
			case PAUSED:
				_hasPaused.getAndSet(true);
				stopTimer();
				break;
			case RESUMED:
				if (_hasPaused.get()) {
					_hasPaused.getAndSet(false);
					start(Executors.newSingleThreadScheduledExecutor());
				}
				break;
			default:
				break;
		}
	}
}
