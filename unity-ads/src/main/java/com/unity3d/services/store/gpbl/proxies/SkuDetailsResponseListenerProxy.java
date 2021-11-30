package com.unity3d.services.store.gpbl.proxies;

import com.unity3d.services.core.reflection.GenericListenerProxy;
import com.unity3d.services.store.listeners.ISkuDetailsResponseListener;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.SkuDetailsBridge;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SkuDetailsResponseListenerProxy extends GenericListenerProxy {
	private static final String onSkuDetailsResponseMethodName = "onSkuDetailsResponse";

	private ISkuDetailsResponseListener _skuDetailsResponseListener;

	public SkuDetailsResponseListenerProxy(ISkuDetailsResponseListener skuDetailsResponseListener) {
		_skuDetailsResponseListener = skuDetailsResponseListener;
	}

	@Override
	public Class<?> getProxyClass() throws ClassNotFoundException {
		return getProxyListenerClass();
	}

	public static Class<?> getProxyListenerClass() throws ClassNotFoundException {
		return Class.forName("com.android.billingclient.api.SkuDetailsResponseListener");
	}

	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
	{
		Object result = null;
		if (m.getName().equals(onSkuDetailsResponseMethodName)) {
			onSkuDetailsResponse(args[0], (List<Object>) args[1]);
		} else {
			result = super.invoke(proxy, m, args);
		}
		return result;
	}

	/**
	 * Wraps onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList)
	 * from the reflected billing library.
	 * @param billingResult Billing result from the operation.
	 * @param skuDetailsList List of SkuDetails objects received from the query.
	 */
	public void onSkuDetailsResponse(Object billingResult, List<Object> skuDetailsList) {
		List<SkuDetailsBridge> skuDetailsBridges = new ArrayList<>();
		for (Object skuDetails : skuDetailsList) {
			skuDetailsBridges.add(new SkuDetailsBridge(skuDetails));
		}
		if (_skuDetailsResponseListener != null) {
			_skuDetailsResponseListener.onBillingResponse(new BillingResultBridge(billingResult), skuDetailsBridges);
		}
	}
}