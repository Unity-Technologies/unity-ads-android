package com.unity3d.ads.test.unit;

import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.UnityAds;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PublicApiTest {
	@Test
	public void testIsSupported() {
		assertTrue("Public API isSupported did not return true", UnityAds.isSupported());
	}

	@Test
	public void testSDKVersion() {
		assertTrue("Public API SDK version is null", UnityAds.getVersion() != null);
	}

	@Test
	public void testIsReady() {
		assertFalse("Public API default placement ready before init", UnityAds.isReady());
		assertFalse("Public API non-existing placement ready", UnityAds.isReady("nonExistingPlacement"));
	}

	@Test
	public void testDebugMode() {
		UnityAds.setDebugMode(false);

		assertFalse("Public API does not allow disabling debug mode", UnityAds.getDebugMode());

		UnityAds.setDebugMode(true);

		assertTrue("Public API does not allow enabling debug mode", UnityAds.getDebugMode());
	}
}