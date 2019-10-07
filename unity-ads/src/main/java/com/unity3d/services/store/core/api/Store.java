package com.unity3d.services.store.core.api;

import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;
import com.unity3d.services.store.StoreError;
import com.unity3d.services.store.StoreEvent;
import com.unity3d.services.store.StoreMonitor;
import com.unity3d.services.store.core.StoreException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public class Store {
	@WebViewExposed
	public static void initialize(String intentName, String intentPackage, WebViewCallback callback) {
		try {
			StoreMonitor.initialize(intentName, intentPackage);
			callback.invoke();
		} catch(Exception e) {
			callback.error(StoreError.UNKNOWN_ERROR, e.getMessage(), e.getClass().getName());
		}
	}

	@WebViewExposed
	public static void startPurchaseTracking(Boolean trackAllActivities, JSONArray exceptions, JSONArray purchaseTypes, WebViewCallback callback) {
		if(!StoreMonitor.isInitialized()) {
			callback.error(StoreError.NOT_INITIALIZED);
			return;
		}

		ArrayList<String> exceptionList = new ArrayList<>();
		ArrayList<String> purchaseTypeList = new ArrayList<>();

		try {
			for (int i = 0; i < exceptions.length(); i++) {
				exceptionList.add(exceptions.getString(i));
			}

			for (int i = 0; i < purchaseTypes.length(); i++) {
				purchaseTypeList.add(purchaseTypes.getString(i));
			}
		} catch(JSONException e) {
			callback.error(StoreError.JSON_ERROR, e.getMessage());
			return;
		}

		StoreMonitor.startPurchaseTracking(trackAllActivities, exceptionList, purchaseTypeList);
		callback.invoke();
	}

	@WebViewExposed
	public static void stopPurchaseTracking(WebViewCallback callback) {
		if(!StoreMonitor.isInitialized()) {
			callback.error(StoreError.NOT_INITIALIZED);
			return;
		}

		StoreMonitor.stopPurchaseTracking();
		callback.invoke();
	}

	@WebViewExposed
	public static void isBillingSupported(final Integer operationId, final String purchaseType, WebViewCallback callback) {
		if(!StoreMonitor.isInitialized()) {
			callback.error(StoreError.NOT_INITIALIZED);
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int result = StoreMonitor.isBillingSupported(purchaseType);
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.BILLING_SUPPORTED_RESULT, operationId, result);
				} catch (InvocationTargetException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.BILLING_SUPPORTED_ERROR, operationId, StoreError.INVOCATION_TARGET, e.getMessage());
				} catch (NoSuchMethodException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.BILLING_SUPPORTED_ERROR, operationId, StoreError.NO_SUCH_METHOD, e.getMessage());
				} catch (IllegalAccessException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.BILLING_SUPPORTED_ERROR, operationId, StoreError.ILLEGAL_ACCESS, e.getMessage());
				} catch (StoreException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.BILLING_SUPPORTED_ERROR, operationId, StoreError.STORE_ERROR, e.getMessage(), e.getResultCode());
				} catch (ClassNotFoundException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.BILLING_SUPPORTED_ERROR, operationId, StoreError.CLASS_NOT_FOUND, e.getMessage());
				} catch(Exception e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.BILLING_SUPPORTED_ERROR, operationId, StoreError.UNKNOWN_ERROR, e.getMessage(), e.getClass().getName());
				}
			}
		}).start();

		callback.invoke();
	}

	@WebViewExposed
	public static void getPurchases(final Integer operationId, final String purchaseType, WebViewCallback callback) {
		if(!StoreMonitor.isInitialized()) {
			callback.error(StoreError.NOT_INITIALIZED);
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject result = StoreMonitor.getPurchases(purchaseType);
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.GETPURCHASES_RESULT, operationId, result);
				} catch(NoSuchMethodException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.GETPURCHASES_ERROR, operationId, StoreError.NO_SUCH_METHOD, e.getMessage());
				} catch(StoreException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.GETPURCHASES_ERROR, operationId, StoreError.STORE_ERROR, e.getMessage(), e.getResultCode());
				} catch(IllegalAccessException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.GETPURCHASES_ERROR, operationId, StoreError.ILLEGAL_ACCESS, e.getMessage());
				} catch(JSONException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.GETPURCHASES_ERROR, operationId, StoreError.JSON_ERROR, e.getMessage());
				} catch(InvocationTargetException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.GETPURCHASES_ERROR, operationId, StoreError.INVOCATION_TARGET, e.getMessage());
				} catch(ClassNotFoundException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.GETPURCHASES_ERROR, operationId, StoreError.CLASS_NOT_FOUND, e.getMessage());
				} catch(Exception e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.GETPURCHASES_ERROR, operationId, StoreError.UNKNOWN_ERROR, e.getMessage(), e.getClass().getName());
				}
			}
		}).start();

		callback.invoke();
	}

	@WebViewExposed
	public static void getPurchaseHistory(final Integer operationId, final String purchaseType, final Integer maxPurchases, WebViewCallback callback) {
		if(!StoreMonitor.isInitialized()) {
			callback.error(StoreError.NOT_INITIALIZED);
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject result = StoreMonitor.getPurchaseHistory(purchaseType, maxPurchases);
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_HISTORY_RESULT, operationId, result);
				} catch(NoSuchMethodException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_HISTORY_ERROR, operationId, StoreError.NO_SUCH_METHOD, e.getMessage());
				} catch(StoreException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_HISTORY_ERROR, operationId, StoreError.STORE_ERROR, e.getMessage(), e.getResultCode());
				} catch(IllegalAccessException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_HISTORY_ERROR, operationId, StoreError.ILLEGAL_ACCESS, e.getMessage());
				} catch(JSONException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_HISTORY_ERROR, operationId, StoreError.JSON_ERROR, e.getMessage());
				} catch(InvocationTargetException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_HISTORY_ERROR, operationId, StoreError.INVOCATION_TARGET, e.getMessage());
				} catch(ClassNotFoundException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_HISTORY_ERROR, operationId, StoreError.CLASS_NOT_FOUND, e.getMessage());
				} catch(Exception e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_HISTORY_ERROR, operationId, StoreError.UNKNOWN_ERROR, e.getMessage(), e.getClass().getName());
				}
			}
		}).start();

		callback.invoke();
	}

	@WebViewExposed
	public static void getSkuDetails(final Integer operationId, final String purchaseType, final JSONArray skuList, WebViewCallback callback) {
		if(!StoreMonitor.isInitialized()) {
			callback.error(StoreError.NOT_INITIALIZED);
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ArrayList<String> skuArray = new ArrayList<>();
					for(int i = 0; i < skuList.length(); i++) {
						skuArray.add(skuList.getString(i));
					}

					JSONArray result = StoreMonitor.getSkuDetails(purchaseType, skuArray);
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.SKU_DETAILS_RESULT, operationId, result);
				} catch(JSONException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.SKU_DETAILS_ERROR, operationId, StoreError.JSON_ERROR, e.getMessage());
				} catch(NoSuchMethodException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.SKU_DETAILS_ERROR, operationId, StoreError.NO_SUCH_METHOD, e.getMessage());
				} catch(IllegalAccessException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.SKU_DETAILS_ERROR, operationId, StoreError.ILLEGAL_ACCESS, e.getMessage());
				} catch(StoreException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.SKU_DETAILS_ERROR, operationId, StoreError.STORE_ERROR, e.getMessage(), e.getResultCode());
				} catch(ClassNotFoundException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.SKU_DETAILS_ERROR, operationId, StoreError.CLASS_NOT_FOUND, e.getMessage());
				} catch(InvocationTargetException e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.SKU_DETAILS_ERROR, operationId, StoreError.INVOCATION_TARGET, e.getMessage());
				} catch(Exception e) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.SKU_DETAILS_ERROR, operationId, StoreError.UNKNOWN_ERROR, e.getMessage(), e.getClass().getName());
				}
			}
		}).start();

		callback.invoke();
	}
}
