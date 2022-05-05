package com.unity3d.services.store.gpbl.bridges.billingclient.v4;

import android.content.Context;

import com.unity3d.services.store.gpbl.bridges.billingclient.common.BillingClientBridgeCommon;
import com.unity3d.services.store.gpbl.bridges.billingclient.common.BillingClientBuilderBridgeCommon;
import com.unity3d.services.store.gpbl.proxies.PurchasesResponseListenerProxy;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class BillingClientBridge extends BillingClientBridgeCommon {

	private static final String queryPurchasesAsyncMethodName = "queryPurchasesAsync";

	public BillingClientBridge(Object billingClientInternalInstance) throws ClassNotFoundException {
		super(billingClientInternalInstance, new HashMap<String, Class<?>[]>() {{
			put(queryPurchasesAsyncMethodName, new Class[]{String.class, PurchasesResponseListenerProxy.getProxyListenerClass()});
		}});
	}

	@Override
	public void queryPurchasesAsync(String purchaseType, PurchasesResponseListenerProxy purchasesResponseListenerProxy) throws ClassNotFoundException {
		callVoidMethod(queryPurchasesAsyncMethodName, _billingClientInternalInstance, purchaseType, purchasesResponseListenerProxy.getProxyInstance());
	}

	public static boolean isAvailable() {
		boolean isAvailable = false;
		try {
			Class<?> billingClientClass = getClassForBridge();
			billingClientClass.getMethod(queryPurchasesAsyncMethodName, String.class, PurchasesResponseListenerProxy.getProxyListenerClass());
			isAvailable = true;
		} catch (NoSuchMethodException | ClassNotFoundException ignored) {
			
		}
		return isAvailable;
	}

	public static BuilderBridge newBuilder(Context context) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
		Object billingClientBuilderInternalInstance = callNonVoidStaticMethod(newBuilderMethodName, context);
		return new BuilderBridge(billingClientBuilderInternalInstance);
	}

	public static class BuilderBridge extends BillingClientBuilderBridgeCommon {

		public BuilderBridge(Object billingClientBuilderInternalInstance) throws ClassNotFoundException {
			super(billingClientBuilderInternalInstance);
		}

		public BillingClientBridgeCommon build() throws ClassNotFoundException {
			Object billingClient = callNonVoidMethod(buildMethodName, _billingClientBuilderInternalInstance);
			return new BillingClientBridge(billingClient);
		}
	}
}
