package com.unity3d.ads.test.instrumentation.services.ads.gmascar.bridges;

import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.unity3d.services.ads.gmascar.bridges.mobileads.MobileAdsBridge;
import com.unity3d.services.ads.gmascar.finder.ScarAdapterVersion;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class MobileAdsBridgeTest {

	@Test
	public void testGetAdapterVersionWhen21() {
		MobileAdsBridge mobileAdsBridge = new MobileAdsBridge();
		ScarAdapterVersion adapterVersion = mobileAdsBridge.getAdapterVersion(MobileAdsBridge.CODE_21);
		Assert.assertEquals(ScarAdapterVersion.V21, adapterVersion);
	}

	@Test
	public void testGetAdapterVersionWhenAnything() {
		MobileAdsBridge mobileAdsBridge = new MobileAdsBridge();
		ScarAdapterVersion adapterVersion = mobileAdsBridge.getAdapterVersion(999);
		Assert.assertEquals(ScarAdapterVersion.V21, adapterVersion);
	}

	@Test
	public void testGetAdapterVersionWhenError() {
		MobileAdsBridge mobileAdsBridge = new MobileAdsBridge();
		ScarAdapterVersion adapterVersion = mobileAdsBridge.getAdapterVersion(-1);
		Assert.assertEquals(ScarAdapterVersion.NA, adapterVersion);
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
