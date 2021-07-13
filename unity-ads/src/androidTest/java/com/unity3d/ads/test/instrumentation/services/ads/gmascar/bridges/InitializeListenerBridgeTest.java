package com.unity3d.ads.test.instrumentation.services.ads.gmascar.bridges;

import com.unity3d.services.ads.gmascar.bridges.InitializeListenerBridge;

import org.junit.Assert;
import org.junit.Test;

public class InitializeListenerBridgeTest {

	@Test
	public void testInitializeListenerBridge() {
		InitializeListenerBridge initializeListenerBridge = new InitializeListenerBridge();
		Object listenerProxy = initializeListenerBridge.createInitializeListenerProxy();
		Assert.assertNotNull(listenerProxy);
	}
}
