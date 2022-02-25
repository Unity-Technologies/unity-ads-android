package com.unity3d.services.store.gpbl.proxies;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.SkuDetailsBridge;
import com.unity3d.services.store.listeners.ISkuDetailsResponseListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class SkuDetailsResponseListenerProxyTest {
	@Mock
	ISkuDetailsResponseListener skuDetailsResponseListener;

	@Test
	public void testSkuDetailsResponseListenerProxy() {
		setPurchaseUpdatedListenerForTest(BillingClient.BillingResponseCode.OK, new ArrayList<>());
		Mockito.verify(skuDetailsResponseListener).onBillingResponse(Mockito.any(BillingResultBridge.class), Mockito.<SkuDetailsBridge>anyList());
	}

	@Test
	public void testSkuDetailsResponseListenerNullProxy() {
		SkuDetailsResponseListenerProxy skuDetailsResponseListenerProxy = new SkuDetailsResponseListenerProxy(null);
		skuDetailsResponseListenerProxy.onSkuDetailsResponse(null, null);
	}

	@Test
	public void testSkuDetailsResponseProxyNullList() {
		setPurchaseUpdatedListenerForTest(BillingClient.BillingResponseCode.OK, null);
		Mockito.verify(skuDetailsResponseListener).onBillingResponse(Mockito.any(BillingResultBridge.class), ArgumentMatchers.<List<SkuDetailsBridge>>isNull());
	}

	private void setPurchaseUpdatedListenerForTest(int responseCode, List<Object> skuDetailsList) {
		SkuDetailsResponseListenerProxy skuDetailsResponseListenerProxy = new SkuDetailsResponseListenerProxy(skuDetailsResponseListener);
		skuDetailsResponseListenerProxy.onSkuDetailsResponse(BillingResult.newBuilder().setResponseCode(responseCode).build(), skuDetailsList);
	}
}
