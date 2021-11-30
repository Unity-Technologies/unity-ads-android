package com.unity3d.services.store.core;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.store.StoreEvent;
import com.unity3d.services.store.core.api.Store;
import com.unity3d.services.store.gpbl.StoreBilling;
import com.unity3d.services.store.listeners.PurchasesResponseListener;

import java.util.ArrayList;

public class StoreLifecycleListener implements Application.ActivityLifecycleCallbacks {
	private final ArrayList<String> _purchaseTypes;
	private final StoreBilling _storeBilling;
	private final PurchasesResponseListener _purchaseResponseListener;

	public StoreLifecycleListener(ArrayList<String> purchaseTypes, StoreBilling storeBilling) {
		_purchaseTypes = purchaseTypes;
		_storeBilling = storeBilling;
		_purchaseResponseListener = new PurchasesResponseListener(StoreEvent.PURCHASES_ON_RESUME_RESULT, StoreEvent.PURCHASES_ON_RESUME_ERROR);
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

	}

	@Override
	public void onActivityStarted(Activity activity) {

	}

	@Override
	public void onActivityResumed(Activity activity) {
		try {
			for (String purchaseType : _purchaseTypes) {
				_storeBilling.getPurchases(purchaseType, _purchaseResponseListener);
			}
		} catch (ClassNotFoundException exception) {
			DeviceLog.warning("Couldn't fetch purchases onActivityResumed. " + exception.getMessage());
		}

	}

	@Override
	public void onActivityPaused(Activity activity) {

	}

	@Override
	public void onActivityStopped(Activity activity) {

	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

	}

	@Override
	public void onActivityDestroyed(Activity activity) {

	}
	
}