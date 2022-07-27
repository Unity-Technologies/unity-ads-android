package com.unity3d.ads.test.instrumentation.services.store.gpbl.bridges;

import com.unity3d.services.store.gpbl.bridges.PurchaseBridge;
import com.unity3d.services.store.gpbl.bridges.PurchasesResultBridge;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PurchasesResultBridgeTest {
	@Mock
	PurchasesResultBridge _purchasesResultBridge;

	@Test
	public void testPurchasesBridge() {
		Mockito.when(_purchasesResultBridge.getPurchasesList()).thenCallRealMethod();
		Mockito.when(_purchasesResultBridge.callNonVoidMethod(Mockito.anyString(), Mockito.any())).thenReturn(Arrays.asList(new Object(), new Object()));
		List<PurchaseBridge> purchaseBridgeList = _purchasesResultBridge.getPurchasesList();
		Assert.assertNotNull(purchaseBridgeList);
		Assert.assertEquals(2, purchaseBridgeList.size());
	}

	@Test
	public void testPurchasesBridgeReturnNull() {
		Mockito.when(_purchasesResultBridge.getPurchasesList()).thenCallRealMethod();
		Mockito.when(_purchasesResultBridge.callNonVoidMethod(Mockito.anyString(), Mockito.any())).thenReturn(null);
		List<PurchaseBridge> purchaseBridgeList = _purchasesResultBridge.getPurchasesList();
		Assert.assertNotNull(purchaseBridgeList);
		Assert.assertEquals(0, purchaseBridgeList.size());
	}
}
