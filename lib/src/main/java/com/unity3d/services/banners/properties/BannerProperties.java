package com.unity3d.services.banners.properties;

import android.view.View;

public class BannerProperties {
    private static View bannerParent;

    public static View getBannerParent() {
        return bannerParent;
    }

    public static void setBannerParent(View bannerParent) {
        BannerProperties.bannerParent = bannerParent;
    }
}
