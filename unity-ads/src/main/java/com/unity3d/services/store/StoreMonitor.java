package com.unity3d.services.store;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;
import com.unity3d.services.store.core.StoreException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

@TargetApi(14)
public class StoreMonitor {
	private static Object _billingService;
	private static StoreLifecycleListener _lifecycleListener;

	public static void initialize(String intentName, String intentPackage) {
		Intent intent = new Intent(intentName);
		intent.setPackage(intentPackage);

		ServiceConnection serviceConnection = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				_billingService = StoreBilling.asInterface(ClientProperties.getApplicationContext(), service);
				if(_billingService != null) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.INITIALIZED);
				} else {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.INITIALIZATION_FAILED);
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				_billingService = null;

				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.DISCONNECTED);
			}
		};

		ClientProperties.getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}

	public static boolean isInitialized() {
		return _billingService != null;
	}

	public static int isBillingSupported(String purchaseType) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, StoreException, InvocationTargetException {
		return StoreBilling.isBillingSupported(ClientProperties.getApplicationContext(), _billingService, purchaseType);
	}

	public static JSONObject getPurchases(String purchaseType) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, JSONException, IllegalAccessException, StoreException {
		return StoreBilling.getPurchases(ClientProperties.getApplicationContext(), _billingService, purchaseType);
	}

	public static JSONObject getPurchaseHistory(String purchaseType, int maxPurchases) throws NoSuchMethodException, StoreException, IllegalAccessException, JSONException, InvocationTargetException, ClassNotFoundException {
		return StoreBilling.getPurchaseHistory(ClientProperties.getApplicationContext(), _billingService, purchaseType, maxPurchases);
	}

	public static JSONArray getSkuDetails(String purchaseType, ArrayList<String> skuList) throws NoSuchMethodException, StoreException, IllegalAccessException, JSONException, InvocationTargetException, ClassNotFoundException {
		return StoreBilling.getSkuDetails(ClientProperties.getApplicationContext(), _billingService, purchaseType, skuList);
	}

	public static void startPurchaseTracking(boolean trackAllActivities, ArrayList<String> exceptions, ArrayList<String> purchaseTypes) {
		if(_lifecycleListener != null) {
			stopPurchaseTracking();
		}

		_lifecycleListener = new StoreLifecycleListener(trackAllActivities, exceptions, purchaseTypes);
		ClientProperties.getApplication().registerActivityLifecycleCallbacks(_lifecycleListener);
	}

	public static void stopPurchaseTracking() {
		if(_lifecycleListener != null) {
			ClientProperties.getApplication().unregisterActivityLifecycleCallbacks(_lifecycleListener);
			_lifecycleListener = null;
		}
	}

	public static void sendPurchaseStatusOnResume(String activityName, ArrayList<String> purchaseTypes) {
		if(!isInitialized()) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_RESUME_ERROR, StoreError.NOT_INITIALIZED, activityName, "StoreMonitor not initialized");
			return;
		}

		try {
			JSONObject results = new JSONObject();

			if(purchaseTypes.contains("inapp")) {
				JSONObject inAppStatus = getPurchases("inapp");
				results.put("inapp", inAppStatus);
			}

			if(purchaseTypes.contains("subs")) {
				JSONObject subsStatus = getPurchases("subs");
				results.put("subs", subsStatus);
			}

			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_RESUME, activityName, results);
		} catch (ClassNotFoundException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_RESUME_ERROR, StoreError.CLASS_NOT_FOUND, activityName, e.getMessage());
		} catch (NoSuchMethodException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_RESUME_ERROR, StoreError.NO_SUCH_METHOD, activityName, e.getMessage());
		} catch (InvocationTargetException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_RESUME_ERROR, StoreError.INVOCATION_TARGET, activityName, e.getMessage());
		} catch (JSONException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_RESUME_ERROR, StoreError.JSON_ERROR, activityName, e.getMessage());
		} catch (IllegalAccessException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_RESUME_ERROR, StoreError.ILLEGAL_ACCESS, activityName, e.getMessage());
		} catch (StoreException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_RESUME_ERROR, StoreError.STORE_ERROR, activityName, e.getMessage(), e.getResultCode());
		}
	}

	public static void sendPurchaseStatusOnStop(String activityName, ArrayList<String> purchaseTypes) {
		if(!isInitialized()) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_STOP_ERROR, StoreError.NOT_INITIALIZED, activityName, "StoreMonitor not initialized");
			return;
		}

		try {
			JSONObject results = new JSONObject();

			if(purchaseTypes.contains("inapp")) {
				JSONObject inAppStatus = getPurchases("inapp");
				results.put("inapp", inAppStatus);
			}

			if(purchaseTypes.contains("subs")) {
				JSONObject subsStatus = getPurchases("subs");
				results.put("subs", subsStatus);
			}

			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_STOP, activityName, results);
		} catch (ClassNotFoundException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_STOP_ERROR, StoreError.CLASS_NOT_FOUND, activityName, e.getMessage());
		} catch (NoSuchMethodException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_STOP_ERROR, StoreError.NO_SUCH_METHOD, activityName, e.getMessage());
		} catch (InvocationTargetException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_STOP_ERROR, StoreError.INVOCATION_TARGET, activityName, e.getMessage());
		} catch (JSONException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_STOP_ERROR, StoreError.JSON_ERROR, activityName, e.getMessage());
		} catch (IllegalAccessException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_STOP_ERROR, StoreError.ILLEGAL_ACCESS, activityName, e.getMessage());
		} catch (StoreException e) {
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_STATUS_ON_STOP_ERROR, StoreError.STORE_ERROR, activityName, e.getMessage(), e.getResultCode());
		}
	}
}
