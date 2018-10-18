package com.unity3d.services.banners.api;

import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.unity3d.services.banners.view.BannerEvent;
import com.unity3d.services.banners.view.BannerPosition;
import com.unity3d.services.banners.properties.BannerProperties;
import com.unity3d.services.banners.view.BannerView;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.List;

public class Banner {

    @WebViewExposed
    public static void load(final JSONArray viewsArray, final String style, final Integer width, final Integer height, final WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                BannerView view = BannerView.getOrCreateInstance();
                view.setBannerDimensions(width, height, BannerPosition.fromString(style));
                view.setViews(getArrayFromJSONArray(viewsArray));

                WebViewApp app = WebViewApp.getCurrentApp();
                if (app != null) {
                    app.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_LOADED);
                }
            }
        });
        callback.invoke();
    }

    @WebViewExposed
    public static void destroy(WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                BannerView view = BannerView.getInstance();
                if (view != null) {
                    view.destroy();

                    if (BannerProperties.getBannerParent() != null) {
                        ViewParent parent = BannerProperties.getBannerParent().getParent();
                        if (parent != null && parent instanceof ViewGroup) {
                            ((ViewGroup) parent).removeView(BannerProperties.getBannerParent());
                        }
                    }

                    BannerProperties.setBannerParent(null);
                    WebViewApp app = WebViewApp.getCurrentApp();
                    if (app != null) {
                        app.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_DESTROYED);
                    }
                }
            }
        });
        callback.invoke();
    }

    @WebViewExposed
    public static void setViewFrame(final String viewName, final Integer x, final Integer y, final Integer width, final Integer height, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BannerView view = BannerView.getInstance();
                if (view != null) {
                    view.setViewFrame(viewName, x, y, width, height);
                }
            }
        });
        callback.invoke();
    }

    @WebViewExposed
    public static void setViews(final JSONArray views, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BannerView view = BannerView.getInstance();
                if (view != null) {
                    view.setViews(getArrayFromJSONArray(views));
                }
            }
        });
        callback.invoke();
    }

    @WebViewExposed
    public static void setBannerFrame(final String style, final Integer width, final Integer height, WebViewCallback callback) {
        Utilities.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                BannerView view = BannerView.getInstance();
                if (view != null) {
                    view.setBannerDimensions(width, height, BannerPosition.fromString(style));
                    view.setLayoutParams(view.getLayoutParams());
                }
            }
        });
        callback.invoke();
    }

    private static List<String> getArrayFromJSONArray(JSONArray arr) {
        String[] out = new String[arr.length()];
        for (int i = 0; i < out.length; i++) {
            try {
                out[i] = arr.getString(i);
            } catch (JSONException e) {
                DeviceLog.warning("Exception converting JSON Array to String Array: %s", e);
            }
        }
        return Arrays.asList(out);
    }
}
