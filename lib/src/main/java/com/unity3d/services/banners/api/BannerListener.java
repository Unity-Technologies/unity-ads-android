package com.unity3d.services.banners.api;

import com.unity3d.services.banners.UnityBanners;
import com.unity3d.services.banners.properties.BannerProperties;
import com.unity3d.services.banners.view.BannerView;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class BannerListener {

    @WebViewExposed
    public static void sendShowEvent(final String placementId, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (UnityBanners.getBannerListener() != null) {
                    UnityBanners.getBannerListener().onUnityBannerShow(placementId);
                }
            }
        });

        callback.invoke();
    }

    @WebViewExposed
    public static void sendClickEvent(final String placementId, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (UnityBanners.getBannerListener() != null) {
                    UnityBanners.getBannerListener().onUnityBannerClick(placementId);
                }
            }
        });

        callback.invoke();
    }

    @WebViewExposed
    public static void sendHideEvent(final String placementId, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (UnityBanners.getBannerListener() != null) {
                    UnityBanners.getBannerListener().onUnityBannerHide(placementId);
                }
            }
        });

        callback.invoke();
    }

    @WebViewExposed
    public static void sendErrorEvent(final String message, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (UnityBanners.getBannerListener() != null) {
                    UnityBanners.getBannerListener().onUnityBannerError(message);
                }
            }
        });

        callback.invoke();
    }

    @WebViewExposed
    public static void sendLoadEvent(final String placementId, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (UnityBanners.getBannerListener() != null) {
                    UnityBanners.getBannerListener().onUnityBannerLoaded(placementId, BannerView.getInstance());
                }
            }
        });

        callback.invoke();
    }

    @WebViewExposed
    public static void sendUnloadEvent(final String placementId, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (UnityBanners.getBannerListener() != null) {
                    UnityBanners.getBannerListener().onUnityBannerUnloaded(placementId);
                }
            }
        });

        callback.invoke();
    }
}
