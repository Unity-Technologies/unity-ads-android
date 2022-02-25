package com.unity3d.services.store.gpbl.proxies;

import com.unity3d.services.core.reflection.GenericListenerProxy;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.PurchaseHistoryRecordBridge;
import com.unity3d.services.store.listeners.IPurchaseHistoryResponseListener;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PurchaseHistoryResponseListenerProxy extends GenericListenerProxy {
	private static final String onPurchaseHistoryResponseMethodName = "onPurchaseHistoryResponse";
	private IPurchaseHistoryResponseListener _purchaseUpdatedResponseListener;
	private int _maxPurchases;

	public PurchaseHistoryResponseListenerProxy(IPurchaseHistoryResponseListener purchaseHistoryResponseListener, int maxPurchases) {
		_purchaseUpdatedResponseListener = purchaseHistoryResponseListener;
		_maxPurchases = maxPurchases;
	}

	@Override
	public Class<?> getProxyClass() throws ClassNotFoundException {
		return getProxyListenerClass();
	}

	public static Class<?> getProxyListenerClass() throws ClassNotFoundException {
		return Class.forName("com.android.billingclient.api.PurchaseHistoryResponseListener");
	}

	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		Object result = null;
		if (m.getName().equals(onPurchaseHistoryResponseMethodName)) {
			onPurchaseHistoryResponse(args[0], (List<Object>) args[1]);
		} else {
			result = super.invoke(proxy, m, args);
		}
		return result;
	}

	// Wraps onPurchaseHistoryResponse(BillingResult billingResult, List<PurchaseHistoryRecord> purchaseHistoryRecordList)
	public void onPurchaseHistoryResponse(Object billingResult, List<Object> purchaseHistoryRecordList) {
		List<PurchaseHistoryRecordBridge> purchaseHistoryRecordBridges = null;
		if (purchaseHistoryRecordList != null) {
			purchaseHistoryRecordBridges = new ArrayList<>();
			for (int purchaseCount = 0; purchaseCount < _maxPurchases && purchaseCount < purchaseHistoryRecordList.size(); purchaseCount++) {
				purchaseHistoryRecordBridges.add(new PurchaseHistoryRecordBridge(purchaseHistoryRecordList.get(purchaseCount)));
			}
		}
		if (_purchaseUpdatedResponseListener != null) {
			_purchaseUpdatedResponseListener.onBillingResponse(new BillingResultBridge(billingResult), purchaseHistoryRecordBridges);
		}
	}

}