package com.unity3d.ads.test.instrumentation.services.core.misc;

import com.unity3d.services.core.misc.EventSubject;
import com.unity3d.services.core.misc.IEventListener;
import com.unity3d.services.core.timer.IIntervalTimerFactory;
import com.unity3d.services.core.timer.IIntervalTimerListener;
import com.unity3d.services.core.timer.IntervalTimer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;

import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class EventSubjectTest<T> {

	@Mock
	IntervalTimer mockIntervalTimer;

	@Mock
	IIntervalTimerFactory mockTimerFactory;

	@Mock
	IEventListener mockEventListener;

	private String TEST_ACTIVITY_NAME = "com.test.activity";
	private Integer TEST_DURATION_MS = 1000;

	@Before
	public void setup() {
		Mockito.when(mockTimerFactory.createTimer(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.<IIntervalTimerListener>any())).thenReturn(mockIntervalTimer);
	}

	@Test
	public void testSubscribeWithEmptyEventQueue() {
		EventSubject<Integer> eventSubject = new EventSubject(TEST_ACTIVITY_NAME, new ArrayDeque<Integer>(), TEST_DURATION_MS, mockTimerFactory);
		eventSubject.subscribe(mockEventListener);
		Mockito.verify(mockIntervalTimer, times(0)).start(Mockito.<ScheduledExecutorService>any());
	}

	@Test
	public void testSubscribeWithNullIntervalTimer() {
		Mockito.when(mockTimerFactory.createTimer(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(), Mockito.<IIntervalTimerListener>any())).thenReturn(null);
		EventSubject<Integer> eventSubject = new EventSubject(TEST_ACTIVITY_NAME, new ArrayDeque<Integer>(), TEST_DURATION_MS, mockTimerFactory);
		eventSubject.subscribe(mockEventListener);
		Mockito.verify(mockIntervalTimer, times(0)).start(Mockito.<ScheduledExecutorService>any());
	}

	@Test
	public void testSubscribeWithNullEventListener() {
		EventSubject<Integer> eventSubject = new EventSubject(TEST_ACTIVITY_NAME, new ArrayDeque<>(Arrays.asList(0, 1)), TEST_DURATION_MS, mockTimerFactory);
		eventSubject.subscribe(null);
		Mockito.verify(mockIntervalTimer, times(0)).start(Mockito.<ScheduledExecutorService>any());
	}

	@Test
	public void testSubscribeAndEventQueueIsEmpty() {
		final Queue<Integer> testQueue = new ArrayDeque<>(Arrays.asList(0, 1));

		EventSubject<Integer> eventSubject = new EventSubject(TEST_ACTIVITY_NAME, testQueue, TEST_DURATION_MS, mockTimerFactory);
   		eventSubject.subscribe(mockEventListener);

		int originalQueueSize = testQueue.size();
		for (int i = 0; i < originalQueueSize; i++) {
			eventSubject.sendNextEvent();
			Mockito.verify(mockEventListener, times(i+1)).onNextEvent(Mockito.any());
			Mockito.verify(mockIntervalTimer, times(i == originalQueueSize - 1 ? 1 : 0)).kill();
			Assert.assertEquals(testQueue.size(), originalQueueSize - i - 1);
			Assert.assertEquals(eventSubject.eventQueueIsEmpty(), i == originalQueueSize - 1 ? true : false);
		}
	}

	@Test
	public void testUnsubscribe() {
		EventSubject<Integer> eventSubject = new EventSubject(TEST_ACTIVITY_NAME, new ArrayDeque<Integer>(), TEST_DURATION_MS, mockTimerFactory);
		eventSubject.unsubscribe();
		Mockito.verify(mockIntervalTimer, times(1)).kill();
	}
}
