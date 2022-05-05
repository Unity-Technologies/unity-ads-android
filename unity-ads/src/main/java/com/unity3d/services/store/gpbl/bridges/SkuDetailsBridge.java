package com.unity3d.services.store.gpbl.bridges;

public class SkuDetailsBridge extends CommonJsonResponseBridge {

	public SkuDetailsBridge(Object skuDetails) {
		super(skuDetails);
	}

	@Override
	protected String getClassName() {
		return "com.android.billingclient.api.SkuDetails";
	}

}
