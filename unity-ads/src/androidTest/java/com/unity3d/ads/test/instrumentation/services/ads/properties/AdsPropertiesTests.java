package com.unity3d.ads.test.instrumentation.services.ads.properties;

import androidx.test.rule.ActivityTestRule;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.properties.AdsProperties;
import com.unity3d.ads.test.instrumentation.InstrumentationTestActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AdsPropertiesTests {

	@Rule
	public final ActivityTestRule<InstrumentationTestActivity> _activityRule = new ActivityTestRule<>(InstrumentationTestActivity.class);

	@Before
	public void before() {
		for (IUnityAdsListener listener : AdsProperties.getListeners()) {
			AdsProperties.removeListener(listener);
		}
		AdsProperties.setListener(null);
	}

	@Test
	public void testSetListener() {
		IUnityAdsListener listener = Mockito.mock(IUnityAdsListener.class);
		AdsProperties.setListener(listener);

		// test listener set
		assertEquals(listener, AdsProperties.getListener());
		// test that getListeners includes listener from set
		assertTrue(AdsProperties.getListeners().contains(listener));
		assertEquals(1, AdsProperties.getListeners().size());
	}

	@Test
	public void testGetListener() {
		// should be null when no listener is set
		assertNull(AdsProperties.getListener());
	}

	@Test
	public void testAddListener() {
		IUnityAdsListener listener = Mockito.mock(IUnityAdsListener.class);
		AdsProperties.addListener(listener);

		Set<IUnityAdsListener> listeners = AdsProperties.getListeners();

		assertEquals(1, listeners.size());
		assertTrue(listeners.contains(listener));
	}

	@Test
	public void testAddMultipleListener() {
		IUnityAdsListener listener1 = Mockito.mock(IUnityAdsListener.class);
		IUnityAdsListener listener2 = Mockito.mock(IUnityAdsListener.class);

		AdsProperties.addListener(listener1);
		AdsProperties.addListener(listener2);

		Set<IUnityAdsListener> listeners = AdsProperties.getListeners();

		assertEquals(2, listeners.size());
		assertTrue(listeners.contains(listener1));
		assertTrue(listeners.contains(listener2));
	}

	@Test
	public void testRemoveListener() {
		IUnityAdsListener listener = Mockito.mock(IUnityAdsListener.class);

		AdsProperties.addListener(listener);

		Set<IUnityAdsListener> listeners = AdsProperties.getListeners();

		assertEquals(1, listeners.size());
		assertTrue(listeners.contains(listener));

		AdsProperties.removeListener(listener);
		listeners = AdsProperties.getListeners();

		assertEquals(0, listeners.size());
		assertFalse(listeners.contains(listener));
	}

	@Test
	public void testRemoveDuringIteration() {
		IUnityAdsListener listener1 = Mockito.mock(IUnityAdsListener.class);
		IUnityAdsListener listener2 = Mockito.mock(IUnityAdsListener.class);

		AdsProperties.addListener(listener1);
		AdsProperties.addListener(listener2);

		Set<IUnityAdsListener> listeners = AdsProperties.getListeners();
		try {
			for (IUnityAdsListener listener : listeners) {
				AdsProperties.removeListener(listener);
			}
			assertTrue(true);
		} catch (Exception e) {
			fail("An exception should not be thrown");
		}
	}

	@Test
	public void testInitializeMultipleListeners() {
		IUnityAdsListener listener1 = Mockito.mock(IUnityAdsListener.class);
		IUnityAdsListener listener2 = Mockito.mock(IUnityAdsListener.class);

		UnityAds.initialize(_activityRule.getActivity(), "14851", listener1);
		UnityAds.initialize(_activityRule.getActivity(), "14851", listener2);

		assertEquals(2, AdsProperties.getListeners().size());
		assertTrue(AdsProperties.getListeners().contains(listener1));
		assertTrue(AdsProperties.getListeners().contains(listener2));
		assertTrue(AdsProperties.getListener().equals(listener1));
	}

	@Test
	public void testWrappingFirstListener() {
		IUnityAdsListener listener1 = Mockito.mock(IUnityAdsListener.class);
		IUnityAdsListener listener2 = Mockito.mock(IUnityAdsListener.class);
		IUnityAdsListener listener3 = Mockito.mock(IUnityAdsListener.class);

		// Initialize with first listener
		UnityAds.initialize(_activityRule.getActivity(), "14851", listener1);

		assertEquals(1, AdsProperties.getListeners().size());
		assertTrue(UnityAds.getListener().equals(listener1));

		// Replace listener from initialize
		UnityAds.setListener(listener2);

		assertEquals(1, AdsProperties.getListeners().size());
		assertTrue(UnityAds.getListener().equals(listener2));
		assertTrue(AdsProperties.getListeners().contains(listener2));
		assertFalse(AdsProperties.getListeners().contains(listener1));

		// Add another listener that should only be added
		UnityAds.addListener(listener3);

		assertTrue(UnityAds.getListener().equals(listener2));
		assertEquals(2, AdsProperties.getListeners().size());
		assertFalse(AdsProperties.getListeners().contains(listener1));
		assertTrue(AdsProperties.getListeners().contains(listener2));
		assertTrue(AdsProperties.getListeners().contains(listener3));
	}

	@Test
	public void testSetOverwriteInit() {
		IUnityAdsListener listener1 = Mockito.mock(IUnityAdsListener.class);
		IUnityAdsListener listener2 = Mockito.mock(IUnityAdsListener.class);
		IUnityAdsListener listener3 = Mockito.mock(IUnityAdsListener.class);

		UnityAds.initialize(_activityRule.getActivity(), "14851", listener1);
		UnityAds.setListener(listener2);
		UnityAds.initialize(_activityRule.getActivity(), "14851", listener3);

		assertEquals(2, AdsProperties.getListeners().size());
		assertFalse(AdsProperties.getListeners().contains(listener1));
		assertTrue(AdsProperties.getListeners().contains(listener2));
		assertTrue(AdsProperties.getListeners().contains(listener3));
	}

}
