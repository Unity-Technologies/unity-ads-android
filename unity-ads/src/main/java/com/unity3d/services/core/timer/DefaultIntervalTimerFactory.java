package com.unity3d.services.core.timer;

import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.lifecycle.CachedLifecycle;

public class DefaultIntervalTimerFactory implements IIntervalTimerFactory {

	@Override
	public IIntervalTimer createTimer(final Integer totalDurationMs, final Integer totalIntervals, final IIntervalTimerListener timerListener) {
		boolean useNewTimer = new ConfigurationReader().getCurrentConfiguration().getExperiments().isNewLifecycleTimer();
		return new IntervalTimer(totalDurationMs, totalIntervals, useNewTimer, timerListener, CachedLifecycle.getLifecycleListener());
	}
}
