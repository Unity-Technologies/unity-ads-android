package com.unity3d.ads.test.instrumentation.services.ads.gmascar.bridges;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.unity3d.services.ads.gmascar.bridges.MobileAdsBridge;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.timeout;

public class MobileAdsBridgeTest {

	@Test
	@Ignore("Have to ignore for now cause the bridge is stateful (underlying call to GMA static init depends on other tests")
	public void testMobileAdsBridgeGetVersionNotInitialized() {
		MobileAdsBridge mobileAdsBridge = new MobileAdsBridge();
		String versionString = mobileAdsBridge.getVersionString();
		Assert.assertEquals("0.0.0", versionString);
	}

	@Test
	public void testMobileAdsBridgeGetVersion() {
		OnInitializationCompleteListener initializationCompleteListener = Mockito.mock(OnInitializationCompleteListener.class);
		MobileAdsBridge mobileAdsBridge = new MobileAdsBridge();
		mobileAdsBridge.initialize(InstrumentationRegistry.getInstrumentation().getContext(), initializationCompleteListener);
		Mockito.verify(initializationCompleteListener, timeout(1000).times(1)).onInitializationComplete(Mockito.any(InitializationStatus.class));
		String versionString = mobileAdsBridge.getVersionString();
		Assert.assertTrue(String.format("Minor version 203404000 is not found in %s", versionString), versionString.contains("203404000"));
	}

	@Test
	@Ignore("Cannot test this case since the underlying GMA call is static so test ordering impacts this result.")
	public void testMobileAdsBridgeGetInitStatusNotInitialized() {
		MobileAdsBridge mobileAdsBridge = new MobileAdsBridge();
		Object initializationStatus = mobileAdsBridge.getInitializationStatus();
		Assert.assertNull(initializationStatus);
	}

	@Test
	public void testMobileAdsBridgeGetInitStatus() {
		OnInitializationCompleteListener initializationCompleteListener = Mockito.mock(OnInitializationCompleteListener.class);
		MobileAdsBridge mobileAdsBridge = new MobileAdsBridge();
		mobileAdsBridge.initialize(InstrumentationRegistry.getInstrumentation().getContext(), initializationCompleteListener);
		Object initializationStatus = mobileAdsBridge.getInitializationStatus();
		Assert.assertTrue(InitializationStatus.class.isAssignableFrom(initializationStatus.getClass()));
	}
}
