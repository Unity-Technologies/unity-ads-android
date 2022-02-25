package com.unity3d.services.store.gpbl.proxies;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.PurchaseBridge;
import com.unity3d.services.store.listeners.IPurchasesResponseListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PurchasesResponseListenerProxyTest {
	@Mock
	IPurchasesResponseListener purchasesResponseListener;

	@Test
	public void testPurchasesResponseListenerProxy() {
		setPurchasesListenerForTest(BillingClient.BillingResponseCode.OK, new ArrayList<>());
		Mockito.verify(purchasesResponseListener).onBillingResponse(Mockito.any(BillingResultBridge.class), Mockito.<PurchaseBridge>anyList());
	}

	@Test
	public void testPurchasesResponseListenerNullProxy() {
		PurchasesResponseListenerProxy purchasesResponseListenerProxy = new PurchasesResponseListenerProxy(null);
		purchasesResponseListenerProxy.onQueryPurchasesResponse(null, null);
	}

	@Test
	public void testPurchasesResponseListenerProxyNullList() {
		setPurchasesListenerForTest(BillingClient.BillingResponseCode.OK, null);
		Mockito.verify(purchasesResponseListener).onBillingResponse(Mockito.any(BillingResultBridge.class), ArgumentMatchers.<List<PurchaseBridge>>isNull());
	}

	private void setPurchasesListenerForTest(int responseCode, List<Object> purchasesRecordList) {
		PurchasesResponseListenerProxy purchasesResponseListenerProxy = new PurchasesResponseListenerProxy(purchasesResponseListener);
		purchasesResponseListenerProxy.onQueryPurchasesResponse(BillingResult.newBuilder().setResponseCode(responseCode).build(), purchasesRecordList);
	}
}
