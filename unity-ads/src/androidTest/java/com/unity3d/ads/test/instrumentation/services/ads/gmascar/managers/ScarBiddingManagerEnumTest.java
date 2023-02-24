package com.unity3d.ads.test.instrumentation.services.ads.gmascar.managers;

import static org.junit.Assert.assertEquals;

import com.unity3d.services.ads.gmascar.managers.SCARBiddingManagerType;

import org.junit.Test;

public class ScarBiddingManagerEnumTest {

	@Test
	public void testEnumToNameMappings() {
		assertEquals("dis", SCARBiddingManagerType.DISABLED.getName());
		assertEquals("hyb", SCARBiddingManagerType.HYBRID.getName());
		assertEquals("laz", SCARBiddingManagerType.LAZY.getName());
		assertEquals("eag", SCARBiddingManagerType.EAGER.getName());
	}

	@Test
	public void testNameToEnumMappings() {
		assertEquals(SCARBiddingManagerType.DISABLED, SCARBiddingManagerType.fromName("dis"));
		assertEquals(SCARBiddingManagerType.HYBRID, SCARBiddingManagerType.fromName("hyb"));
		assertEquals(SCARBiddingManagerType.LAZY, SCARBiddingManagerType.fromName("laz"));
		assertEquals(SCARBiddingManagerType.EAGER, SCARBiddingManagerType.fromName("eag"));
	}
}
