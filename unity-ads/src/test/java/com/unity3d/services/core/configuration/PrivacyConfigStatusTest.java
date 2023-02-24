package com.unity3d.services.core.configuration;

import org.junit.Assert;
import org.junit.Test;

public class PrivacyConfigStatusTest {

	@Test
	public void testPrivacyConfigStatusToLower() {
		Assert.assertEquals("unknown", PrivacyConfigStatus.UNKNOWN.toLowerCase());
		Assert.assertEquals("allowed", PrivacyConfigStatus.ALLOWED.toLowerCase());
		Assert.assertEquals("denied", PrivacyConfigStatus.DENIED.toLowerCase());
	}
}
