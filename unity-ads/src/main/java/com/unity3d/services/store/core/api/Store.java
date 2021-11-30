package com.unity3d.services.store.core.api;

import com.unity3d.services.ads.gmascar.handlers.WebViewErrorHandler;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;
import com.unity3d.services.store.StoreError;
import com.unity3d.services.store.StoreEvent;
import com.unity3d.services.store.StoreMonitor;
import com.unity3d.services.store.core.StoreExceptionHandler;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class Store {
	private static final StoreExceptionHandler storeExceptionHandler = new StoreExceptionHandler(new WebViewErrorHandler());

	@WebViewExposed
	public static void initialize(WebViewCallback callback) {
		try {
			StoreMonitor.initialize(storeExceptionHandler);
			callback.invoke();
		} catch(Exception e) {
			callback.error(StoreError.UNKNOWN_ERROR, e.getMessage(), e.getClass().getName());
		}
	}

	@WebViewExposed
	public static void startPurchaseTracking(JSONArray purchaseTypes, WebViewCallback callback) {
		if(!StoreMonitor.isInitialized()) {
			callback.error(StoreError.NOT_INITIALIZED);
			return;
		}

		ArrayList<String> purchaseTypeList = new ArrayList<>();

		try {
			for (int i = 0; i < purchaseTypes.length(); i++) {
				purchaseTypeList.add(purchaseTypes.getString(i));
			}
		} catch(JSONException e) {
			callback.error(StoreError.JSON_ERROR, e.getMessage());
			return;
		}

		StoreMonitor.startPurchaseTracking(purchaseTypeList);
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
	public static void isFeatureSupported(final Integer operationId, final String purchaseType, WebViewCallback callback) {
		if(!StoreMonitor.isInitialized()) {
			callback.error(StoreError.NOT_INITIALIZED);
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				StoreMonitor.isFeatureSupported(operationId, purchaseType);
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
		StoreMonitor.getPurchases(operationId, purchaseType);
		callback.invoke();
	}

	@WebViewExposed
	public static void getPurchaseHistory(final Integer operationId, final String purchaseType, final Integer maxPurchases, WebViewCallback callback) {
		if(!StoreMonitor.isInitialized()) {
			callback.error(StoreError.NOT_INITIALIZED);
			return;
		}

		StoreMonitor.getPurchaseHistory(operationId, purchaseType, maxPurchases);
		callback.invoke();
	}

	@WebViewExposed
	public static void getSkuDetails(final Integer operationId, final String purchaseType, final JSONArray skuList, WebViewCallback callback) {
		if(!StoreMonitor.isInitialized()) {
			callback.error(StoreError.NOT_INITIALIZED);
			return;
		}

		try {
			ArrayList<String> skuArray = new ArrayList<>();
			for(int i = 0; i < skuList.length(); i++) {
				skuArray.add(skuList.getString(i));
			}
			StoreMonitor.getSkuDetails(operationId, purchaseType, skuArray);
		} catch(JSONException exception) {
			storeExceptionHandler.handleStoreException(StoreEvent.SKU_DETAILS_LIST_REQUEST_ERROR, operationId, exception);
		}

		callback.invoke();
	}
}
