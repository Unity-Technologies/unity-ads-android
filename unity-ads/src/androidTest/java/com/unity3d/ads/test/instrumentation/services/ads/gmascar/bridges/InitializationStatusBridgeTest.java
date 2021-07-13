package com.unity3d.ads.test.instrumentation.services.ads.gmascar.bridges;

import com.unity3d.services.ads.gmascar.bridges.InitializationStatusBridge;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class InitializationStatusBridgeTest {

	@Test
	public void testInitializationStatusBridgeNotInitialized() {
		InitializationStatusBridge initializationStatusBridge = new InitializationStatusBridge();
		Map<String, Object> adapterStatusMap = initializationStatusBridge.getAdapterStatusMap(new Object());
		Assert.assertNull(adapterStatusMap);
	}
}
