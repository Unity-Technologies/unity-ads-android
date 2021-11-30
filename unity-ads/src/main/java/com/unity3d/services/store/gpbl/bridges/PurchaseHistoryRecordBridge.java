package com.unity3d.services.store.gpbl.bridges;

import java.util.HashMap;

public class PurchaseHistoryRecordBridge extends CommonJsonResponseBridge {

	public PurchaseHistoryRecordBridge(Object purchaseHistoryRecord) {
		super(purchaseHistoryRecord, new HashMap<String, Class[]>() {{
			put(getOriginalJsonMethodName, new Class[]{});
		}});
	}

	@Override
	protected String getClassName() {
		return "com.android.billingclient.api.PurchaseHistoryRecord";
	}

}
