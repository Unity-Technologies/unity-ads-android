package com.unity3d.services.store.gpbl.bridges;

public class PurchaseHistoryRecordBridge extends CommonJsonResponseBridge {

	public PurchaseHistoryRecordBridge(Object purchaseHistoryRecord) {
		super(purchaseHistoryRecord);
	}

	@Override
	protected String getClassName() {
		return "com.android.billingclient.api.PurchaseHistoryRecord";
	}

}
