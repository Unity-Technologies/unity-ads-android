package com.unity3d.services.store.gpbl.bridges;

import com.unity3d.services.core.reflection.GenericBridge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PurchasesResultBridge extends GenericBridge {
	private static final String getBillingResultMethodName = "getBillingResult";
	private static final String getPurchasesListMethodName = "getPurchasesList";

	private final Object _purchasesResult;

	public PurchasesResultBridge(Object purchasesResult) {
		super(new HashMap<String, Class<?>[]>(){{
			put(getBillingResultMethodName, new Class[]{});
			put(getPurchasesListMethodName, new Class[]{});
		}});
		_purchasesResult = purchasesResult;
	}

	@Override
	protected String getClassName() {
		return "com.android.billingclient.api.Purchase$PurchasesResult";
	}

	public BillingResultBridge getBillingResult() {
		return new BillingResultBridge(callNonVoidMethod(getBillingResultMethodName, _purchasesResult));
	}

	public List<PurchaseBridge> getPurchasesList() {
		List<Object> purchases = callNonVoidMethod(getPurchasesListMethodName, _purchasesResult);
		List<PurchaseBridge> purchasesBridge = new ArrayList<>();
		for (Object purchase : purchases) {
			purchasesBridge.add(new PurchaseBridge(purchase));
		}
		return purchasesBridge;
	}
}
