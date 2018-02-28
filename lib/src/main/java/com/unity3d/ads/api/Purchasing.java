package com.unity3d.ads.api;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.purchasing.IPurchasing;
import com.unity3d.ads.purchasing.PurchasingError;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

public class Purchasing {

    public static IPurchasing purchaseInterface = null;

    public static void setPurchasingInterface(IPurchasing purchasing){
        purchaseInterface = purchasing;
    }

    @WebViewExposed
    public static void initiatePurchasingCommand(final String eventString, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (purchaseInterface != null) {
                    purchaseInterface.onPurchasingCommand(eventString);
                }
            }
        });

        if (purchaseInterface != null) {
            callback.invoke();
        }
        else {
            callback.error(PurchasingError.PURCHASE_INTERFACE_NULL);
        }
    }

    @WebViewExposed
    public static void getPromoVersion(WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (purchaseInterface != null) {
                    purchaseInterface.onGetPurchasingVersion();
                }
            }
        });

        if (purchaseInterface != null) {
            callback.invoke();
        }
        else {
            callback.error(PurchasingError.PURCHASE_INTERFACE_NULL);
        }
    }

    @WebViewExposed
    public static void getPromoCatalog(WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (purchaseInterface != null) {
                    purchaseInterface.onGetProductCatalog();
                }
            }
        });

        if (purchaseInterface != null) {
            callback.invoke();
        }
        else {
            callback.error(PurchasingError.PURCHASE_INTERFACE_NULL);
        }
    }

    @WebViewExposed
    public static void initializePurchasing(WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (purchaseInterface != null) {
                    purchaseInterface.onInitializePurchasing();
                }
            }
        });

        if (purchaseInterface != null) {
            callback.invoke();
        }
        else {
            callback.error(PurchasingError.PURCHASE_INTERFACE_NULL);
        }
    }
}
