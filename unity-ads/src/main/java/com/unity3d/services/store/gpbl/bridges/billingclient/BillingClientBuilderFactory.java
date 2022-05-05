package com.unity3d.services.store.gpbl.bridges.billingclient;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;

public class BillingClientBuilderFactory {
	public static IBillingClientBuilderBridge getBillingClientBuilder(Context context) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (com.unity3d.services.store.gpbl.bridges.billingclient.v4.BillingClientBridge.isAvailable()) {
			return com.unity3d.services.store.gpbl.bridges.billingclient.v4.BillingClientBridge.newBuilder(context);
		} else {
			// Fallback to V3 implementation
			return com.unity3d.services.store.gpbl.bridges.billingclient.v3.BillingClientBridge.newBuilder(context);
		}
	}
}
