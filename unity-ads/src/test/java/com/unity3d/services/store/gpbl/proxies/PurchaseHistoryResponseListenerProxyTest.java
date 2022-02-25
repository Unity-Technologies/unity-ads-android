package com.unity3d.services.store.gpbl.proxies;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.PurchaseHistoryRecordBridge;
import com.unity3d.services.store.listeners.IPurchaseHistoryResponseListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class PurchaseHistoryResponseListenerProxyTest {
	@Mock
	IPurchaseHistoryResponseListener purchaseHistoryResponseListener;

	@Test
	public void testPurchaseHistoryResponseListenerProxy() {
		setPurchaseHistoryListenerForTest(BillingClient.BillingResponseCode.OK, new ArrayList<>());
		Mockito.verify(purchaseHistoryResponseListener).onBillingResponse(Mockito.any(BillingResultBridge.class), Mockito.<PurchaseHistoryRecordBridge>anyList());
	}

	@Test
	public void testPurchaseHistoryResponseListenerProxyNoItem() {
		setPurchaseHistoryListenerForTest(BillingClient.BillingResponseCode.OK, new ArrayList<>(), 0);
		Mockito.verify(purchaseHistoryResponseListener).onBillingResponse(Mockito.any(BillingResultBridge.class), Mockito.<PurchaseHistoryRecordBridge>anyList());
	}

	@Test
	public void testPurchaseHistoryResponseListenerProxyNegativeItem() {
		setPurchaseHistoryListenerForTest(BillingClient.BillingResponseCode.OK, new ArrayList<>(), -1);
		Mockito.verify(purchaseHistoryResponseListener).onBillingResponse(Mockito.any(BillingResultBridge.class), Mockito.<PurchaseHistoryRecordBridge>anyList());
	}

	@Test
	public void testPurchaseHistoryResponseListenerProxyTooManyItem() {
		setPurchaseHistoryListenerForTest(BillingClient.BillingResponseCode.OK, new ArrayList<>(), 9999);
		Mockito.verify(purchaseHistoryResponseListener).onBillingResponse(Mockito.any(BillingResultBridge.class), Mockito.<PurchaseHistoryRecordBridge>anyList());
	}

	@Test
	public void testPurchaseHistoryResponseListenerProxyNullList() {
		setPurchaseHistoryListenerForTest(BillingClient.BillingResponseCode.OK, null);
		Mockito.verify(purchaseHistoryResponseListener).onBillingResponse(Mockito.any(BillingResultBridge.class), ArgumentMatchers.<List<PurchaseHistoryRecordBridge>>isNull());
	}

	private void setPurchaseHistoryListenerForTest(int responseCode, List<Object> purchaseHistoryRecordList) {
		setPurchaseHistoryListenerForTest(responseCode, purchaseHistoryRecordList, 1);
	}

	private void setPurchaseHistoryListenerForTest(int responseCode, List<Object> purchaseHistoryRecordList, int maxPurchases) {
		PurchaseHistoryResponseListenerProxy purchaseHistoryResponseListenerProxy = new PurchaseHistoryResponseListenerProxy(purchaseHistoryResponseListener, maxPurchases);
		purchaseHistoryResponseListenerProxy.onPurchaseHistoryResponse(BillingResult.newBuilder().setResponseCode(responseCode).build(), purchaseHistoryRecordList);
	}
}
