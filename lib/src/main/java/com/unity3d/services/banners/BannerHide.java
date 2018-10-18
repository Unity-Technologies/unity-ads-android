package com.unity3d.services.banners;

import android.os.ConditionVariable;

import com.unity3d.services.ads.properties.AdsProperties;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.CallbackStatus;

import java.lang.reflect.Method;

public class BannerHide {
    private static ConditionVariable _waitHideStatus;

    public static synchronized boolean hide() throws NoSuchMethodException {
        Method showCallback = BannerHide.class.getMethod("showCallback", CallbackStatus.class);
        _waitHideStatus = new ConditionVariable();
        WebViewApp.getCurrentApp().invokeMethod("webview", "hideBanner", showCallback);
        boolean success = _waitHideStatus.block(AdsProperties.getShowTimeout());
        _waitHideStatus = null;
        return success;
    }

    public static void showCallback(CallbackStatus status) {
        if (_waitHideStatus != null && status.equals(CallbackStatus.OK)) {
            _waitHideStatus.open();
        }
    }
}
