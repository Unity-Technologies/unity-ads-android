package com.unity3d.ads.test.instrumentation.services.ads.gmascar.finder;

import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;
import com.unity3d.services.ads.gmascar.bridges.MobileAdsBridge;
import com.unity3d.services.ads.gmascar.finder.GMAInitializer;
import com.unity3d.services.ads.gmascar.finder.PresenceDetector;
import com.unity3d.services.ads.gmascar.finder.ScarVersionFinder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScarVersionFinderTest {
	@Mock
	MobileAdsBridge mobileAdsBridgeMock;
	@Mock
	PresenceDetector presenceDetector;
	@Mock
	GMAInitializer gmaInitializer;

	@Before
	public void setup() {
		Mockito.when(gmaInitializer.getInitializeListenerBridge()).thenReturn(Mockito.mock(InitializeListenerBridge.class));
	}

	@Test
	public void testScarVersionFinder() {
		Mockito.when(mobileAdsBridgeMock.getVersionString()).thenReturn("afma-sdk-a-v204890999.203404000.1");
		ScarVersionFinder scarVersionFinder = new ScarVersionFinder(mobileAdsBridgeMock, presenceDetector, gmaInitializer);
		long versionCode = scarVersionFinder.getGoogleSdkVersionCode();
		Assert.assertEquals(203404000, versionCode);
	}


	@Test
	public void testScarVersionFinderNullVersion() {
		MobileAdsBridge mobileAdsBridgeMock = Mockito.mock(MobileAdsBridge.class);
		Mockito.when(mobileAdsBridgeMock.getVersionString()).thenReturn(null);
		ScarVersionFinder scarVersionFinder = new ScarVersionFinder(mobileAdsBridgeMock, presenceDetector, gmaInitializer);
		long versionCode = scarVersionFinder.getGoogleSdkVersionCode();
		Assert.assertEquals(-1, versionCode);
	}

	@Test
	public void testScarVersionFinderMissingVersion() {
		MobileAdsBridge mobileAdsBridgeMock = Mockito.mock(MobileAdsBridge.class);
		Mockito.when(mobileAdsBridgeMock.getVersionString()).thenReturn("");
		ScarVersionFinder scarVersionFinder = new ScarVersionFinder(mobileAdsBridgeMock, presenceDetector, gmaInitializer);
		long versionCode = scarVersionFinder.getGoogleSdkVersionCode();
		Assert.assertEquals(-1, versionCode);
	}

	@Test
	public void testScarVersionFinderInvalidVersion() {
		MobileAdsBridge mobileAdsBridgeMock = Mockito.mock(MobileAdsBridge.class);
		Mockito.when(mobileAdsBridgeMock.getVersionString()).thenReturn("invalid");
		ScarVersionFinder scarVersionFinder = new ScarVersionFinder(mobileAdsBridgeMock, presenceDetector, gmaInitializer);
		long versionCode = scarVersionFinder.getGoogleSdkVersionCode();
		Assert.assertEquals(-1, versionCode);
	}
}
