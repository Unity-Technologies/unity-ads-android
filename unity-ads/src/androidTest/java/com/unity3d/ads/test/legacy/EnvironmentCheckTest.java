package com.unity3d.ads.test.legacy;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.core.configuration.EnvironmentCheck;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class EnvironmentCheckTest {
	@Test
	public void testIsEnvironmentCheckOk() {
		assertTrue("Environment check failed, SDK will fail init", EnvironmentCheck.isEnvironmentOk());
	}

	@Test
	public void testProGuardTest() {
		assertTrue("Environment check for ProGuard failed", EnvironmentCheck.testProGuard());
	}

	@Test
	public void testCacheDirectory() {
		assertTrue("Environment check for cache directory failed", EnvironmentCheck.testCacheDirectory());
	}
}