package com.unity3d.services.core.timer;

import com.unity3d.services.core.lifecycle.CachedLifecycle;

public class DefaultIntervalTimerFactory implements IIntervalTimerFactory {

	@Override
	public IIntervalTimer createTimer(final String timedActivityName, final Integer totalDurationMs, final Integer totalIntervals, final IIntervalTimerListener timerListener) {
		return new IntervalTimer(timedActivityName, totalDurationMs, totalIntervals, timerListener, CachedLifecycle.getLifecycleListener());
	}
}
