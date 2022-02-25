package com.unity3d.services.store.gpbl.proxies;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.PurchaseBridge;
import com.unity3d.services.store.listeners.IPurchaseUpdatedResponseListener;
import com.unity3d.services.store.listeners.IPurchasesResponseListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PurchaseUpdatedListenerProxyTest {
	@Mock
	IPurchaseUpdatedResponseListener purchaseUpdatedResponseListener;

	@Test
	public void testPurchaseUpdatedListenerProxy() {
		setPurchaseUpdatedListenerForTest(BillingClient.BillingResponseCode.OK, new ArrayList<>());
		Mockito.verify(purchaseUpdatedResponseListener).onBillingResponse(Mockito.any(BillingResultBridge.class), Mockito.<PurchaseBridge>anyList());
	}

	@Test
	public void testPurchaseUpdatedListenerNullProxy() {
		PurchaseUpdatedListenerProxy purchaseHistoryResponseListenerProxy = new PurchaseUpdatedListenerProxy(null);
		purchaseHistoryResponseListenerProxy.onPurchasesUpdated(null, null);
	}

	@Test
	public void testPurchaseUpdatedListenerProxyNullList() {
		setPurchaseUpdatedListenerForTest(BillingClient.BillingResponseCode.OK, null);
		Mockito.verify(purchaseUpdatedResponseListener).onBillingResponse(Mockito.any(BillingResultBridge.class), ArgumentMatchers.<List<PurchaseBridge>>isNull());
	}

	private void setPurchaseUpdatedListenerForTest(int responseCode, List<Object> purchasesRecordList) {
		PurchaseUpdatedListenerProxy purchaseHistoryResponseListenerProxy = new PurchaseUpdatedListenerProxy(purchaseUpdatedResponseListener);
		purchaseHistoryResponseListenerProxy.onPurchasesUpdated(BillingResult.newBuilder().setResponseCode(responseCode).build(), purchasesRecordList);
	}
}
