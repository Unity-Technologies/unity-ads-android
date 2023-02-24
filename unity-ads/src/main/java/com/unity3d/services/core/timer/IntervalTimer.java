package com.unity3d.services.core.timer;

import com.unity3d.services.core.lifecycle.LifecycleCache;

import java.util.concurrent.atomic.AtomicInteger;

public class IntervalTimer extends BaseTimer implements IIntervalTimer {

	private final Integer _totalIntervals;
	private final Integer _intervalDuration;
	private Integer _nextInterval;

	private IIntervalTimerListener _timerListener;

	private final AtomicInteger _currentPosition = new AtomicInteger(0);

	public IntervalTimer(final Integer totalDurationMs, final Integer totalIntervals, IIntervalTimerListener timerListener, LifecycleCache lifecycleCache) {
		super(totalDurationMs, null, lifecycleCache);
		_totalIntervals = totalIntervals;
		_timerListener = timerListener;

		_intervalDuration = _totalIntervals == 0 ? totalDurationMs : totalDurationMs / _totalIntervals;
		_nextInterval = _intervalDuration;
	}

	@Override
	public void onStep() {
		if (_currentPosition.addAndGet(_delayMs) >= _nextInterval) {
			onNextInterval();
		}
		super.onStep();
	}

	public void onNextInterval() {
		if (_timerListener != null) {
			_timerListener.onNextIntervalTriggered();
		}
		_nextInterval += _intervalDuration;
	}

	@Override
	public void kill() {
		super.kill();
		_timerListener = null;
	}
}
