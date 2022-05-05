package com.unity3d.services.store.gpbl.bridges.billingclient;

import com.unity3d.services.store.gpbl.proxies.PurchaseUpdatedListenerProxy;

import java.lang.reflect.InvocationTargetException;

public interface IBillingClientBuilderBridge {
	IBillingClientBuilderBridge setListener(PurchaseUpdatedListenerProxy purchaseUpdatedListenerProxy) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException;
	IBillingClientBuilderBridge enablePendingPurchases();
	IBillingClient build() throws ClassNotFoundException;
}
