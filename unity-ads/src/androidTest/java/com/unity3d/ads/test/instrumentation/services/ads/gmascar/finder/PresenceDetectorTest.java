package com.unity3d.ads.test.instrumentation.services.ads.gmascar.finder;

import com.unity3d.services.ads.gmascar.bridges.AdapterStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializationStatusBridge;
import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;
import com.unity3d.services.ads.gmascar.finder.PresenceDetector;
import com.unity3d.services.ads.gmascar.finder.ScarVersionFinder;
import com.unity3d.services.ads.gmascar.bridges.MobileAdsBridge;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PresenceDetectorTest {
	@Mock
	MobileAdsBridge mobileAdsBridgeMock;
	@Mock
	InitializeListenerBridge initializeListenerBridgeMock;
	@Mock
	InitializationStatusBridge initializationStatusBridgeMock;
	@Mock
	AdapterStatusBridge adapterStatusBridgeMock;

	@Before
	public void setup() {
		Mockito.when(mobileAdsBridgeMock.exists()).thenReturn(true);
		Mockito.when(initializeListenerBridgeMock.exists()).thenReturn(true);
		Mockito.when(initializationStatusBridgeMock.exists()).thenReturn(true);
		Mockito.when(adapterStatusBridgeMock.exists()).thenReturn(true);
	}

	@Test
	public void testScarPresenceDetector() {
		PresenceDetector scarPresenceDetector = new PresenceDetector(mobileAdsBridgeMock, initializeListenerBridgeMock, initializationStatusBridgeMock, adapterStatusBridgeMock);
		Assert.assertTrue(scarPresenceDetector.areGMAClassesPresent());
	}

	@Test
	public void testScarPresenceDetectorWithNullBridges() {
		PresenceDetector scarPresenceDetector = new PresenceDetector(null, null, null, null);
		Assert.assertFalse(scarPresenceDetector.areGMAClassesPresent());
	}

	@Test
	public void testScarPresenceDetectorMobileAdsClassMissing() {
		Mockito.when(mobileAdsBridgeMock.exists()).thenReturn(false);
		PresenceDetector scarPresenceDetector = new PresenceDetector(mobileAdsBridgeMock, initializeListenerBridgeMock, initializationStatusBridgeMock, adapterStatusBridgeMock);
		Assert.assertFalse(scarPresenceDetector.areGMAClassesPresent());
	}

	@Test
	public void testScarPresenceDetectorListenerClassMissing() {
		Mockito.when(initializeListenerBridgeMock.exists()).thenReturn(false);
		PresenceDetector scarPresenceDetector = new PresenceDetector(mobileAdsBridgeMock, initializeListenerBridgeMock, initializationStatusBridgeMock, adapterStatusBridgeMock);
		Assert.assertFalse(scarPresenceDetector.areGMAClassesPresent());
	}

	@Test
	public void testScarPresenceDetectorInitStatusClassMissing() {
		Mockito.when(initializationStatusBridgeMock.exists()).thenReturn(false);
		PresenceDetector scarPresenceDetector = new PresenceDetector(mobileAdsBridgeMock, initializeListenerBridgeMock, initializationStatusBridgeMock, adapterStatusBridgeMock);
		Assert.assertFalse(scarPresenceDetector.areGMAClassesPresent());
	}

	@Test
	public void testScarPresenceDetectorAdapterStatusClassMissing() {
		Mockito.when(adapterStatusBridgeMock.exists()).thenReturn(false);
		PresenceDetector scarPresenceDetector = new PresenceDetector(mobileAdsBridgeMock, initializeListenerBridgeMock, initializationStatusBridgeMock, adapterStatusBridgeMock);
		Assert.assertFalse(scarPresenceDetector.areGMAClassesPresent());
	}
}
