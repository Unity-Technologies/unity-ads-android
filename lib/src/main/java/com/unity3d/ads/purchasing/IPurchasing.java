package com.unity3d.ads.purchasing;

public interface IPurchasing {
    /**
     * Called when we initialize the PurchasingAPI in the webview
     *
     */
    void onGetPurchasingVersion();

    /**
     * Called whenever the IAP catalog refreshes in the Promo Parser
     *
     */
    void onGetProductCatalog();

    /**
     * Called when an in-app purchase is initiated from an ad.
     *
     * @param eventString The string provided via the ad.
     */
    void onPurchasingCommand(String eventString);

    /**
     * Called to trigger intialization of purchasing APIs
     *
     */
    void onInitializePurchasing();
}
