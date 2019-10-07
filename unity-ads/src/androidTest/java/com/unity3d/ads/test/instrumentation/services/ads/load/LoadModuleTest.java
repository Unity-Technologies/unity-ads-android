package com.unity3d.ads.test.instrumentation.services.ads.load;

import android.support.test.runner.AndroidJUnit4;

import com.unity3d.services.ads.load.ILoadBridge;
import com.unity3d.services.ads.load.LoadModule;
import com.unity3d.services.core.configuration.IInitializationListener;
import com.unity3d.services.core.configuration.IInitializationNotificationCenter;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;
import com.unity3d.services.core.properties.SdkProperties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class LoadModuleTest {

	private ILoadBridge loadBridge;
	private InitializationNotificationCenter initializationNotificationCenter;
	private LoadModule loadModule;
	private IInitializationNotificationCenter notificationCenter;

	private IInitializationListener listener;

	@Before
	public void before() {
		loadBridge = mock(ILoadBridge.class);
		notificationCenter = mock(IInitializationNotificationCenter.class);

		doAnswer(new Answer() {
			public Object answer(InvocationOnMock invocation) {
				listener = invocation.getArgument(0);
				return null;
			}})
			.when(notificationCenter).addListener(any(IInitializationListener.class));

		loadModule = new LoadModule(loadBridge, notificationCenter);
	}

	@Test
	public void testSubscription() {
		verify(notificationCenter, times(1)).addListener(loadModule);
		verify(notificationCenter, times(1)).addListener(any(IInitializationListener.class));
	}

	@Test
	public void testLoadAfterInitialized() {
		SdkProperties.setInitialized(true);
		loadModule.load("test");

		LinkedHashMap<String, Integer> expectedMap = new LinkedHashMap<>();
		expectedMap.put("test", new Integer(1));
		verify(loadBridge, times(1)).loadPlacements(expectedMap);
	}

	@Test
	public void testTwiceLoadAfterInitialized() {
		SdkProperties.setInitialized(true);
		loadModule.load("test1");
		loadModule.load("test2");

		LinkedHashMap<String, Integer> expectedMap = new LinkedHashMap<>();
		expectedMap.put("test1", new Integer(1));
		LinkedHashMap<String, Integer> expectedMap2 = new LinkedHashMap<>();
		expectedMap2.put("test2", new Integer(1));
		verify(loadBridge, times(2)).loadPlacements(any(Map.class));
		verify(loadBridge, times(1)).loadPlacements(expectedMap);
		verify(loadBridge, times(1)).loadPlacements(expectedMap2);
	}

	@Test
	public void testLoadWithNilPlacement() {
		SdkProperties.setInitialized(true);
		loadModule.load(null);
		verify(loadBridge, never()).loadPlacements(any(Map.class));
	}

	@Test
	public void testLoadWithEmptyPlacement() {
		SdkProperties.setInitialized(true);
		loadModule.load("");
		verify(loadBridge, never()).loadPlacements(any(Map.class));
	}

	@Test
	public void testLoadBeforeInitialized() throws Exception {
		SdkProperties.setInitialized(false);
		loadModule.load("test");

		verify(loadBridge, never()).loadPlacements(any(Map.class));

		listener.onSdkInitialized();

		LinkedHashMap<String, Integer> expectedMap = new LinkedHashMap<>();
		expectedMap.put("test", new Integer(1));
		verify(loadBridge, times(1)).loadPlacements(expectedMap);
	}

	@Test
	public void testLoadBeforeInitializedAndBatchInvocation() {
		SdkProperties.setInitialized(false);
		loadModule.load("test1");
		loadModule.load("test2");

		verify(loadBridge, never()).loadPlacements(any(LinkedHashMap.class));

		listener.onSdkInitialized();

		LinkedHashMap<String, Integer> expectedMap = new LinkedHashMap<>();
		expectedMap.put("test1", new Integer(1));
		expectedMap.put("test2", new Integer(1));
		verify(loadBridge, times(1)).loadPlacements(expectedMap);
	}

	@Test
	public void testLoadBeforeInitializedSamePlacement() {
		SdkProperties.setInitialized(false);
		loadModule.load("test1");
		loadModule.load("test1");

		verify(loadBridge, never()).loadPlacements(any(LinkedHashMap.class));

		listener.onSdkInitialized();

		LinkedHashMap<String, Integer> expectedMap = new LinkedHashMap<>();
		expectedMap.put("test1", new Integer(2));
		verify(loadBridge, times(1)).loadPlacements(expectedMap);
	}

	@Test
	public void testLoadBeforeInitializedWithNilPlacement() {
		SdkProperties.setInitialized(false);
		loadModule.load(null);

		listener.onSdkInitialized();

		verify(loadBridge, never()).loadPlacements(any(LinkedHashMap.class));
	}

	@Test
	public void testLoadBeforeInitializedWithEmptyPlacement() {
		SdkProperties.setInitialized(false);
		loadModule.load("");

		listener.onSdkInitialized();

		verify(loadBridge, never()).loadPlacements(any(LinkedHashMap.class));
	}

	@Test
	public void testDidNotInitialized() {
		SdkProperties.setInitialized(false);
		loadModule.load("test1");
		loadModule.load("test2");

		listener.onSdkInitializationFailed("test error", 1);

		verify(loadBridge, never()).loadPlacements(any(LinkedHashMap.class));
	}
}
