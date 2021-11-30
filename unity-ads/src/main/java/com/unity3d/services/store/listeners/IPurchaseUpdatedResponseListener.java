package com.unity3d.services.store.listeners;

import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.PurchaseBridge;

import java.util.List;

public interface IPurchaseUpdatedResponseListener extends IBillingDataResponseListener<PurchaseBridge> {
	void onBillingResponse(BillingResultBridge billingResult, List<PurchaseBridge> purchases);
}
