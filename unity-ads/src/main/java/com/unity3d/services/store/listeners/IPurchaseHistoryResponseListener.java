package com.unity3d.services.store.listeners;

import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.PurchaseHistoryRecordBridge;

import java.util.List;

public interface IPurchaseHistoryResponseListener extends IBillingDataResponseListener<PurchaseHistoryRecordBridge> {
	void onBillingResponse(BillingResultBridge billingResult, List<PurchaseHistoryRecordBridge> purchaseHistoryRecordList);
}
