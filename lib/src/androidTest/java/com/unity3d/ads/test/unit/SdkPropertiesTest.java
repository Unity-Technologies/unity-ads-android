package com.unity3d.ads.test.unit;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.BuildConfig;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SdkPropertiesTest {

	private static final String CONFIG_URL = "http://unityads.unity3d.com/";

	@BeforeClass
	public static void prepareTests() {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
	}

	@Before
	public void resetTests() throws Exception {
		SdkProperties.setInitialized(false);
		SdkProperties.setTestMode(false);
		SdkProperties.setConfigUrl(CONFIG_URL);
	}

	@Test
	public void testIsInitialized() {
		assertEquals("isInitialized should be false when starting each test", false, SdkProperties.isInitialized());
		SdkProperties.setInitialized(true);
		assertEquals("setInitialized was used but isInitialized doesn't correspond", true, SdkProperties.isInitialized());
	}

	@Test
	public void testTestMode() {
		assertEquals("isTestMode should be false when starting each test", false, SdkProperties.isTestMode());
		SdkProperties.setTestMode(true);
		assertEquals("setTestMode was used but isTestMode doesn't correspond", true, SdkProperties.isTestMode());
	}

	@Test
	public void testConfigUrl() throws MalformedURLException, URISyntaxException {
		assertEquals("getConfigUrl value wasn't default in the beginning of the test", CONFIG_URL, SdkProperties.getConfigUrl());
		SdkProperties.setConfigUrl("http://unityads.unity3d.com/test");
		assertEquals("setConfigUrl was used but getConfigUrl doesn't correspond", "http://unityads.unity3d.com/test", SdkProperties.getConfigUrl());
	}

	@Test (expected = MalformedURLException.class)
	public void testNullConfigUrl () throws MalformedURLException, URISyntaxException {
		assertEquals("getConfigUrl value wasn't default in the beginning of the test", CONFIG_URL, SdkProperties.getConfigUrl());
		SdkProperties.setConfigUrl(null);
	}

	@Test (expected = MalformedURLException.class)
	public void testNonHTTPorHTTPSConfigUrl () throws MalformedURLException, URISyntaxException {
		assertEquals("getConfigUrl value wasn't default in the beginning of the test", CONFIG_URL, SdkProperties.getConfigUrl());
		SdkProperties.setConfigUrl("test://unityads.unity3d.com/");
	}

	@Test (expected = URISyntaxException.class)
	public void testMalformedConfigUrl () throws MalformedURLException, URISyntaxException {
		assertEquals("getConfigUrl value wasn't default in the beginning of the test", "http://unityads.unity3d.com/", SdkProperties.getConfigUrl());
		SdkProperties.setConfigUrl("http://u n i t y a d s . u n i t y 3 d . c o m /");
	}

	@Test
	public void testCacheDirectory() {
		assertNotNull("getCacheDirectory should not ever return null", SdkProperties.getCacheDirectory());
		assertNotNull("getCacheDirectory.getAbsolutePath() should not ever return null", SdkProperties.getCacheDirectory().getAbsolutePath());
		assertTrue("getCacheDirectory.getAbsolutePath() should not ever return null", SdkProperties.getCacheDirectory().getAbsolutePath().length() > 10);
	}

	@Test
	public void testCacheDirectoryName()  {
		assertNotNull("getCacheDirectoryName should not ever return null", SdkProperties.getCacheDirectoryName());
		assertEquals("cacheDirectoryName should be " + "UnityAdsCache".length() + " characters long", "UnityAdsCache".length(), SdkProperties.getCacheDirectoryName().length());
		assertEquals("cacheDirectoryName should be \"UnityAdsCache\",", "UnityAdsCache", SdkProperties.getCacheDirectoryName());
	}

	@Test
	public void testGetSdkVersion() {
		assertNotNull("getSdkVersion should not ever return null", SdkProperties.getVersionName());
	}

	@Test
	public void testShowTimeout() {
		int timeout = 1234;

		SdkProperties.setShowTimeout(timeout);
		assertEquals("show timeout not what it should be", timeout, SdkProperties.getShowTimeout());
	}

	@Test
	public void testGetVersionCode() {
		assertEquals("Version code not what it should be", BuildConfig.VERSION_CODE, SdkProperties.getVersionCode());
	}
}