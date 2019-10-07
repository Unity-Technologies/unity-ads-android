package com.unity3d.ads.test.instrumentation.services.core.configuration;

import com.unity3d.services.core.configuration.IInitializationListener;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class InitializationNotificationCenterTest {

	private InitializationNotificationCenter initializationNotificationCenter;

	@Before
	public void before() {
		initializationNotificationCenter = new InitializationNotificationCenter();
	}

	@Test
	public void testAddDelegateAndTriggerOnSdkInitialized() {
		IInitializationListener listener = mock(IInitializationListener.class);
		initializationNotificationCenter.addListener(listener);

		initializationNotificationCenter.triggerOnSdkInitialized();

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener, times(1)).onSdkInitialized();
	}

	@Test
	public void testAddSameDelegateTwiceAndTriggerOnSdkInitialized() {
		IInitializationListener listener = mock(IInitializationListener.class);
		initializationNotificationCenter.addListener(listener);
		initializationNotificationCenter.addListener(listener);

		initializationNotificationCenter.triggerOnSdkInitialized();

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener, times(1)).onSdkInitialized();
	}

	@Test
	public void testAddMutipleAndTriggerOnSdkInitialized() {
		IInitializationListener listener1 = mock(IInitializationListener.class);
		IInitializationListener listener2 = mock(IInitializationListener.class);
		IInitializationListener listener3 = mock(IInitializationListener.class);

		initializationNotificationCenter.addListener(listener1);
		initializationNotificationCenter.addListener(listener2);
		initializationNotificationCenter.addListener(listener3);

		initializationNotificationCenter.triggerOnSdkInitialized();

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener1, times(1)).onSdkInitialized();
		verify(listener2, times(1)).onSdkInitialized();
		verify(listener3, times(1)).onSdkInitialized();

	}

	@Test
	public void testAddAndTriggerOnSdkInitializedMultipleTimes() {
		IInitializationListener listener = mock(IInitializationListener.class);

		initializationNotificationCenter.addListener(listener);

		initializationNotificationCenter.triggerOnSdkInitialized();
		initializationNotificationCenter.triggerOnSdkInitialized();

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener, times(2)).onSdkInitialized();
	}

	@Test
	public void testRemoveAndTriggerOnSdkInitialized() {
		IInitializationListener listener = mock(IInitializationListener.class);

		initializationNotificationCenter.addListener(listener);

		initializationNotificationCenter.triggerOnSdkInitialized();

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		initializationNotificationCenter.removeListener(listener);

		initializationNotificationCenter.triggerOnSdkInitialized();

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener, times(1)).onSdkInitialized();
	}

	@Test
	public void testRemoveNotAdded() {
		IInitializationListener listener = mock(IInitializationListener.class);

		initializationNotificationCenter.removeListener(listener);
		initializationNotificationCenter.triggerOnSdkInitialized();

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener, times(0)).onSdkInitialized();
	}

	@Test
	public void testAddDelegateAndTriggerOnSdkInitializationFailed() {
		IInitializationListener listener = mock(IInitializationListener.class);
		initializationNotificationCenter.addListener(listener);

		initializationNotificationCenter.triggerOnSdkInitializationFailed("test", 0);

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener, times(1)).onSdkInitializationFailed("SDK Failed to Initialize due to test", 0);
	}

	@Test
	public void testAddSameDelegateTwiceAndTriggerOnSdkInitializationFailed() {
		IInitializationListener listener = mock(IInitializationListener.class);
		initializationNotificationCenter.addListener(listener);
		initializationNotificationCenter.addListener(listener);

		initializationNotificationCenter.triggerOnSdkInitializationFailed("test", 0);

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener, times(1)).onSdkInitializationFailed("SDK Failed to Initialize due to test", 0);
	}

	@Test
	public void testAddMutipleAndTriggerOnSdkInitializationFailed() {
		IInitializationListener listener1 = mock(IInitializationListener.class);
		IInitializationListener listener2 = mock(IInitializationListener.class);
		IInitializationListener listener3 = mock(IInitializationListener.class);

		initializationNotificationCenter.addListener(listener1);
		initializationNotificationCenter.addListener(listener2);
		initializationNotificationCenter.addListener(listener3);

		initializationNotificationCenter.triggerOnSdkInitializationFailed("test", 0);

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener1, times(1)).onSdkInitializationFailed("SDK Failed to Initialize due to test", 0);
		verify(listener2, times(1)).onSdkInitializationFailed("SDK Failed to Initialize due to test", 0);
		verify(listener3, times(1)).onSdkInitializationFailed("SDK Failed to Initialize due to test", 0);

	}

	@Test
	public void testAddAndTriggerOnSdkInitializationFailedMultipleTimes() {
		IInitializationListener listener = mock(IInitializationListener.class);

		initializationNotificationCenter.addListener(listener);

		initializationNotificationCenter.triggerOnSdkInitializationFailed("test", 0);
		initializationNotificationCenter.triggerOnSdkInitializationFailed("test", 0);

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener, times(2)).onSdkInitializationFailed("SDK Failed to Initialize due to test", 0);
	}

	@Test
	public void testRemoveAndTriggerOnSdkInitializationFailed() {
		IInitializationListener listener = mock(IInitializationListener.class);

		initializationNotificationCenter.addListener(listener);

		initializationNotificationCenter.triggerOnSdkInitializationFailed("test", 0);

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		initializationNotificationCenter.removeListener(listener);

		initializationNotificationCenter.triggerOnSdkInitializationFailed("test", 0);

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener, times(1)).onSdkInitializationFailed("SDK Failed to Initialize due to test", 0);
	}

	@Test
	public void testRemoveNotAddedAndTriggerOnSdkInitializationFailed() {
		IInitializationListener listener = mock(IInitializationListener.class);

		initializationNotificationCenter.removeListener(listener);
		initializationNotificationCenter.triggerOnSdkInitializationFailed("test", 0);

		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		verify(listener, times(0)).onSdkInitializationFailed("SDK Failed to Initialize due to test", 0);
	}

}
