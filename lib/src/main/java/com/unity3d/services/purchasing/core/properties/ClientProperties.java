package com.unity3d.services.purchasing.core.properties;

import com.unity3d.services.purchasing.core.IPurchasingAdapter;

public class ClientProperties {
    private static IPurchasingAdapter adapter;

    public static void setAdapter(IPurchasingAdapter adapter) {
        ClientProperties.adapter = adapter;
    }

    public static IPurchasingAdapter getAdapter() {
        return adapter;
    }
}
