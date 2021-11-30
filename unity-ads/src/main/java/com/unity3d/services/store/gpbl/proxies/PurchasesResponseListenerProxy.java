package com.unity3d.services.store.gpbl.proxies;

import com.unity3d.services.core.reflection.GenericListenerProxy;
import com.unity3d.services.store.listeners.IPurchasesResponseListener;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.PurchaseBridge;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PurchasesResponseListenerProxy extends GenericListenerProxy {
	private static final String onQueryPurchasesResponseMethodName = "onQueryPurchasesResponse";
	private IPurchasesResponseListener _purchasesResponseListener;
	public PurchasesResponseListenerProxy(IPurchasesResponseListener purchasesResponseListener) {
		_purchasesResponseListener = purchasesResponseListener;
	}

	@Override
	public Class<?> getProxyClass() throws ClassNotFoundException {
		return getProxyListenerClass();
	}

	public static Class<?> getProxyListenerClass() throws ClassNotFoundException {
		return Class.forName("com.android.billingclient.api.PurchasesResponseListener");
	}

	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
	{
		Object result = null;

		if (m.getName().equals(onQueryPurchasesResponseMethodName)) {
			onQueryPurchasesResponse(args[0], (List<Object>) args[1]);
		} else {
			result = super.invoke(proxy, m, args);
		}
		return result;
	}

	/**
	 * Wraps onQueryPurchasesResponse (BillingResult billingResult, List<Purchase> purchases)
	 * from the reflected billing library.
	 * @param billingResult Billing result from the operation.
	 * @param purchases List of Purchase objects received from the query.
	 */
	public void onQueryPurchasesResponse(Object billingResult, List<Object> purchases) {
		BillingResultBridge billingResultBridge = new BillingResultBridge(billingResult);
		List<PurchaseBridge> purchasesBridge = new ArrayList<>();
		for (Object purchase : purchases) {
			purchasesBridge.add(new PurchaseBridge(purchase));
		}
		_purchasesResponseListener.onBillingResponse(billingResultBridge, purchasesBridge);
	}

}