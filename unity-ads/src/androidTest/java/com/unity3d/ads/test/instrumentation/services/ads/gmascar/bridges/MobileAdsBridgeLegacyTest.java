package com.unity3d.ads.test.instrumentation.services.ads.gmascar.bridges;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.unity3d.services.ads.gmascar.bridges.mobileads.MobileAdsBridgeLegacy;
import com.unity3d.services.ads.gmascar.finder.ScarAdapterVersion;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.timeout;

public class MobileAdsBridgeLegacyTest {

	@Test
	@Ignore("Have to ignore for now cause the bridge is stateful (underlying call to GMA static init depends on other tests")
	public void testMobileAdsBridgeGetVersionNotInitialized() {
		MobileAdsBridgeLegacy mobileAdsBridge = new MobileAdsBridgeLegacy();
		String versionString = mobileAdsBridge.getVersionString();
		Assert.assertEquals("0.0.0", versionString);
	}

	@Test
	public void testMobileAdsBridgeV20GetVersion() {
		OnInitializationCompleteListener initializationCompleteListener = Mockito.mock(OnInitializationCompleteListener.class);
		MobileAdsBridgeLegacy mobileAdsBridge = new MobileAdsBridgeLegacy();
		mobileAdsBridge.initialize(InstrumentationRegistry.getInstrumentation().getContext(), initializationCompleteListener);
		Mockito.verify(initializationCompleteListener, timeout(5000).times(1)).onInitializationComplete(Mockito.any(InitializationStatus.class));
		String versionString = mobileAdsBridge.getVersionString();
		Assert.assertTrue(String.format("Minor version 203404000 is not found in %s", versionString), versionString.contains("203404000"));
	}

	@Test
	public void testGetAdapterVersionWhen192() {
		MobileAdsBridgeLegacy mobileAdsBridge = new MobileAdsBridgeLegacy();
		ScarAdapterVersion adapterVersion = mobileAdsBridge.getAdapterVersion(MobileAdsBridgeLegacy.CODE_19_2);
		Assert.assertEquals(ScarAdapterVersion.V192, adapterVersion);
	}

	@Test
	public void testGetAdapterVersionWhen195() {
		MobileAdsBridgeLegacy mobileAdsBridge = new MobileAdsBridgeLegacy();
		ScarAdapterVersion adapterVersion = mobileAdsBridge.getAdapterVersion(MobileAdsBridgeLegacy.CODE_19_5);
		Assert.assertEquals(ScarAdapterVersion.V195, adapterVersion);
	}

	@Test
	public void testGetAdapterVersionWhen198() {
		MobileAdsBridgeLegacy mobileAdsBridge = new MobileAdsBridgeLegacy();
		ScarAdapterVersion adapterVersion = mobileAdsBridge.getAdapterVersion(MobileAdsBridgeLegacy.CODE_19_8);
		Assert.assertEquals(ScarAdapterVersion.V195, adapterVersion);
	}

	@Test
	public void testGetAdapterVersionWhen20() {
		MobileAdsBridgeLegacy mobileAdsBridge = new MobileAdsBridgeLegacy();
		ScarAdapterVersion adapterVersion = mobileAdsBridge.getAdapterVersion(MobileAdsBridgeLegacy.CODE_20_0);
		Assert.assertEquals(ScarAdapterVersion.V20, adapterVersion);
	}

	@Test
	public void testGetAdapterVersionWhen21() {
		MobileAdsBridgeLegacy mobileAdsBridge = new MobileAdsBridgeLegacy();
		ScarAdapterVersion adapterVersion = mobileAdsBridge.getAdapterVersion(MobileAdsBridgeLegacy.CODE_21_0);
		Assert.assertEquals(ScarAdapterVersion.NA, adapterVersion);
	}

	@Test
	public void testGetAdapterVersionWhenError() {
		MobileAdsBridgeLegacy mobileAdsBridge = new MobileAdsBridgeLegacy();
		ScarAdapterVersion adapterVersion = mobileAdsBridge.getAdapterVersion(-1);
		Assert.assertEquals(ScarAdapterVersion.NA, adapterVersion);
	}

	@Test
	@Ignore("Cannot test this case since the underlying GMA call is static so test ordering impacts this result.")
	public void testMobileAdsBridgeGetInitStatusNotInitialized() {
		MobileAdsBridgeLegacy mobileAdsBridge = new MobileAdsBridgeLegacy();
		Object initializationStatus = mobileAdsBridge.getInitializationStatus();
		Assert.assertNull(initializationStatus);
	}

	@Test
	public void testMobileAdsBridgeGetInitStatus() {
		OnInitializationCompleteListener initializationCompleteListener = Mockito.mock(OnInitializationCompleteListener.class);
		MobileAdsBridgeLegacy mobileAdsBridge = new MobileAdsBridgeLegacy();
		mobileAdsBridge.initialize(InstrumentationRegistry.getInstrumentation().getContext(), initializationCompleteListener);
		Object initializationStatus = mobileAdsBridge.getInitializationStatus();
		Assert.assertTrue(InitializationStatus.class.isAssignableFrom(initializationStatus.getClass()));
	}
}

