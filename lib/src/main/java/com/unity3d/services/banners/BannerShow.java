package com.unity3d.services.banners;

import android.os.ConditionVariable;

import com.unity3d.services.ads.properties.AdsProperties;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.CallbackStatus;

import java.lang.reflect.Method;

public class BannerShow {
    private static ConditionVariable _waitShowStatus;

    public static synchronized boolean show(String placementId) throws NoSuchMethodException {
        Method showCallback = BannerShow.class.getMethod("showCallback", CallbackStatus.class);
        _waitShowStatus = new ConditionVariable();
        WebViewApp.getCurrentApp().invokeMethod("webview", "showBanner", showCallback, placementId);
        boolean success = _waitShowStatus.block(AdsProperties.getShowTimeout());
        _waitShowStatus = null;
        return success;
    }

    public static void showCallback(CallbackStatus status) {
        if (_waitShowStatus != null && status.equals(CallbackStatus.OK)) {
            _waitShowStatus.open();
        }
    }
}
