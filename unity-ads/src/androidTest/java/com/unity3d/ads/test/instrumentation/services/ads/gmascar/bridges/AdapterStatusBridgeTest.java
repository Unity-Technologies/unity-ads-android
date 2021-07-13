package com.unity3d.ads.test.instrumentation.services.ads.gmascar.bridges;

import com.google.android.gms.ads.initialization.AdapterStatus;
import com.unity3d.services.ads.gmascar.bridges.AdapterStatusBridge;

import org.junit.Assert;
import org.junit.Test;

public class AdapterStatusBridgeTest {

	@Test
	public void testAdapterStatusBridge() {
		AdapterStatusBridge adapterStatusBridge = new AdapterStatusBridge();
		Object[] statesEnum = adapterStatusBridge.getAdapterStatesEnum();
		Assert.assertEquals(AdapterStatus.State.values().length, statesEnum.length);
		Assert.assertEquals(AdapterStatus.State.NOT_READY, statesEnum[0]);
		Assert.assertEquals(AdapterStatus.State.READY, statesEnum[1]);
	}
}
