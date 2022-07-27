package com.unity3d.services.core.timer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;

import com.unity3d.services.core.lifecycle.IAppActiveListener;
import com.unity3d.services.core.lifecycle.LifecycleCache;
import com.unity3d.services.core.lifecycle.LifecycleEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(MockitoJUnitRunner.class)
public class BaseTimerTest {

	@Mock
	ITimerListener mockTimerListener;

	@Mock
	LifecycleCache mockLifecycleCache;

	@Mock
	ScheduledExecutorService mockExecutor;

	private final Integer TEST_DURATION_MS = 4000;
	private final long TEST_DELAY_MS = 1000;

	BaseTimer timer;

	@Before
	public void setup() {
		timer = new BaseTimer(TEST_DURATION_MS, mockTimerListener, mockLifecycleCache);
	}

	@Test
	public void testAddAppActiveListener() {
		Mockito.verify(mockLifecycleCache, times(1)).addListener(timer);
	}

	@Test
	public void testStart() {
		timer.start(mockExecutor);

		assertTrue(timer.isRunning());
		Mockito.verify(mockExecutor, times(1)).scheduleAtFixedRate(Mockito.<Runnable>any(), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void testTimerAlreadyRunning() {
		timer.start(mockExecutor);
		timer.start(mockExecutor);

		assertTrue(timer.isRunning());
		Mockito.verify(mockExecutor, times(1)).scheduleAtFixedRate(Mockito.<Runnable>any(), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void testKill() {
		timer.start(mockExecutor);
		timer.kill();

		assertFalse(timer.isRunning());
		Mockito.verify(mockExecutor, times(1)).shutdown();
		Mockito.verify(mockLifecycleCache, times(1)).removeListener(Mockito.<IAppActiveListener>any());
	}

	@Test
	public void testStartKillStart() {
		timer.start(mockExecutor);
		timer.kill();
		timer.start(mockExecutor);

		assertTrue(timer.isRunning());
		Mockito.verify(mockExecutor, times(2)).scheduleAtFixedRate(Mockito.<Runnable>any(), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void testOnAppStateChangePauseResume() throws InterruptedException {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		timer.start(executorService);

		timer.onAppStateChanged(LifecycleEvent.PAUSED);
		assertFalse(timer.isRunning());

		timer.onAppStateChanged(LifecycleEvent.RESUMED);
		assertTrue(timer.isRunning());

		Thread.sleep(5000);
		Mockito.verify(mockTimerListener, times(1)).onTimerFinished();
		assertFalse(timer.isRunning());
		assertTrue(executorService.isShutdown());
	}

	@Test
	public void testTimerStopRestart() throws InterruptedException {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		timer.start(executorService);

		timer.stop();
		assertFalse(timer.isRunning());

		timer.restart(Executors.newSingleThreadScheduledExecutor());
		assertTrue(timer.isRunning());

		Thread.sleep(5000);
		Mockito.verify(mockTimerListener, times(1)).onTimerFinished();
		assertFalse(timer.isRunning());
		assertTrue(executorService.isShutdown());
	}

	@Test
	public void testTimerFinishedNotified() throws InterruptedException {
		ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
		timer.start(executorService);

		Thread.sleep(5000);
		Mockito.verify(mockTimerListener, times(1)).onTimerFinished();
		assertFalse(timer.isRunning());
		assertTrue(executorService.isShutdown());
	}

	@Test
	public void testOnAppStateChangedToResumed() {
		timer.start(mockExecutor);
		timer.onAppStateChanged(LifecycleEvent.RESUMED);

		Mockito.verify(mockExecutor, times(1)).scheduleAtFixedRate(Mockito.<Runnable>any(), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TEST_DELAY_MS), Mockito.eq(TimeUnit.MILLISECONDS));
	}

	@Test
	public void testOnAppStateChangedToResumedWontStartInActiveTimer() {
		timer.onAppStateChanged(LifecycleEvent.RESUMED);

		assertFalse(timer.isRunning());
	}

	@Test
	public void testOnAppStateChangedToResumedStartedTimerWillBeRunning() {
		timer.start(mockExecutor);
		timer.onAppStateChanged(LifecycleEvent.RESUMED);

		assertTrue(timer.isRunning());
	}
}