package com.unity3d.ads.metadata;

import android.content.Context;

import com.unity3d.ads.device.Storage;
import com.unity3d.ads.device.StorageEvent;
import com.unity3d.ads.device.StorageManager;
import com.unity3d.ads.log.DeviceLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class InAppPurchaseMetaData extends MetaData {
	public static final String KEY_PRODUCT_ID = "productId";
	public static final String KEY_PRICE = "price";
	public static final String KEY_CURRENCY = "currency";
	public static final String KEY_RECEIPT_PURCHASE_DATA = "receiptPurchaseData";
	public static final String KEY_SIGNATURE = "signature";

	public InAppPurchaseMetaData (Context context) {
		super(context);
		setCategory("iap");
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
	public void set (String key, Object value) {
		if (_metaData == null) {
			_metaData = new HashMap<>();
		}

		_metaData.put(key, value);
	}

	@Override
	public void commit () {
		if (StorageManager.init(_context)) {
			Storage storage = StorageManager.getStorage(StorageManager.StorageType.PUBLIC);

			if (_metaData != null && storage != null) {
				Object purchaseObject = storage.get(getCategory() + ".purchases");
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

				JSONObject purchase = new JSONObject();

				try {
					for (String key : _metaData.keySet()) {
						purchase.put(key, _metaData.get(key));
					}

					purchase.put("ts", System.currentTimeMillis());
				}
				catch (JSONException e) {
					DeviceLog.error("Error constructing purchase object");
					return;
				}

				purchases.put(purchase);
				storage.set(getCategory() + ".purchases", purchases);
				storage.writeStorage();
				storage.sendEvent(StorageEvent.SET, _metaData);
			}
		}
		else {
			DeviceLog.error("Unity Ads could not commit metadata due to storage error or the data is null");
		}
	}

}
