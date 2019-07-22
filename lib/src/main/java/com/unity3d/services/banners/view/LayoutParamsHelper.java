package com.unity3d.services.banners.view;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

class LayoutParamsHelper {
    static ViewGroup.LayoutParams updateLayoutParamsForPosition(ViewGroup.LayoutParams params, BannerPosition position) {
        if (params instanceof FrameLayout.LayoutParams) {
            return updateFrameLayoutParamsForPosition((FrameLayout.LayoutParams)params, position);
        } else if (params instanceof RelativeLayout.LayoutParams) {
            return updateRelativeLayoutParamsForPosition((RelativeLayout.LayoutParams)params, position);
        } else {
            return params;
        }
    }

    private static ViewGroup.LayoutParams updateRelativeLayoutParamsForPosition(RelativeLayout.LayoutParams params, BannerPosition position) {
        return position.addLayoutRules(params);
    }

    private static ViewGroup.LayoutParams updateFrameLayoutParamsForPosition(FrameLayout.LayoutParams params, BannerPosition position) {
        params.gravity = position.getGravity();
        return params;
    }
}
