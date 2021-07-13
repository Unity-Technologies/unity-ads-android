package com.unity3d.scar.adapter.common;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DispatchGroupTest {
	private Runnable _mockedRunnable;

	@Before
	public void initMocks() {
		_mockedRunnable = Mockito.mock(Runnable.class);
	}

	@Test
	public void testDispatchGroupCompletedNotifyAfterLeave() {
		DispatchGroup dispatchGroup = new DispatchGroup();
		dispatchGroup.enter();
		dispatchGroup.leave();
		dispatchGroup.notify(_mockedRunnable);
		Mockito.verify(_mockedRunnable, Mockito.times(1)).run();
	}

	@Test
	public void testDispatchGroupCompletedLeaveAfterNotify() {
		DispatchGroup dispatchGroup = new DispatchGroup();
		dispatchGroup.enter();
		dispatchGroup.notify(_mockedRunnable);
		dispatchGroup.leave();
		Mockito.verify(_mockedRunnable, Mockito.times(1)).run();
	}

	@Test
	public void testDispatchGroupMultipleCycle() {
		DispatchGroup dispatchGroup = new DispatchGroup();
		dispatchGroup.enter();
		dispatchGroup.enter();
		dispatchGroup.notify(_mockedRunnable);
		dispatchGroup.leave();
		dispatchGroup.leave();
		Mockito.verify(_mockedRunnable, Mockito.times(1)).run();
	}

	@Test
	public void testDispatchGroupMultipleCycleMissingOneLeave() {
		DispatchGroup dispatchGroup = new DispatchGroup();
		dispatchGroup.enter();
		dispatchGroup.enter();
		dispatchGroup.notify(_mockedRunnable);
		dispatchGroup.leave();
		Mockito.verify(_mockedRunnable, Mockito.times(0)).run();
	}

	@Test
	public void testDispatchGroupNot() {
		DispatchGroup dispatchGroup = new DispatchGroup();
		dispatchGroup.enter();
		dispatchGroup.notify(_mockedRunnable);
		Mockito.verify(_mockedRunnable, Mockito.times(0)).run();
	}
}
