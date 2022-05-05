package com.unity3d.services.core.timer;

import java.util.concurrent.ScheduledExecutorService;

public interface IIntervalTimer {
	void start(ScheduledExecutorService timerService);
	void onNextInterval();
	void kill();
}
