package com.unity3d.ads.test.legacy;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.ads.BuildConfig;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;

@RunWith(AndroidJUnit4.class)
public class SdkPropertiesTest {

	private static final String CONFIG_URL = "http://unityads.unity3d.com/";

	@BeforeClass
	public static void prepareTests() {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
	}

	@Before
	public void resetTests() throws Exception {
		SdkProperties.setInitialized(false);
		SdkProperties.setTestMode(false);
		SdkProperties.setConfigUrl(CONFIG_URL);
		SdkProperties.resetInitializationListeners();
		SdkProperties.setInitializeState(SdkProperties.InitializationState.NOT_INITIALIZED);
	}

	@Test
	public void testIsInitialized() {
		assertEquals("isInitialized should be false when starting each test", false, SdkProperties.isInitialized());
		SdkProperties.setInitialized(true);
		assertEquals("setInitialized was used but isInitialized doesn't correspond", true, SdkProperties.isInitialized());
	}

	@Test
	public void testSetAndGetInitializeState() {
		assertEquals(SdkProperties.InitializationState.NOT_INITIALIZED, SdkProperties.getCurrentInitializationState());
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);
		assertEquals(SdkProperties.InitializationState.INITIALIZING, SdkProperties.getCurrentInitializationState());
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_FAILED);
		assertEquals(SdkProperties.InitializationState.INITIALIZED_FAILED, SdkProperties.getCurrentInitializationState());
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY);
		assertEquals(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY, SdkProperties.getCurrentInitializationState());
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
	public void testGetVersionCode() {
		assertEquals("Version code not what it should be", BuildConfig.VERSION_CODE, SdkProperties.getVersionCode());
	}

	@Test
	public void testIsChinaLocale() {
		assertTrue("Should return true with a china iso alpha 2 code", SdkProperties.isChinaLocale("cn"));
		assertTrue("Should return true with a china iso alpha 3 code", SdkProperties.isChinaLocale("chn"));
		assertTrue("Should return true with an uppercase china iso alpha 2 code", SdkProperties.isChinaLocale("CN"));
		assertTrue("Should return true with an uppercase china iso alpha 3 code", SdkProperties.isChinaLocale("CHN"));
		assertTrue("Should return true with an upper and lowercase china iso alpha 3 code", SdkProperties.isChinaLocale("ChN"));

		assertFalse("Should return false with a US iso code", SdkProperties.isChinaLocale("us"));
	}

	@Test
	public void testGetInitializationListeners() {
		IUnityAdsInitializationListener[] initializationListeners = SdkProperties.getInitializationListeners();

		assertEquals(0, initializationListeners.length);
	}

	@Test
	public void testAddInitializationListenerAndGetInitializationListeners() {
		IUnityAdsInitializationListener initializationListener = Mockito.mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener);

		IUnityAdsInitializationListener[] initializationListeners = SdkProperties.getInitializationListeners();

		assertEquals(1, initializationListeners.length);
		assertTrue(initializationListeners[0] == initializationListener);
	}

	@Test
	public void testAddMultipleSameInitializationListenerAndGetInitializationListeners() {
		IUnityAdsInitializationListener initializationListener1 = Mockito.mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener1);
		SdkProperties.addInitializationListener(initializationListener1);

		IUnityAdsInitializationListener[] initializationListeners = SdkProperties.getInitializationListeners();

		assertEquals(1, initializationListeners.length);
		assertTrue(initializationListeners[0] == initializationListener1);
	}

	@Test
	public void testAddMultipleInitializationListenerAndGetInitializationListeners() {
		IUnityAdsInitializationListener initializationListener1 = Mockito.mock(IUnityAdsInitializationListener.class);
		IUnityAdsInitializationListener initializationListener2 = Mockito.mock(IUnityAdsInitializationListener.class);
		IUnityAdsInitializationListener initializationListener3 = Mockito.mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener1);
		SdkProperties.addInitializationListener(initializationListener2);
		SdkProperties.addInitializationListener(initializationListener3);

		IUnityAdsInitializationListener[] initializationListeners = SdkProperties.getInitializationListeners();

		assertEquals(3, initializationListeners.length);
		assertTrue(initializationListeners[0] == initializationListener1);
		assertTrue(initializationListeners[1] == initializationListener2);
		assertTrue(initializationListeners[2] == initializationListener3);

	}

	@Test
	public void testResetInitializationListenersAndGetInitializationListeners() {
		IUnityAdsInitializationListener initializationListener1 = Mockito.mock(IUnityAdsInitializationListener.class);
		IUnityAdsInitializationListener initializationListener2 = Mockito.mock(IUnityAdsInitializationListener.class);
		IUnityAdsInitializationListener initializationListener3 = Mockito.mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener1);
		SdkProperties.addInitializationListener(initializationListener2);
		SdkProperties.addInitializationListener(initializationListener3);

		IUnityAdsInitializationListener[] initializationListeners = SdkProperties.getInitializationListeners();
		assertEquals(3, initializationListeners.length);

		SdkProperties.resetInitializationListeners();
		initializationListeners = SdkProperties.getInitializationListeners();

		assertEquals(0, initializationListeners.length);
	}

	@Test
	public void testAddInitializationListenerAfterResetInitializationListenersAndGetInitializationListeners() {
		IUnityAdsInitializationListener initializationListener = Mockito.mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener);

		IUnityAdsInitializationListener[] initializationListeners = SdkProperties.getInitializationListeners();
		assertEquals(1, initializationListeners.length);

		SdkProperties.resetInitializationListeners();
		initializationListeners = SdkProperties.getInitializationListeners();

		assertEquals(0, initializationListeners.length);

		SdkProperties.addInitializationListener(initializationListener);
		initializationListeners = SdkProperties.getInitializationListeners();

		assertEquals(1, initializationListeners.length);
		assertTrue(initializationListeners[0] == initializationListener);
	}

	@Test
	public void testNotifyInitializationCompleteMultipleListeners() {
		IUnityAdsInitializationListener initializationListener1 = Mockito.mock(IUnityAdsInitializationListener.class);
		IUnityAdsInitializationListener initializationListener2 = Mockito.mock(IUnityAdsInitializationListener.class);
		IUnityAdsInitializationListener initializationListener3 = Mockito.mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener1);
		SdkProperties.addInitializationListener(initializationListener2);
		SdkProperties.addInitializationListener(initializationListener3);

		IUnityAdsInitializationListener[] initializationListeners = SdkProperties.getInitializationListeners();
		assertEquals(3, initializationListeners.length);

		SdkProperties.notifyInitializationComplete();

		assertEquals(SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY, SdkProperties.getCurrentInitializationState());
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener1, times(1)).onInitializationComplete();
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener2, times(1)).onInitializationComplete();
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener3, times(1)).onInitializationComplete();
	}

	@Test
	public void testNotifyInitializationFailed() {
		IUnityAdsInitializationListener initializationListener = Mockito.mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener);

		IUnityAdsInitializationListener[] initializationListeners = SdkProperties.getInitializationListeners();
		assertEquals(1, initializationListeners.length);

		SdkProperties.notifyInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, "Sdk failed to initialize.");

		assertEquals(SdkProperties.InitializationState.INITIALIZED_FAILED, SdkProperties.getCurrentInitializationState());
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener, times(1)).onInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, "Sdk failed to initialize.");
	}
}
