package com.unity3d.services.banners;

import android.app.Activity;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.ads.placement.Placement;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.ads.properties.AdsProperties;


/**
 * Created by andrewkonecny on 4/26/18.
 */

public final class UnityBanners {

    public static void loadBanner(Activity activity) {
        loadBanner(activity, Placement.getDefaultBannerPlacement());
    }

    public static void loadBanner(final Activity activity, final String placementId) {
        DeviceLog.entered();
        if (!UnityAds.isSupported()) {
            sendError("Unity Ads is not supported on this device.");
        }
        if (!UnityAds.isInitialized()) {
            sendError("UnityAds is not initialized.");
            return;
        }
        if (!UnityAds.isReady(placementId)) {
            sendError("Banner placement " + placementId + " is not ready");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ClientProperties.setActivity(activity);
                    if (!BannerShow.show(placementId)) {
                        sendError("Could not show banner in time");
                    }
                } catch (Exception e) {
                    sendError(e.getMessage());
                }
            }
        }).start();
    }

    public static void destroy() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!BannerHide.hide()) {
                        sendError(("Could not hide banner in time"));
                    }
                } catch (Exception e) {
                    sendError(e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Change listener for IUnityAdsListener callbacks
     *
     * @param listener New listener for IUnityAdsListener callbacks
     */
    public static void setBannerListener(IUnityBannerListener listener) {
        AdsProperties.setBannerListener(listener);
    }

    /**
     * Get current listener for IUnityAdsListener callbacks
     *
     * @return Current listener for IUnityAdsListener callbacks
     */
    public static IUnityBannerListener getBannerListener() {
        return AdsProperties.getBannerListener();
    }
    
    private static void sendError(final String message) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IUnityBannerListener listener = getBannerListener();
                if (listener != null) {
                    listener.onUnityBannerError(message);
                }
            }
        });
    }
}

