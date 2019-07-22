package com.unity3d.services.purchasing.core.api;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.monetization.core.utilities.JSONUtilities;
import com.unity3d.services.purchasing.core.IPurchasingAdapter;
import com.unity3d.services.purchasing.core.IRetrieveProductsListener;
import com.unity3d.services.purchasing.core.ITransactionListener;
import com.unity3d.services.purchasing.core.Product;
import com.unity3d.services.purchasing.core.PurchasingError;
import com.unity3d.services.purchasing.core.PurchasingEvent;
import com.unity3d.services.purchasing.core.TransactionDetails;
import com.unity3d.services.purchasing.core.TransactionDetailsUtilities;
import com.unity3d.services.purchasing.core.TransactionError;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;
import com.unity3d.services.purchasing.core.TransactionErrorDetails;
import com.unity3d.services.purchasing.core.TransactionErrorDetailsUtilities;
import com.unity3d.services.purchasing.core.properties.ClientProperties;
import com.unity3d.services.purchasing.core.webview.WebViewEventCategory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CustomPurchasing {
    private static IRetrieveProductsListener retrieveProductsListener = new IRetrieveProductsListener() {
        @Override
        public void onProductsRetrieved(List<Product> availableProducts) {
            WebViewApp app = WebViewApp.getCurrentApp();
            if (app != null) {
                app.sendEvent(WebViewEventCategory.CUSTOM_PURCHASING, PurchasingEvent.PRODUCTS_RETRIEVED, getJSONArrayFromProductList(availableProducts));
            }
        }
    };

    private static ITransactionListener transactionListener = new ITransactionListener() {
        @Override
        public void onTransactionComplete(TransactionDetails details) {
            WebViewApp app = WebViewApp.getCurrentApp();
            if (app != null) {
                app.sendEvent(WebViewEventCategory.CUSTOM_PURCHASING, PurchasingEvent.TRANSACTION_COMPLETE, TransactionDetailsUtilities.getJSONObjectForTransactionDetails(details));
            }
        }

        @Override
        public void onTransactionError(TransactionErrorDetails details) {
            WebViewApp app = WebViewApp.getCurrentApp();
            if (app != null) {
                app.sendEvent(WebViewEventCategory.CUSTOM_PURCHASING, PurchasingEvent.TRANSACTION_ERROR, TransactionErrorDetailsUtilities.getJSONObjectForTransactionErrorDetails(details));
            }
        }
    };

    static JSONArray getJSONArrayFromProductList(List<Product> products) {
        JSONArray array = new JSONArray();
        for (Product product : products) {
            try {
                array.put(getJSONObjectForProduct(product));
            } catch (JSONException e) {
                DeviceLog.error("Could not generate JSON for product: %s", e.getMessage());
            }
        }
        return array;
    }

    private static JSONObject getJSONObjectForProduct(Product product) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("productId", product.getProductId());
        object.put("localizedPriceString", product.getLocalizedPriceString());
        object.put("localizedTitle", product.getLocalizedTitle());
        object.put("isoCurrencyCode", product.getIsoCurrencyCode());
        object.put("localizedPrice", product.getLocalizedPrice());
        object.put("localizedDescription", product.getLocalizedDescription());
        object.put("productType", product.getProductType());
        return object;
    }

    @WebViewExposed
    public static void available(WebViewCallback callback) {
        IPurchasingAdapter adapter = ClientProperties.getAdapter();
        callback.invoke(adapter != null);
    }

    @WebViewExposed
    public static void refreshCatalog(WebViewCallback callback) {
        final IPurchasingAdapter adapter = ClientProperties.getAdapter();
        if (adapter != null) {
            try {
                Utilities.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.retrieveProducts(retrieveProductsListener);
                    }
                });
                callback.invoke();
            } catch (Exception e) {
                callback.error(PurchasingError.RETRIEVE_PRODUCTS_ERROR, e);
            }
        } else {
            callback.error(PurchasingError.PURCHASING_ADAPTER_NULL);
        }
    }

    @WebViewExposed
    public static void purchaseItem(final String productID, final JSONObject extras, WebViewCallback callback) {
        final IPurchasingAdapter adapter = ClientProperties.getAdapter();
        if (adapter != null) {
            Utilities.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.onPurchase(productID, transactionListener, JSONUtilities.jsonObjectToMap(extras));
                }
            });
            callback.invoke();
        } else {
            callback.error(PurchasingError.PURCHASING_ADAPTER_NULL);
        }
    }
}
