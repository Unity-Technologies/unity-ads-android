package com.unity3d.services.store.gpbl.bridges;

import com.unity3d.services.core.log.DeviceLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class PurchaseBridge extends CommonJsonResponseBridge {
	private static final String getSignatureMethodName = "getSignature";

	private final Object _purchase;

	public PurchaseBridge(Object purchase) {
		super(purchase, new HashMap<String, Class<?>[]>() {{
			put(getSignatureMethodName, new Class[]{});
		}});
		_purchase = purchase;
	}

	@Override
	protected String getClassName() {
		return "com.android.billingclient.api.Purchase";
	}

	public String getSignature() {
		return callNonVoidMethod(getSignatureMethodName, _purchase);
	}

	public JSONObject toJson() {
		JSONObject purchaseJson = new JSONObject();
		try {
			purchaseJson.put("purchaseData", getOriginalJson());
			purchaseJson.put("signature", getSignature());
		} catch (JSONException e) {
			DeviceLog.warning("Could not build Purchase result Json: ", e.getMessage());
		}
		return purchaseJson;
	}
}
