package com.unity3d.services.banners.view;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.unity3d.services.banners.UnityBanners;

public enum BannerPosition {
    TOP_LEFT(new int[] {RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.ALIGN_PARENT_LEFT}, Gravity.TOP | Gravity.LEFT),
    TOP_CENTER(new int[] {RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.CENTER_HORIZONTAL}, Gravity.TOP | Gravity.CENTER_HORIZONTAL),
    TOP_RIGHT(new int[] {RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.ALIGN_PARENT_RIGHT}, Gravity.TOP | Gravity.RIGHT),
    BOTTOM_LEFT(new int[] {RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.ALIGN_PARENT_LEFT}, Gravity.BOTTOM | Gravity.LEFT),
    BOTTOM_CENTER(new int[] {RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.CENTER_HORIZONTAL}, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL),
    BOTTOM_RIGHT(new int[] {RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.ALIGN_PARENT_RIGHT}, Gravity.BOTTOM | Gravity.RIGHT),
    CENTER(new int[] {RelativeLayout.CENTER_IN_PARENT}, Gravity.CENTER),
    NONE(new int[] {}, Gravity.NO_GRAVITY);

    private final int[] _rules;
    private int _gravity;

    BannerPosition(int[] rules, int gravity) {
        _rules = rules;
        _gravity = gravity;
    }

    public static BannerPosition fromString(String in) {
        if (in == null || in.equals("none")) {
            return NONE;
        } else if (in.equals("topleft")) {
            return TOP_LEFT;
        } else if (in.equals("topright")) {
            return TOP_RIGHT;
        } else if (in.equals("topcenter")) {
            return TOP_CENTER;
        } else if (in.equals("bottomleft")) {
            return BOTTOM_LEFT;
        } else if (in.equals("bottomright")) {
            return BOTTOM_RIGHT;
        } else if (in.equals("bottomcenter")) {
            return BOTTOM_CENTER;
        } else if (in.equals("center")) {
            return CENTER;
        }
        return NONE;
    }

    public ViewGroup.LayoutParams addLayoutRules(RelativeLayout.LayoutParams params) {
        for (int rule : this._rules) {
            params.addRule(rule);
        }
        return params;
    }

    public int getGravity() {
        return _gravity;
    }

}
