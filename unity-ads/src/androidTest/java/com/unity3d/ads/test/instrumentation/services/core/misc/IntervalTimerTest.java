package com.unity3d.ads.test.instrumentation.services.core.misc;

import com.unity3d.services.core.lifecycle.LifecycleCache;
import com.unity3d.services.core.lifecycle.LifecycleEvent;
import com.unity3d.services.core.timer.IIntervalTimerListener;
import com.unity3d.services.core.timer.IntervalTimer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class IntervalTimerTest {

	@Mock
	IIntervalTimerListener mockTimerListener;

	@Mock
	LifecycleCache mockLifecycleCache;

	@Mock
	ScheduledExecutorService mockTimer;

	private String TEST_ACTIVITY_NAME = "com.test.activity";
	private Integer TEST_DURATION_MS = 4000;
	private Integer TEST_INTERVAL_SIZE = 2;
	private long TEST_DELAY_MS = 1000;

	@Test
	public void testAddAppActiveListener() {
		IntervalTimer intervalTimer = getIntervalTimer();
		Mockito.verify(mockLifecycleCache, times(1)).addListener(TEST_ACTIVITY_NAME, intervalTimer);
	}

	@Test
	public void testStart() {
		IntervalTimer intervalTimer = getIntervalTimer();
		intervalTimer.start(mockTimer);

		Mockito.verify(mockTimer, times(1)).scheduleAtFixedRate(Mockito.<Runnable>any(), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void testTimerAlreadyRunning() {
		IntervalTimer intervalTimer = getIntervalTimer();
		intervalTimer.start(mockTimer);
		intervalTimer.start(mockTimer);

		Mockito.verify(mockTimer, times(1)).scheduleAtFixedRate(Mockito.<Runnable>any(), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void testOnNextMs() {
		IntervalTimer intervalTimer = getIntervalTimer();
		intervalTimer.start(mockTimer);

		int nextTimes = 0;
		for (int i = 0; i <= 3; i++) {
			intervalTimer.onNextMs();
			Mockito.verify(mockTimerListener, times(i == 1 || i == 3 ? ++nextTimes : nextTimes)).onNextIntervalTriggered();
			Mockito.verify(mockTimer, times(i == 3 ? 1 : 0)).shutdown();
		}
	}

	@Test
	public void testKill() {
		IntervalTimer intervalTimer = getIntervalTimer();
		intervalTimer.start(mockTimer);
		intervalTimer.kill();
		Mockito.verify(mockTimer, times(1)).shutdown();
		Mockito.verify(mockLifecycleCache, times(1)).removeListener(TEST_ACTIVITY_NAME);
	}

	@Test
	public void testStartKillStart() {
		IntervalTimer intervalTimer = getIntervalTimer();
		intervalTimer.start(mockTimer);
		intervalTimer.kill();
		intervalTimer.start(mockTimer);

		Mockito.verify(mockTimer, times(2)).scheduleAtFixedRate(Mockito.<Runnable>any(), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void testOnAppStateChangedToPaused() {
		IntervalTimer intervalTimer = getIntervalTimer();
		intervalTimer.start(mockTimer);
		intervalTimer.onAppStateChanged(LifecycleEvent.PAUSED);
		Mockito.verify(mockTimer, times(1)).shutdown();
	}

	@Test
	public void testOnAppStateChangedToResumed() {
		IntervalTimer intervalTimer = getIntervalTimer();
		intervalTimer.start(mockTimer);
		intervalTimer.onAppStateChanged(LifecycleEvent.RESUMED);
		Mockito.verify(mockTimer, times(1)).scheduleAtFixedRate(Mockito.<Runnable>any(), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TimeUnit.MILLISECONDS));
	}

	private IntervalTimer getIntervalTimer() {
		return new IntervalTimer(TEST_ACTIVITY_NAME, TEST_DURATION_MS, TEST_INTERVAL_SIZE, mockTimerListener, mockLifecycleCache);
	}
}

