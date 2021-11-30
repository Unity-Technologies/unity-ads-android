package com.unity3d.services.store.gpbl.bridges;

import java.util.HashMap;

public class SkuDetailsBridge extends CommonJsonResponseBridge {

	public SkuDetailsBridge(Object skuDetails) {
		super(skuDetails, new HashMap<String, Class[]>() {{
			put(getOriginalJsonMethodName, new Class[]{});
		}});
	}

	@Override
	protected String getClassName() {
		return "com.android.billingclient.api.SkuDetails";
	}

}
