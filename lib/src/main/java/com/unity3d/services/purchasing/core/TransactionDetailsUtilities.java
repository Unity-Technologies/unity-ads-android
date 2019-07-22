package com.unity3d.services.purchasing.core;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.monetization.core.utilities.JSONUtilities;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public final class TransactionDetailsUtilities {

    public static final String TRANSACTION_ID = "transactionId";
    public static final String PRODUCT_ID = "productId";
    public static final String PRICE = "price";
    public static final String CURRENCY = "currency";
    public static final String RECEIPT = "receipt";
    public static final String EXTRAS = "extras";

    public static JSONObject getJSONObjectForTransactionDetails(TransactionDetails details) {
        JSONObject object = new JSONObject();
        try {
            object.put(TRANSACTION_ID, details.getTransactionId());
            object.put(PRODUCT_ID, details.getProductId());
            object.put(PRICE, details.getPrice());
            object.put(CURRENCY, details.getCurrency());
            object.put(RECEIPT, details.getReceipt());
            object.put(EXTRAS, JSONUtilities.mapToJsonObject(details.getExtras()));
        } catch (Exception e) {
            DeviceLog.error("Could not generate JSON for transaction details: %s", e.getMessage());
        }
        return object;
    }

    public static Map<String, Object> getEventDataForTransactionDetails(TransactionDetails details) {
        Map<String, Object> data = new HashMap<>();
        data.put(TRANSACTION_ID, details.getTransactionId());
        data.put(PRODUCT_ID, details.getProductId());
        data.put(PRICE, details.getPrice());
        data.put(CURRENCY, details.getCurrency());
        data.put(RECEIPT, details.getReceipt());
        data.put(EXTRAS, details.getExtras());
        return data;
    }
}
