package com.unity3d.services.store.gpbl.bridges.billingclient.v3;

import android.content.Context;

import com.unity3d.services.store.gpbl.bridges.PurchasesResultBridge;
import com.unity3d.services.store.gpbl.bridges.billingclient.IBillingClient;
import com.unity3d.services.store.gpbl.bridges.billingclient.common.BillingClientBridgeCommon;
import com.unity3d.services.store.gpbl.bridges.billingclient.common.BillingClientBuilderBridgeCommon;
import com.unity3d.services.store.gpbl.proxies.PurchasesResponseListenerProxy;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class BillingClientBridge extends BillingClientBridgeCommon {

	private static final String queryPurchasesMethodName = "queryPurchases";

	public BillingClientBridge(Object billingClientInternalInstance) throws ClassNotFoundException {
		super(billingClientInternalInstance, new HashMap<String, Class<?>[]>() {{
			put(queryPurchasesMethodName, new Class[]{String.class});
		}});
	}

	@Override
	public void queryPurchasesAsync(String purchaseType, PurchasesResponseListenerProxy purchasesResponseListenerProxy) {
		// Fake async callback for GPLv3 as the getPurchasesAsync API was not introduced until GPLv4
		Object result = callNonVoidMethod(queryPurchasesMethodName, _billingClientInternalInstance, purchaseType);
		PurchasesResultBridge purchasesResultBridge = new PurchasesResultBridge(result);
		purchasesResponseListenerProxy.getPurchasesResponseListener().onBillingResponse(purchasesResultBridge.getBillingResult(), purchasesResultBridge.getPurchasesList());
	}

	public static BuilderBridge newBuilder(Context context) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
		Object billingClientBuilderInternalInstance = callNonVoidStaticMethod(newBuilderMethodName, context);
		return new BuilderBridge(billingClientBuilderInternalInstance);
	}

	public static class BuilderBridge extends BillingClientBuilderBridgeCommon {

		public BuilderBridge(Object billingClientBuilderInternalInstance) throws ClassNotFoundException {
			super(billingClientBuilderInternalInstance);
		}

		public IBillingClient build() throws ClassNotFoundException {
			Object billingClient = callNonVoidMethod(buildMethodName, _billingClientBuilderInternalInstance);
			return new BillingClientBridge(billingClient);
		}
	}
}
