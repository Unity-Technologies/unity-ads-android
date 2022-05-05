package com.unity3d.services.store.gpbl.bridges.billingclient.common;

import com.unity3d.services.core.reflection.GenericBridge;
import com.unity3d.services.store.gpbl.bridges.billingclient.IBillingClientBuilderBridge;
import com.unity3d.services.store.gpbl.proxies.PurchaseUpdatedListenerProxy;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public abstract class BillingClientBuilderBridgeCommon extends GenericBridge implements IBillingClientBuilderBridge {
	private static final String setListenerMethodName = "setListener";
	private static final String enablePendingPurchasesMethodName = "enablePendingPurchases";
	protected static final String buildMethodName = "build";

	protected Object _billingClientBuilderInternalInstance;

	public BillingClientBuilderBridgeCommon(Object billingClientBuilderInternalInstance) throws ClassNotFoundException {
		super(new HashMap<String, Class<?>[]>() {{
			put(setListenerMethodName, new Class[]{PurchaseUpdatedListenerProxy.getProxyListenerClass()});
			put(enablePendingPurchasesMethodName, new Class[]{});
			put(buildMethodName, new Class[]{});
		}});
		_billingClientBuilderInternalInstance = billingClientBuilderInternalInstance;
	}

	@Override
	protected String getClassName() {
		return "com.android.billingclient.api.BillingClient$Builder";
	}

	@Override
	public IBillingClientBuilderBridge setListener(PurchaseUpdatedListenerProxy purchaseUpdatedListenerProxy) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		_billingClientBuilderInternalInstance = callNonVoidMethod(setListenerMethodName, _billingClientBuilderInternalInstance, purchaseUpdatedListenerProxy.getProxyInstance());
		return this;
	}

	@Override
	public IBillingClientBuilderBridge enablePendingPurchases() {
		_billingClientBuilderInternalInstance = callNonVoidMethod(enablePendingPurchasesMethodName, _billingClientBuilderInternalInstance);
		return this;
	}
}
