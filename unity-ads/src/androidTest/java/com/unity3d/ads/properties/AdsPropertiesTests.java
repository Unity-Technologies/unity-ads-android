package com.unity3d.ads.properties;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.test.instrumentation.services.ads.mocks.UnityAdsListenerMock;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AdsPropertiesTests {

	@Before
	public void before() {
		for (IUnityAdsListener listener : AdsProperties.getListeners()) {
			AdsProperties.removeListener(listener);
		}
	}

	@Test
	public void testAddListener() {
		UnityAdsListenerMock listener = new UnityAdsListenerMock();
		AdsProperties.addListener(listener);
		Set<IUnityAdsListener> listeners = AdsProperties.getListeners();
		assertEquals(1, listeners.size());
		assertTrue(listeners.contains(listener));
	}

	@Test
	public void testAddMultipleListener() {
		UnityAdsListenerMock listener1 = new UnityAdsListenerMock();
		AdsProperties.addListener(listener1);
		UnityAdsListenerMock listener2 = new UnityAdsListenerMock();
		AdsProperties.addListener(listener2);
		Set<IUnityAdsListener> listeners = AdsProperties.getListeners();
		assertEquals(2, listeners.size());
		assertTrue(listeners.contains(listener1));
		assertTrue(listeners.contains(listener2));
	}

	@Test
	public void testRemoveListener() {
		UnityAdsListenerMock listener = new UnityAdsListenerMock();
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
		UnityAdsListenerMock listener1 = new UnityAdsListenerMock();
		AdsProperties.addListener(listener1);
		UnityAdsListenerMock listener2 = new UnityAdsListenerMock();
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

}
