package com.unity3d.services.store.gpbl.proxies;

import com.unity3d.services.core.reflection.GenericListenerProxy;
import com.unity3d.services.store.listeners.IPurchaseUpdatedResponseListener;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.PurchaseBridge;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PurchaseUpdatedListenerProxy extends GenericListenerProxy {
	private static final String onPurchasesUpdatedMethodName = "onPurchasesUpdated";
	private IPurchaseUpdatedResponseListener _purchaseUpdatedResponseListener;
	public PurchaseUpdatedListenerProxy(IPurchaseUpdatedResponseListener purchaseUpdatedResponseListener) {
		_purchaseUpdatedResponseListener = purchaseUpdatedResponseListener;
	}

	@Override
	public Class<?> getProxyClass() throws ClassNotFoundException {
		return getProxyListenerClass();
	}

	public static Class<?> getProxyListenerClass() throws ClassNotFoundException {
		return Class.forName("com.android.billingclient.api.PurchasesUpdatedListener");
	}

	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		Object result = null;
		if (m.getName().equals(onPurchasesUpdatedMethodName)) {
			onPurchasesUpdated(args[0], (List<Object>) args[1]);
		} else {
			result = super.invoke(proxy, m, args);
		}
		return result;
	}
	// Wraps onPurchasesUpdated (BillingResult billingResult, List<Purchase> purchases)
	public void onPurchasesUpdated(Object billingResult, List<Object> purchases) {
		List<PurchaseBridge> purchasesBridge = null;
		if (purchases != null) {
			purchasesBridge = new ArrayList<>();
			for (Object purchase : purchases) {
				purchasesBridge.add(new PurchaseBridge(purchase));
			}
		}
		if (_purchaseUpdatedResponseListener != null) {
			_purchaseUpdatedResponseListener.onBillingResponse(new BillingResultBridge(billingResult), purchasesBridge);
		}

	}
}