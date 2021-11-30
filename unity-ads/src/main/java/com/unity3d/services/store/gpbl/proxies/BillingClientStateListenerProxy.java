package com.unity3d.services.store.gpbl.proxies;

import com.unity3d.services.core.reflection.GenericListenerProxy;
import com.unity3d.services.store.gpbl.IBillingClientStateListener;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;

import java.lang.reflect.Method;

public class BillingClientStateListenerProxy extends GenericListenerProxy {
	private static String onBillingServiceDisconnectedMethodName = "onBillingServiceDisconnected";
	private static String onBillingSetupFinishedMethodName = "onBillingSetupFinished";

	private IBillingClientStateListener _billingClientStateListener;

	public BillingClientStateListenerProxy(IBillingClientStateListener billingClientStateListener) {
		_billingClientStateListener = billingClientStateListener;
	}

	@Override
	public Class<?> getProxyClass() throws ClassNotFoundException {
		return getProxyListenerClass();
	}

	public static Class<?> getProxyListenerClass() throws ClassNotFoundException {
		return Class.forName("com.android.billingclient.api.BillingClientStateListener");
	}

	@Override
	public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
		Object result = null;
		if (m.getName().equals(onBillingSetupFinishedMethodName)) {
			onBillingSetupFinished(args[0]);
		} else if (m.getName().equals(onBillingServiceDisconnectedMethodName)) {
			onBillingServiceDisconnected();
		} else {
			result = super.invoke(proxy, m, args);
		}
		return result;
	}

	private void onBillingSetupFinished(Object billingResult) {
		if (_billingClientStateListener != null) {
			_billingClientStateListener.onBillingSetupFinished(new BillingResultBridge(billingResult));
		}
	}

	private void onBillingServiceDisconnected() {
		if (_billingClientStateListener != null) {
			_billingClientStateListener.onBillingServiceDisconnected();
		}
	}
}
