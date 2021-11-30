package com.unity3d.services.store;

import android.annotation.TargetApi;

import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;
import com.unity3d.services.store.core.StoreExceptionHandler;
import com.unity3d.services.store.core.StoreLifecycleListener;
import com.unity3d.services.store.gpbl.BillingResultResponseCode;
import com.unity3d.services.store.gpbl.IBillingClientStateListener;
import com.unity3d.services.store.listeners.IPurchaseHistoryResponseListener;
import com.unity3d.services.store.listeners.IPurchaseUpdatedResponseListener;
import com.unity3d.services.store.listeners.ISkuDetailsResponseListener;
import com.unity3d.services.store.gpbl.StoreBilling;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.PurchaseBridge;
import com.unity3d.services.store.gpbl.bridges.PurchaseHistoryRecordBridge;
import com.unity3d.services.store.gpbl.bridges.SkuDetailsBridge;
import com.unity3d.services.store.listeners.PurchasesResponseListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@TargetApi(14)
public class StoreMonitor {
	private static StoreBilling _storeBilling;
	private static AtomicBoolean _isInitialized = new AtomicBoolean(false);
	private static StoreExceptionHandler _storeExceptionHandler;
	private static StoreLifecycleListener _lifecycleListener;

	public static void initialize(StoreExceptionHandler storeExceptionHandler) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InterruptedException, InvocationTargetException {
		if (_isInitialized.get()) {
			// Already initialized
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.INITIALIZATION_REQUEST_RESULT, BillingResultResponseCode.OK.getResponseCode());
			return;
		}

		_storeExceptionHandler = storeExceptionHandler;
		_storeBilling = new StoreBilling(ClientProperties.getApplicationContext(), new IPurchaseUpdatedResponseListener() {
			@Override
			public void onBillingResponse(BillingResultBridge billingResult, List<PurchaseBridge> purchases) {
				if (billingResult.getResponseCode() == BillingResultResponseCode.OK) {
					JSONArray purchasesJson = new JSONArray();
					for (PurchaseBridge purchaseBridge : purchases) {
						purchasesJson.put(purchaseBridge.toJson());
					}
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASES_UPDATED_RESULT, purchasesJson);
				} else {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASES_UPDATED_ERROR, billingResult.getResponseCode());
				}
			}
		});
		_storeBilling.initialize(new IBillingClientStateListener() {
			@Override
			public void onBillingSetupFinished(BillingResultBridge billingResult) {
				if (billingResult.getResponseCode() == BillingResultResponseCode.OK) {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.INITIALIZATION_REQUEST_RESULT, billingResult.getResponseCode());
					_isInitialized.set(true);
				} else {
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.INITIALIZATION_REQUEST_FAILED, billingResult.getResponseCode());
				}
			}

			@Override
			public void onBillingServiceDisconnected() {
				WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.DISCONNECTED_RESULT);
			}
		});
	}

	public static boolean isInitialized() {
		return _isInitialized.get();
	}

	public static int isFeatureSupported(int operationId, String purchaseType) {
		int result = -1;
		try {
			result = _storeBilling.isFeatureSupported(purchaseType);
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.IS_FEATURE_SUPPORTED_REQUEST_RESULT, operationId, result);
		} catch (Exception exception) {
			_storeExceptionHandler.handleStoreException(StoreEvent.IS_FEATURE_SUPPORTED_REQUEST_ERROR, operationId, exception);
		}
		return result;
	}

	public static void getPurchases(final int operationId, String purchaseType) {
		try {
			_storeBilling.getPurchases(purchaseType, new PurchasesResponseListener(operationId, StoreEvent.PURCHASES_REQUEST_RESULT, StoreEvent.PURCHASES_REQUEST_ERROR));
		} catch (Exception exception) {
			_storeExceptionHandler.handleStoreException(StoreEvent.PURCHASES_REQUEST_ERROR, operationId, exception);
		}
	}

	public static void getPurchaseHistory(final int operationId, String purchaseType, int maxPurchases) {
		try {
			_storeBilling.getPurchaseHistory(purchaseType, maxPurchases, new IPurchaseHistoryResponseListener() {
				@Override
				public void onBillingResponse(BillingResultBridge billingResult, List<PurchaseHistoryRecordBridge> purchaseHistoryRecordList) {
					JSONArray jsonArray = new JSONArray();
					for(PurchaseHistoryRecordBridge purchaseHistoryRecordBridge : purchaseHistoryRecordList) {
						jsonArray.put(purchaseHistoryRecordBridge.getOriginalJson());
					}
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.PURCHASE_HISTORY_LIST_REQUEST_RESULT, operationId, jsonArray);
				}
			});
		} catch (Exception exception) {
			_storeExceptionHandler.handleStoreException(StoreEvent.PURCHASE_HISTORY_LIST_REQUEST_ERROR, operationId, exception);
		}
	}

	public static void getSkuDetails(final int operationId, String purchaseType, ArrayList<String> skuList) {
		try {
			_storeBilling.getSkuDetails(purchaseType, skuList, new ISkuDetailsResponseListener() {
				@Override
				public void onBillingResponse(BillingResultBridge billingResult, List<SkuDetailsBridge> skuDetailsList) {
					JSONArray skuDetailsJson = new JSONArray();
					for (SkuDetailsBridge skuDetailsBridge : skuDetailsList) {
						skuDetailsJson.put(skuDetailsBridge.getOriginalJson());
					}
					WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, StoreEvent.SKU_DETAILS_LIST_REQUEST_RESULT, operationId, skuDetailsJson);
				}
			});
		} catch(Exception exception) {
			_storeExceptionHandler.handleStoreException(StoreEvent.SKU_DETAILS_LIST_REQUEST_ERROR, operationId, exception);
		}
	}

	public static void startPurchaseTracking(ArrayList<String> purchaseTypes) {
		if(_lifecycleListener != null) {
			stopPurchaseTracking();
		}

		_lifecycleListener = new StoreLifecycleListener(purchaseTypes, _storeBilling);
		ClientProperties.getApplication().registerActivityLifecycleCallbacks(_lifecycleListener);

	}

	public static void stopPurchaseTracking() {
		if(_lifecycleListener != null) {
			ClientProperties.getApplication().unregisterActivityLifecycleCallbacks(_lifecycleListener);
			_lifecycleListener = null;
		}
	}
}
