package com.unity3d.ads.metadata;

import android.content.Context;

import com.unity3d.ads.device.Storage;
import com.unity3d.ads.device.StorageEvent;
import com.unity3d.ads.device.StorageManager;
import com.unity3d.ads.log.DeviceLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InAppPurchaseMetaData extends MetaData {
	public static final String KEY_PRODUCT_ID = "productId";
	public static final String KEY_PRICE = "price";
	public static final String KEY_CURRENCY = "currency";
	public static final String KEY_RECEIPT_PURCHASE_DATA = "receiptPurchaseData";
	public static final String KEY_SIGNATURE = "signature";

	public static final String IAP_KEY = "iap";

	public InAppPurchaseMetaData (Context context) {
		super(context);
	}

	public void setProductId (String productId) {
		set(KEY_PRODUCT_ID, productId);
	}

	public void setPrice (Double price) {
		set(KEY_PRICE, price);
	}

	public void setCurrency (String currency) {
		set(KEY_CURRENCY, currency);
	}

	public void setReceiptPurchaseData (String receiptPurchaseData) {
		set(KEY_RECEIPT_PURCHASE_DATA, receiptPurchaseData);
	}

	public void setSignature (String signature) {
		set(KEY_SIGNATURE, signature);
	}

	@Override
	public synchronized boolean set (String key, Object value) {
		return setRaw(key, value);
	}

	@Override
	public void commit () {
		if (StorageManager.init(_context)) {
			Storage storage = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

			if (getData() != null && storage != null) {
				Object purchaseObject = storage.get(IAP_KEY + ".purchases");
				JSONArray purchases = null;

				if (purchaseObject != null) {
					try {
						purchases = (JSONArray)purchaseObject;
					}
					catch (Exception e) {
						DeviceLog.error("Invalid object type for purchases");
					}
				}

				if (purchases == null) {
					purchases = new JSONArray();
				}

				JSONObject purchase = getData();

				try {
					purchase.put("ts", System.currentTimeMillis());
				}
				catch (JSONException e) {
					DeviceLog.error("Error constructing purchase object");
					return;
				}

				purchases.put(purchase);
				storage.set(IAP_KEY + ".purchases", purchases);
				storage.writeStorage();
				storage.sendEvent(StorageEvent.SET, storage.get(IAP_KEY + ".purchases"));
			}
		}
		else {
			DeviceLog.error("Unity Ads could not commit metadata due to storage error or the data is null");
		}
	}

}
