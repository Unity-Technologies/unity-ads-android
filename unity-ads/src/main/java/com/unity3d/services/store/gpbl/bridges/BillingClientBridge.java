package com.unity3d.services.store.gpbl.bridges;

import android.content.Context;

import com.unity3d.services.core.reflection.GenericBridge;
import com.unity3d.services.store.gpbl.BillingResultResponseCode;
import com.unity3d.services.store.gpbl.proxies.BillingClientStateListenerProxy;
import com.unity3d.services.store.gpbl.proxies.PurchaseHistoryResponseListenerProxy;
import com.unity3d.services.store.gpbl.proxies.PurchaseUpdatedListenerProxy;
import com.unity3d.services.store.gpbl.proxies.PurchasesResponseListenerProxy;
import com.unity3d.services.store.gpbl.proxies.SkuDetailsResponseListenerProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BillingClientBridge extends GenericBridge {
	private static final String newBuilderMethodName = "newBuilder";
	private static final String startConnectionMethodName = "startConnection";
	private static final String endConnectionMethodName = "endConnection";
	private static final String querySkuDetailsAsyncMethodName = "querySkuDetailsAsync";
	private static final String queryPurchaseHistoryAsyncMethodName = "queryPurchaseHistoryAsync";
	private static final String queryPurchasesAsyncMethodName = "queryPurchasesAsync";
	private static final String isFeatureSupportedMethodName = "isFeatureSupported";
	private static final String isReadyMethodName = "isReady";

	private static final Map<String, Class<?>[]> staticMethods = new HashMap<String, Class<?>[]>() {{
		put(newBuilderMethodName, new Class[]{Context.class});
	}};

	private final Object _billingClientInternalInstance;

	public BillingClientBridge(Object billingClientInternalInstance) throws ClassNotFoundException {
		super(new HashMap<String, Class[]>() {{
			put(newBuilderMethodName, new Class[]{Context.class});
			put(startConnectionMethodName, new Class[]{BillingClientStateListenerProxy.getProxyListenerClass()});
			put(endConnectionMethodName, new Class[]{});
			put(querySkuDetailsAsyncMethodName, new Class[]{SkuDetailsParamsBridge.getClassForBridge(), SkuDetailsResponseListenerProxy.getProxyListenerClass()});
			put(queryPurchaseHistoryAsyncMethodName, new Class[]{String.class, PurchaseHistoryResponseListenerProxy.getProxyListenerClass()});
			put(queryPurchasesAsyncMethodName, new Class[]{String.class, PurchasesResponseListenerProxy.getProxyListenerClass()});
			put(isFeatureSupportedMethodName, new Class[]{String.class});
			put(isReadyMethodName, new Class[]{});
		}});
		_billingClientInternalInstance = billingClientInternalInstance;
	}

	@Override
	protected String getClassName() {
		return "com.android.billingclient.api.BillingClient";
	}

	public void startConnection(BillingClientStateListenerProxy billingClientStateListenerProxy) throws ClassNotFoundException {
		callVoidMethod(startConnectionMethodName, _billingClientInternalInstance, billingClientStateListenerProxy.getProxyInstance());
	}

	public void endConnection() {
		callVoidMethod(endConnectionMethodName, _billingClientInternalInstance);
	}

	public BillingResultResponseCode isFeatureSupported(String purchaseType) {
		Object billingResult = callNonVoidMethod(isFeatureSupportedMethodName, _billingClientInternalInstance, purchaseType);
		BillingResultBridge billingResultBridge = new BillingResultBridge(billingResult);
		return billingResultBridge.getResponseCode();
	}

	public boolean isReady() {
		return callNonVoidMethod(isReadyMethodName, _billingClientInternalInstance);
	}

	public void querySkuDetailsAsync(SkuDetailsParamsBridge params, SkuDetailsResponseListenerProxy skuDetailsResponseListenerProxy) throws ClassNotFoundException {
		callVoidMethod(querySkuDetailsAsyncMethodName, _billingClientInternalInstance, params.getInternalInstance(), skuDetailsResponseListenerProxy.getProxyInstance());
	}

	public void queryPurchaseHistoryAsync(String skuType, PurchaseHistoryResponseListenerProxy purchaseHistoryResponseListenerProxy) throws ClassNotFoundException {
		callVoidMethod(queryPurchaseHistoryAsyncMethodName, _billingClientInternalInstance, skuType, purchaseHistoryResponseListenerProxy.getProxyInstance());
	}

	public void queryPurchasesAsync(String purchaseType, PurchasesResponseListenerProxy purchasesResponseListenerProxy) throws ClassNotFoundException {
		callVoidMethod(queryPurchasesAsyncMethodName, _billingClientInternalInstance, purchaseType, purchasesResponseListenerProxy.getProxyInstance());
	}

	public static BuilderBridge newBuilder(Context context) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {
		Object billingClientBuilderInternalInstance = callNonVoidStaticMethod(newBuilderMethodName, context);
		return new BuilderBridge(billingClientBuilderInternalInstance);
	}

	public static Object callNonVoidStaticMethod(String methodName, Object... parameters) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		Method method = getClassForBridge().getMethod(methodName, staticMethods.get(methodName));
		return method.invoke(null, parameters);
	}

	private static Class<?> getClassForBridge() throws ClassNotFoundException {
		return Class.forName("com.android.billingclient.api.BillingClient");
	}

	public static class BuilderBridge extends GenericBridge {
		private static final String setListenerMethodName = "setListener";
		private static final String enablePendingPurchasesMethodName = "enablePendingPurchases";
		private static final String buildMethodName = "build";

		private Object _billingClientBuilderInternalInstance;

		public BuilderBridge(Object billingClientBuilderInternalInstance) throws ClassNotFoundException {
			super(new HashMap<String, Class[]>() {{
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

		public BuilderBridge setListener(PurchaseUpdatedListenerProxy purchaseUpdatedListenerProxy) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
			_billingClientBuilderInternalInstance = callNonVoidMethod(setListenerMethodName, _billingClientBuilderInternalInstance, purchaseUpdatedListenerProxy.getProxyInstance());
			return this;
		}

		public BuilderBridge enablePendingPurchases() {
			_billingClientBuilderInternalInstance = callNonVoidMethod(enablePendingPurchasesMethodName, _billingClientBuilderInternalInstance);
			return this;
		}

		public BillingClientBridge build() throws ClassNotFoundException {
			Object billingClient = callNonVoidMethod(buildMethodName, _billingClientBuilderInternalInstance);
			return new BillingClientBridge(billingClient);
		}
	}


}
