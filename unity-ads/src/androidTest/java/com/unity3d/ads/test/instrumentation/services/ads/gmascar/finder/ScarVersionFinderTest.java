package com.unity3d.ads.test.instrumentation.services.ads.gmascar.finder;

import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;
import com.unity3d.services.ads.gmascar.bridges.mobileads.IMobileAdsBridge;
import com.unity3d.services.ads.gmascar.bridges.mobileads.MobileAdsBridge;
import com.unity3d.services.ads.gmascar.bridges.mobileads.MobileAdsBridgeLegacy;
import com.unity3d.services.ads.gmascar.finder.GMAInitializer;
import com.unity3d.services.ads.gmascar.finder.PresenceDetector;
import com.unity3d.services.ads.gmascar.finder.ScarVersionFinder;
import com.unity3d.services.ads.gmascar.utils.GMAEventSender;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doCallRealMethod;

@RunWith(MockitoJUnitRunner.class)
public class ScarVersionFinderTest {
	@Mock
	IMobileAdsBridge mobileAdsBridgeMock;
	@Mock
	PresenceDetector presenceDetector;
	@Mock
	GMAInitializer gmaInitializer;

	@Before
	public void setup() {
		Mockito.when(gmaInitializer.getInitializeListenerBridge()).thenReturn(Mockito.mock(InitializeListenerBridge.class));
		mobileAdsBridgeMock = Mockito.mock(MobileAdsBridgeLegacy.class);
	}

	@Test
	public void testScarVersionFinderWithMobileAdsBridgeLegacy() {
		doCallRealMethod().when(mobileAdsBridgeMock).getVersionCodeIndex();

		Mockito.when(mobileAdsBridgeMock.getVersionString()).thenReturn("afma-sdk-a-v204890999.210402000.1");
		ScarVersionFinder scarVersionFinder = new ScarVersionFinder(mobileAdsBridgeMock, presenceDetector, gmaInitializer, new GMAEventSender());
		long versionCode = scarVersionFinder.getVersionCode();
		Assert.assertEquals(MobileAdsBridgeLegacy.CODE_20_0, versionCode);
	}

	@Test
	public void testScarVersionFinderWithMobileAdsBridge() {
		MobileAdsBridge mobileAdsBridgeMock = Mockito.mock(MobileAdsBridge.class);
		Mockito.when(mobileAdsBridgeMock.getVersionString()).thenReturn("21.0.0");
		ScarVersionFinder scarVersionFinder = new ScarVersionFinder(mobileAdsBridgeMock, presenceDetector, gmaInitializer, new GMAEventSender());
		long versionCode = scarVersionFinder.getVersionCode();
		Assert.assertEquals(MobileAdsBridge.CODE_21, versionCode);
	}

	@Test
	public void testScarVersionFinderNullVersion() {
		Mockito.when(mobileAdsBridgeMock.getVersionString()).thenReturn(null);
		ScarVersionFinder scarVersionFinder = new ScarVersionFinder(mobileAdsBridgeMock, presenceDetector, gmaInitializer, new GMAEventSender());
		long versionCode = scarVersionFinder.getVersionCode();
		Assert.assertEquals(-1, versionCode);
	}

	@Test
	public void testScarVersionFinderMissingVersion() {
		Mockito.when(mobileAdsBridgeMock.getVersionString()).thenReturn("");
		ScarVersionFinder scarVersionFinder = new ScarVersionFinder(mobileAdsBridgeMock, presenceDetector, gmaInitializer, new GMAEventSender());
		long versionCode = scarVersionFinder.getVersionCode();
		Assert.assertEquals(-1, versionCode);
	}

	@Test
	public void testScarVersionFinderInvalidVersion() {
		Mockito.when(mobileAdsBridgeMock.getVersionString()).thenReturn("invalid");
		ScarVersionFinder scarVersionFinder = new ScarVersionFinder(mobileAdsBridgeMock, presenceDetector, gmaInitializer, new GMAEventSender());
		long versionCode = scarVersionFinder.getVersionCode();
		Assert.assertEquals(-1, versionCode);
	}

}
