package com.unity3d.services.store.gpbl;

import android.content.Context;

import com.unity3d.services.store.gpbl.bridges.BillingClientBridge;
import com.unity3d.services.store.gpbl.bridges.SkuDetailsParamsBridge;
import com.unity3d.services.store.gpbl.proxies.BillingClientStateListenerProxy;
import com.unity3d.services.store.gpbl.proxies.PurchaseHistoryResponseListenerProxy;
import com.unity3d.services.store.gpbl.proxies.PurchaseUpdatedListenerProxy;
import com.unity3d.services.store.gpbl.proxies.PurchasesResponseListenerProxy;
import com.unity3d.services.store.gpbl.proxies.SkuDetailsResponseListenerProxy;
import com.unity3d.services.store.listeners.IPurchaseHistoryResponseListener;
import com.unity3d.services.store.listeners.IPurchaseUpdatedResponseListener;
import com.unity3d.services.store.listeners.IPurchasesResponseListener;
import com.unity3d.services.store.listeners.ISkuDetailsResponseListener;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class StoreBilling {
	private final BillingClientBridge _billingClientBridge;

	public StoreBilling(Context context, IPurchaseUpdatedResponseListener purchaseUpdatedResponseListener) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		BillingClientBridge.BuilderBridge builderBridge = BillingClientBridge.newBuilder(context);
		_billingClientBridge = builderBridge.setListener(new PurchaseUpdatedListenerProxy(purchaseUpdatedResponseListener)).enablePendingPurchases().build();
	}

	public void initialize(IBillingClientStateListener billingClientStateListener) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		_billingClientBridge.startConnection(new BillingClientStateListenerProxy(billingClientStateListener));
	}

	/**
	 *
	 * @param purchaseType Type of purchase (either inapp or subs)
	 * @return 0 If the feature is supported, -1 if unsupported
	 */
	public int isFeatureSupported(String purchaseType) {
		boolean featureSupported;
		String internalPurchaseType = purchaseType;
		if (internalPurchaseType.equals("inapp")) {
			// "inapp" isn't part of the isFeatureSupported API and a call to isReady is what is recommended for "inapp"
			featureSupported = _billingClientBridge.isReady();
		} else {
			if (internalPurchaseType.equals("subs")) {
				internalPurchaseType = "subscriptions";
			}
			featureSupported = (_billingClientBridge.isFeatureSupported(internalPurchaseType) == BillingResultResponseCode.OK);
		}
		return featureSupported ? 0 : -1;
	}

	public void getPurchases(String purchaseType, IPurchasesResponseListener purchasesResponseListener) throws ClassNotFoundException {
		_billingClientBridge.queryPurchasesAsync(purchaseType, new PurchasesResponseListenerProxy(purchasesResponseListener));
	}

	public void getSkuDetails(String purchaseType, ArrayList<String> skuList, ISkuDetailsResponseListener skuDetailsResponseListener) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		SkuDetailsParamsBridge.BuilderBridge skuDetailsParamsBuilderBridge = SkuDetailsParamsBridge.newBuilder();
		SkuDetailsParamsBridge skuDetailsParamsBridge = skuDetailsParamsBuilderBridge.setSkuList(skuList).setType(purchaseType).build();
		_billingClientBridge.querySkuDetailsAsync(skuDetailsParamsBridge, new SkuDetailsResponseListenerProxy(skuDetailsResponseListener));
	}

	public void getPurchaseHistory(String purchaseType, int maxPurchases, IPurchaseHistoryResponseListener purchaseHistoryResponseListener) throws ClassNotFoundException {
		_billingClientBridge.queryPurchaseHistoryAsync(purchaseType, new PurchaseHistoryResponseListenerProxy(purchaseHistoryResponseListener, maxPurchases));
	}
}
