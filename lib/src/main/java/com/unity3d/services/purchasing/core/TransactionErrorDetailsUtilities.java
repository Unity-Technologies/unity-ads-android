package com.unity3d.services.purchasing.core;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.monetization.core.utilities.JSONUtilities;

import org.json.JSONObject;

public final class TransactionErrorDetailsUtilities {

    public static final String TRANSACTION_ERROR = "transactionError";
    public static final String EXCEPTION_MESSAGE = "exceptionMessage";
    public static final String STORE = "store";
    public static final String STORE_SPECIFIC_ERROR_CODE = "storeSpecificErrorCode";
    public static final String EXTRAS = "extras";

    public static JSONObject getJSONObjectForTransactionErrorDetails(TransactionErrorDetails details) {
        JSONObject object = new JSONObject();
        try {
            object.put(TRANSACTION_ERROR, details.getTransactionError().toString());
            object.put(EXCEPTION_MESSAGE, details.getExceptionMessage());
            object.put(STORE, details.getStore().toString());
            object.put(STORE_SPECIFIC_ERROR_CODE, details.getStoreSpecificErrorCode());
            object.put(EXTRAS, JSONUtilities.mapToJsonObject(details.getExtras()));
        } catch (Exception e) {
            DeviceLog.error("Could not generate JSON for Transaction Error Details: %s", e.getMessage());
        }
        return object;
    }

}
