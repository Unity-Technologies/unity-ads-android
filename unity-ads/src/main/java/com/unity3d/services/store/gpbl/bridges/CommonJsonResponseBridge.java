package com.unity3d.services.store.gpbl.bridges;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.reflection.GenericBridge;
import com.unity3d.services.store.gpbl.IBillingResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public abstract class CommonJsonResponseBridge extends GenericBridge implements IBillingResponse {
	protected static final String getOriginalJsonMethodName = "getOriginalJson";
	private final Object _internalBridgeRef;

	public CommonJsonResponseBridge(Object internalBridgeRef, Map<String, Class[]> functionAndParameters) {
		super(functionAndParameters);
		_internalBridgeRef = internalBridgeRef;
	}

	@Override
	public JSONObject getOriginalJson() {
		String jsonString = callNonVoidMethod(getOriginalJsonMethodName, _internalBridgeRef);
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(jsonString);
		} catch (JSONException e) {
			DeviceLog.error("Couldn't parse BillingResponse JSON : %s", e.getMessage());
		}
		return jsonObject;
	}
}
