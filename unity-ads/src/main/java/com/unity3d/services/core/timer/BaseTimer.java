package com.unity3d.services.core.timer;

import com.unity3d.services.core.lifecycle.CachedLifecycle;
import com.unity3d.services.core.lifecycle.IAppActiveListener;
import com.unity3d.services.core.lifecycle.LifecycleCache;
import com.unity3d.services.core.lifecycle.LifecycleEvent;
import com.unity3d.services.core.log.DeviceLog;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple class that represents a timer with a {@link ITimerListener} that notifies when finished
 */
public class BaseTimer implements IBaseTimer, IAppActiveListener {

	private final LifecycleCache _lifecycleCache;
	final Integer _totalDurationMs;
	final Integer _delayMs = 1000;
	Integer _remainingDurationMs;
	private ITimerListener _timerListener;
	private ScheduledFuture<?> _task;

	private ScheduledExecutorService _timerService;
	private final AtomicBoolean _isRunning = new AtomicBoolean(false);
	private final AtomicBoolean _hasPaused = new AtomicBoolean(false);

	public BaseTimer(final Integer totalDurationMs, ITimerListener timerListener, LifecycleCache lifecycleCache) {
		_totalDurationMs = totalDurationMs;
		_remainingDurationMs = totalDurationMs;
		_timerListener = timerListener;
		_lifecycleCache = lifecycleCache;
		if (_lifecycleCache != null) {
			_lifecycleCache.addListener(this);
		}
	}

	public BaseTimer(final Integer totalDurationMs, ITimerListener timerListener) {
		this(totalDurationMs, timerListener, CachedLifecycle.getLifecycleListener());
	}

	/**
	 * Starts the timer countdown
	 * @param timerService {@link ScheduledExecutorService} to run timer on, will be shutdown when timer is stopped via {@link #stop() Stop} or {@link #kill() Kill}
	 */
	public void start(ScheduledExecutorService timerService) {
		if (_isRunning.compareAndSet(false, true)) {
			_timerService = timerService;
			schedule();
		}
	}

	/**
	 * Stops the timer countdown
	 * For timer to continue {@link #restart(ScheduledExecutorService) Restart} is required
	 */
	public void stop() {
		if (_timerService != null && !_timerService.isShutdown()) {
			_timerService.shutdown();
			_timerService = null;
		}
		_isRunning.getAndSet(false);
	}

	/**
	 * Pauses the timer countdown
	 * Countdown can be continued via {@link #resume() Resume} or {@link #restart(ScheduledExecutorService) Restart}
	 * @return if countdown could be paused
	 */
	public boolean pause() {
		boolean result = false;

		if (_task != null && !_task.isCancelled()) {
			_task.cancel(true);
			_task = null;
			result = true;
		}
		_isRunning.getAndSet(false);

		return result;
	}

	/**
	 * Resumes the timer countdown after a {@link #pause() Pause}
	 * @return if countdown resumed successfully
	 */
	public boolean resume() {
		boolean result = false;

		if (_timerService != null && !_timerService.isShutdown()) {
			result = true;
			schedule();
		}
		_isRunning.getAndSet(result);

		return result;
	}

	/**
	 * Restarts the timer to initial countdown after a {@link #stop() Stop}
	 */
	public void restart(ScheduledExecutorService timerService) {
		if (_timerService == null || _timerService.isShutdown()) {
			_timerService = timerService;
		}
		_remainingDurationMs = _totalDurationMs;
		schedule();
		_isRunning.getAndSet(true);
	}

	/**
	 * Stops and cleans up
	 */
	public void kill() {
		stop();
		if (_lifecycleCache != null) {
			_lifecycleCache.removeListener(this);
		}
		_timerListener = null;
	}

	/**
	 * @return if countdown is currently running
	 */
	public boolean isRunning() {
		return _isRunning.get();
	}

	public void onStep() {
		if (_remainingDurationMs <= 0) {
			notifyListeners();
			kill();
		}
	}

	private void schedule() {
		try {
			_task = _timerService.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					_remainingDurationMs = _remainingDurationMs - _delayMs;
					onStep();
				}
			}, _delayMs, _delayMs, TimeUnit.MILLISECONDS);
		} catch (IllegalStateException | IllegalArgumentException | NullPointerException | RejectedExecutionException e) {
			DeviceLog.debug("ERROR: IntervalTimer failed to start due to exception " + e.getLocalizedMessage());
		}
	}

	private void notifyListeners() {
		if (_timerListener != null) {
			_timerListener.onTimerFinished();
		}
	}

	@Override
	public void onAppStateChanged(LifecycleEvent event) {
		switch (event) {
			case PAUSED:
				if (isRunning()) {
					pause();
					_hasPaused.getAndSet(true);
				}
				break;
			case RESUMED:
				if (_hasPaused.get()) {
					_hasPaused.getAndSet(false);
					resume();
				}
				break;
			default:
				break;
		}
	}
}