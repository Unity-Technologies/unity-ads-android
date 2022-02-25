package com.unity3d.services.store.listeners;

import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;
import com.unity3d.services.store.StoreEvent;
import com.unity3d.services.store.gpbl.BillingResultResponseCode;
import com.unity3d.services.store.gpbl.bridges.BillingResultBridge;
import com.unity3d.services.store.gpbl.bridges.PurchaseBridge;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class PurchasesResponseListener implements IPurchasesResponseListener {
	private final Integer _operationId;
	private final StoreEvent _successEvent;
	private final StoreEvent _errorEvent;

	public PurchasesResponseListener(StoreEvent successEvent, StoreEvent errorEvent) {
		this(null, successEvent, errorEvent);
	}

	public PurchasesResponseListener(Integer operationId, StoreEvent successEvent, StoreEvent errorEvent) {
		_operationId = operationId;
		_successEvent = successEvent;
		_errorEvent = errorEvent;
	}

	@Override
	public void onBillingResponse(BillingResultBridge billingResult, List<PurchaseBridge> purchases) {
		ArrayList<Object> params = new ArrayList<>();
		if (_operationId != null) {
			// We provide operation ID only when the request would have come from the WebView.
			params.add(_operationId);
		}
		if (billingResult.getResponseCode() == BillingResultResponseCode.OK) {
			JSONArray purchasesJson = new JSONArray();
			if (purchases != null) {
				for (PurchaseBridge purchaseBridge : purchases) {
					purchasesJson.put(purchaseBridge.toJson());
				}
			}
			params.add(purchasesJson);
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, _successEvent, params.toArray());
		} else {
			params.add(billingResult.getResponseCode());
			WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.STORE, _errorEvent, params.toArray());
		}

	}
}
