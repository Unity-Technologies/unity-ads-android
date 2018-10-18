package com.unity3d.services.purchasing;

import com.unity3d.services.purchasing.core.IPurchasingAdapter;
import com.unity3d.services.purchasing.core.properties.ClientProperties;

public class UnityPurchasing {
    public static void setAdapter(IPurchasingAdapter adapter) {
        ClientProperties.setAdapter(adapter);
    }

    public static IPurchasingAdapter getAdapter() {
        return ClientProperties.getAdapter();
    }
}
