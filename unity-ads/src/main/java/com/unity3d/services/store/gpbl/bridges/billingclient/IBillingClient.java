package com.unity3d.services.store.gpbl.bridges.billingclient;

import com.unity3d.services.store.gpbl.BillingResultResponseCode;
import com.unity3d.services.store.gpbl.bridges.SkuDetailsParamsBridge;
import com.unity3d.services.store.gpbl.proxies.BillingClientStateListenerProxy;
import com.unity3d.services.store.gpbl.proxies.PurchaseHistoryResponseListenerProxy;
import com.unity3d.services.store.gpbl.proxies.PurchasesResponseListenerProxy;
import com.unity3d.services.store.gpbl.proxies.SkuDetailsResponseListenerProxy;

public interface IBillingClient {
	void startConnection(BillingClientStateListenerProxy billingClientStateListenerProxy) throws ClassNotFoundException;
	BillingResultResponseCode isFeatureSupported(String purchaseType);
	boolean isReady();
	void querySkuDetailsAsync(SkuDetailsParamsBridge params, SkuDetailsResponseListenerProxy skuDetailsResponseListenerProxy) throws ClassNotFoundException;
	void queryPurchaseHistoryAsync(String skuType, PurchaseHistoryResponseListenerProxy purchaseHistoryResponseListenerProxy) throws ClassNotFoundException;
	void queryPurchasesAsync(String purchaseType, PurchasesResponseListenerProxy purchasesResponseListenerProxy) throws ClassNotFoundException;
}