package com.unity3d.services.store;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.ArrayList;

@TargetApi(14)
public class StoreLifecycleListener implements Application.ActivityLifecycleCallbacks {
	private boolean _trackAllActivities;
	private ArrayList<String> _exceptions;
	private ArrayList<String> _purchaseTypes;

	public StoreLifecycleListener(boolean trackAllActivities, ArrayList<String> exceptions, ArrayList<String> purchaseTypes) {
		_trackAllActivities = trackAllActivities;
		_exceptions = exceptions;
		_purchaseTypes = purchaseTypes;

	}

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

	}

	@Override
	public void onActivityStarted(Activity activity) {

	}

	@Override
	public void onActivityResumed(Activity activity) {
		boolean isException = false;

		if(_exceptions != null) {
			if(_exceptions.contains(activity.getLocalClassName())) {
				isException = true;
			}
		}

		if((_trackAllActivities && !isException) || (!_trackAllActivities && isException)) {
			if(_purchaseTypes != null) {
				StoreMonitor.sendPurchaseStatusOnResume(activity.getLocalClassName(), _purchaseTypes);
			}
		}
	}

	@Override
	public void onActivityPaused(Activity activity) {

	}

	@Override
	public void onActivityStopped(Activity activity) {
		boolean isException = false;

		if(_exceptions != null) {
			if(_exceptions.contains(activity.getLocalClassName())) {
				isException = true;
			}
		}

		if((_trackAllActivities && !isException) || (!_trackAllActivities && isException)) {
			if(_purchaseTypes != null) {
				StoreMonitor.sendPurchaseStatusOnStop(activity.getLocalClassName(), _purchaseTypes);
			}
		}
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

	}

	@Override
	public void onActivityDestroyed(Activity activity) {

	}
}
