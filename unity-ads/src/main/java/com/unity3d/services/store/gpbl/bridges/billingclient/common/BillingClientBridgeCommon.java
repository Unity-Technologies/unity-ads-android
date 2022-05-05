package com.unity3d.services.store.gpbl.bridges.billingclient.common;

import android.content.Context;

import com.unity3d.services.core.reflection.GenericBridge;
import com.unity3d.services.store.gpbl.BillingResultResponseCode;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.SkuDetailsParamsBridge;
import com.unity3d.services.store.gpbl.bridges.billingclient.IBillingClient;
import com.unity3d.services.store.gpbl.proxies.BillingClientStateListenerProxy;
import com.unity3d.services.store.gpbl.proxies.PurchaseHistoryResponseListenerProxy;
import com.unity3d.services.store.gpbl.proxies.SkuDetailsResponseListenerProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class BillingClientBridgeCommon extends GenericBridge implements IBillingClient {
	protected static final String newBuilderMethodName = "newBuilder";
	protected static final String startConnectionMethodName = "startConnection";
	protected static final String endConnectionMethodName = "endConnection";
	protected static final String querySkuDetailsAsyncMethodName = "querySkuDetailsAsync";
	protected static final String queryPurchaseHistoryAsyncMethodName = "queryPurchaseHistoryAsync";
	protected static final String isFeatureSupportedMethodName = "isFeatureSupported";
	protected static final String isReadyMethodName = "isReady";
	protected final Object _billingClientInternalInstance;

	private static final Map<String, Class<?>[]> staticMethods = new HashMap<String, Class<?>[]>() {{
		put(newBuilderMethodName, new Class[]{Context.class});
	}};

	public BillingClientBridgeCommon(Object billingClientInternalInstance, Map<String, Class<?>[]> functionAndParameters) throws ClassNotFoundException {
		super(appendFunctionAnParameters(functionAndParameters));
		_billingClientInternalInstance = billingClientInternalInstance;
	}

	private static Map<String, Class<?>[]> appendFunctionAnParameters(Map<String, Class<?>[]> functionAndParameters) throws ClassNotFoundException {
		functionAndParameters.putAll(new HashMap<String, Class<?>[]>() {{
			put(newBuilderMethodName, new Class[]{Context.class});
			put(startConnectionMethodName, new Class[]{BillingClientStateListenerProxy.getProxyListenerClass()});
			put(endConnectionMethodName, new Class[]{});
			put(querySkuDetailsAsyncMethodName, new Class[]{SkuDetailsParamsBridge.getClassForBridge(), SkuDetailsResponseListenerProxy.getProxyListenerClass()});
			put(queryPurchaseHistoryAsyncMethodName, new Class[]{String.class, PurchaseHistoryResponseListenerProxy.getProxyListenerClass()});
			put(isFeatureSupportedMethodName, new Class[]{String.class});
			put(isReadyMethodName, new Class[]{});
		}});
		return functionAndParameters;
	}

	@Override
	protected String getClassName() {
		return "com.android.billingclient.api.BillingClient";
	}

	protected static Class<?> getClassForBridge() throws ClassNotFoundException {
		return Class.forName("com.android.billingclient.api.BillingClient");
	}

	@Override
	public void startConnection(BillingClientStateListenerProxy billingClientStateListenerProxy) throws ClassNotFoundException {
		callVoidMethod(startConnectionMethodName, _billingClientInternalInstance, billingClientStateListenerProxy.getProxyInstance());
	}

	public void endConnection() {
		callVoidMethod(endConnectionMethodName, _billingClientInternalInstance);
	}

	@Override
	public BillingResultResponseCode isFeatureSupported(String purchaseType) {
		Object billingResult = callNonVoidMethod(isFeatureSupportedMethodName, _billingClientInternalInstance, purchaseType);
		BillingResultBridge billingResultBridge = new BillingResultBridge(billingResult);
		return billingResultBridge.getResponseCode();
	}

	@Override
	public boolean isReady() {
		return callNonVoidMethod(isReadyMethodName, _billingClientInternalInstance);
	}

	@Override
	public void querySkuDetailsAsync(SkuDetailsParamsBridge params, SkuDetailsResponseListenerProxy skuDetailsResponseListenerProxy) throws ClassNotFoundException {
		callVoidMethod(querySkuDetailsAsyncMethodName, _billingClientInternalInstance, params.getInternalInstance(), skuDetailsResponseListenerProxy.getProxyInstance());
	}

	@Override
	public void queryPurchaseHistoryAsync(String skuType, PurchaseHistoryResponseListenerProxy purchaseHistoryResponseListenerProxy) throws ClassNotFoundException {
		callVoidMethod(queryPurchaseHistoryAsyncMethodName, _billingClientInternalInstance, skuType, purchaseHistoryResponseListenerProxy.getProxyInstance());
	}

	public static Object callNonVoidStaticMethod(String methodName, Object... parameters) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		Method method = getClassForBridge().getMethod(methodName, staticMethods.get(methodName));
		return method.invoke(null, parameters);
	}

}
