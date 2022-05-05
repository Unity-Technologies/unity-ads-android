package com.unity3d.services.store.gpbl.bridges;

import com.unity3d.services.core.reflection.GenericBridge;
import com.unity3d.services.store.gpbl.BillingResultResponseCode;

import java.util.HashMap;

public class BillingResultBridge extends GenericBridge {
	private static final String getResponseCodeMethodName = "getResponseCode";
	private final Object _billingResult;

	public BillingResultBridge(Object billingResult) {
		super(new HashMap<String, Class<?>[]>() {{
			put(getResponseCodeMethodName, new Class[]{});
		}});
		_billingResult = billingResult;
	}

	@Override
	protected String getClassName() {
		return "com.android.billingclient.api.BillingResult";
	}

	public BillingResultResponseCode getResponseCode() {
		int responseCode = callNonVoidMethod(getResponseCodeMethodName, _billingResult);
		return BillingResultResponseCode.fromResponseCode(responseCode);
	}
}
