package com.unity3d.ads.test.instrumentation.services.core.configuration;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.core.configuration.PrivacyConfigStatus;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PrivacyConfigStatusTest {

	@Test
	public void testPrivacyConfigStatusToLower() {
		Assert.assertEquals("unknown", PrivacyConfigStatus.UNKNOWN.toLowerCase());
		Assert.assertEquals("allowed", PrivacyConfigStatus.ALLOWED.toLowerCase());
		Assert.assertEquals("denied", PrivacyConfigStatus.DENIED.toLowerCase());
	}
}
