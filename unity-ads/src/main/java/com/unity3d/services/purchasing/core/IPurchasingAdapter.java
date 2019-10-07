package com.unity3d.services.purchasing.core;

import java.util.Map;

public interface IPurchasingAdapter {
    void retrieveProducts(IRetrieveProductsListener listener);
    void onPurchase(String productID, ITransactionListener listener, Map<String, Object> extras);
}
