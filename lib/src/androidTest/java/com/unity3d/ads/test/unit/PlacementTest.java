package com.unity3d.ads.test.unit;

import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.placement.Placement;
import com.unity3d.ads.properties.SdkProperties;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PlacementTest {
	@Test
	public void testReset() {
		SdkProperties.setInitialized(false);
		Placement.reset();

		assertFalse("Placement reset test: default placement isReady not false", Placement.isReady());
		assertFalse("Placement reset test: random placement isReady not false", Placement.isReady("1234"));
		assertEquals("Placement reset test: default placement getPlacementState is not NOT_AVAILABLE", Placement.getPlacementState(), UnityAds.PlacementState.NOT_AVAILABLE);
		assertEquals("Placement reset test: random placement getPlacementState is not NOT_AVAILABLE", Placement.getPlacementState("1234"), UnityAds.PlacementState.NOT_AVAILABLE);
		assertNull("Placement reset test: default placement defined after reset", Placement.getDefaultPlacement());
	}

	@Test
	public void testDefaultPlacement() {
		String testPlacement = "testPlacement";

		Placement.reset();
		SdkProperties.setInitialized(true);
		assertFalse("Default placement test: default placement ready when not configured", UnityAds.isReady());
		assertEquals("Default placement test: default placement state is not NOT_AVAILABLE when not configured", UnityAds.getPlacementState(), UnityAds.PlacementState.NOT_AVAILABLE);

		Placement.setDefaultPlacement("testPlacement");

		assertEquals("Default placement test: placement names do not match", testPlacement, Placement.getDefaultPlacement());

		Placement.setPlacementState(testPlacement, UnityAds.PlacementState.WAITING.name());

		assertFalse("Default placement test: placement is ready when it should not be ready due to waiting state", UnityAds.isReady());
		assertEquals("Default placement test: placement is not waiting", UnityAds.getPlacementState(), UnityAds.PlacementState.WAITING);

		Placement.setPlacementState(testPlacement, UnityAds.PlacementState.READY.name());
		assertTrue("Default placement test: placement is not ready", UnityAds.isReady());
		assertEquals("Default placement test: placement status is not ready", UnityAds.getPlacementState(), UnityAds.PlacementState.READY);
	}

	public void testCustomPlacement() {
		String customPlacement = "customPlacement";

		Placement.reset();
		SdkProperties.setInitialized(true);
		assertFalse("Custom placement test: placement is ready before placement configuration", UnityAds.isReady(customPlacement));
		assertEquals("Custom placement test: placement state is not NOT_AVAILABLE before placement configuration", UnityAds.getPlacementState(customPlacement), UnityAds.PlacementState.NOT_AVAILABLE);

		Placement.setPlacementState(customPlacement, UnityAds.PlacementState.DISABLED.name());
		assertFalse("Custom placement test: placement is ready but placement state disabled means placement should not be ready", UnityAds.isReady(customPlacement));
		assertEquals("Custom placement test: placement state is not DISABLED after disabling", UnityAds.getPlacementState(customPlacement), UnityAds.PlacementState.DISABLED);

		Placement.setPlacementState(customPlacement, UnityAds.PlacementState.NO_FILL.name());

		assertFalse("Custom placement test: placement is ready but placement state no fill means placement should not be ready", UnityAds.isReady(customPlacement));
		assertEquals("Custom placement test: placement state is not NO_FILL after set to no fill", UnityAds.getPlacementState(customPlacement), UnityAds.PlacementState.NO_FILL);

		Placement.setPlacementState(customPlacement, UnityAds.PlacementState.WAITING.name());
		assertFalse("Custom placement test: placement is ready but placement state waiting means placement should not be ready", UnityAds.isReady(customPlacement));
		assertEquals("Custom placement test: placement state is not WAITING after set to waiting", UnityAds.getPlacementState(customPlacement), UnityAds.PlacementState.WAITING);

		Placement.setPlacementState(customPlacement, UnityAds.PlacementState.READY.name());
		assertTrue("Custom placement test: placement is not ready but placement state ready means placement should be ready", UnityAds.isReady(customPlacement));
		assertEquals("Custom placement test: placement state is not READY after set to ready", UnityAds.getPlacementState(customPlacement), UnityAds.PlacementState.READY);
	}

}