package com.unity3d.services.purchasing.core;

import java.util.List;

public interface IRetrieveProductsListener {
    void onProductsRetrieved(List<Product> availableProducts);
}
