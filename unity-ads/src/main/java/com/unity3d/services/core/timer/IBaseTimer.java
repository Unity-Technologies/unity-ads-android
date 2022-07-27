package com.unity3d.services.core.timer;

import java.util.concurrent.ScheduledExecutorService;

public interface IBaseTimer {
	void start(ScheduledExecutorService timerService);
	void stop();
	boolean pause();
	boolean resume();
	void kill();
}
