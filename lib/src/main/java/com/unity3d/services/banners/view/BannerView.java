package com.unity3d.services.banners.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.ViewUtilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.ads.webplayer.WebPlayer;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BannerView extends RelativeLayout {
    private static final String VIEW_BANNER_PLAYER = "bannerplayer";
    private static final String VIEW_WEB_VIEW = "webview";
    private static final String VIEW_BANNER = "banner";

    private static JSONObject _webSettings = new JSONObject();
    private static JSONObject _webPlayerSettings = new JSONObject();
    private static JSONObject _webPlayerEventSettings = new JSONObject();
    private static BannerView _instance;
    private List<String> _views;
    private WebPlayer _webPlayer;
    private int _lastVisibility = -1;
    private int width;
    private int height;
    private BannerPosition position;

    public BannerView(Context context) {
        super(context);
        _webPlayer = new WebPlayer(context, VIEW_BANNER_PLAYER, _webSettings, _webPlayerSettings);
        _webPlayer.setEventSettings(_webPlayerEventSettings);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            addOnLayoutChangeListener(new OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    BannerView.this.onLayoutChange(v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom);
                }
            });
        }
    }

    public static void setWebPlayerEventSettings(JSONObject webPlayerEventSettings) {
        _webPlayerEventSettings = webPlayerEventSettings;
    }


    public static void setWebPlayerSettings(JSONObject webSettings, JSONObject webPlayerSettings) {
        _webSettings = webSettings;
        _webPlayerSettings = webPlayerSettings;
    }

    public void destroy() {
        removeAllViews();
        ViewParent parent = getParent();
        if (parent != null && parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(this);
        }
        this._webPlayer = null;
        BannerView._instance = null;
    }

    public static BannerView getOrCreateInstance() {
        if (_instance == null) {
            _instance = new BannerView(ClientProperties.getApplicationContext());
        }
        return _instance;
    }

    public static BannerView getInstance() {
        return _instance;
    }

    public void setViews(List<String> views) {
        List<String> viewsToAdd = new ArrayList<>(views);
        List<String> viewsToRemove = new ArrayList<>();
        if (this._views != null) {
            viewsToRemove.addAll(this._views);
            viewsToRemove.removeAll(views);
            viewsToAdd.removeAll(this._views);
        }
        this._views = views;
        for (String view : viewsToRemove) {
            this.removeView(view);
        }
        for (String view : viewsToAdd) {
            this.addView(view);
        }
    }

    private void removeView(String viewName) {
        View view = getViewForName(viewName);
        if (view != null) {
            ViewUtilities.removeViewFromParent(view);
        }

        switch (viewName) {
            case VIEW_BANNER_PLAYER:
                this._webPlayer = null;
                break;
        }
    }

    private void addView(String viewName) {
        View view = getViewForName(viewName);
        if (view != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addView(view, params);
        } else {
            DeviceLog.warning("No view defined for viewName: %s", viewName);
        }
    }

    public WebPlayer getWebPlayer() {
        return _webPlayer;
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params != null) {
            params.width = this.width;
            params.height = this.height;
            super.setLayoutParams(LayoutParamsHelper.updateLayoutParamsForPosition(params, position));
        }
    }

    public void setViewFrame(String viewName, Integer x, Integer y, Integer width, Integer height) {
        View view = getViewForName(viewName);
        if (view == null) {
            return;
        }

        if (view == this) {
            DeviceLog.warning("Not setting viewFrame for banner, use `setLayoutParams` instead.");
        } else {
            RelativeLayout.LayoutParams params = new LayoutParams(width, height);
            params.setMargins(x, y, 0, 0);
            view.setLayoutParams(params);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        WebViewApp app = WebViewApp.getCurrentApp();
        if (app != null) {
            app.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_ATTACHED);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        WebViewApp app = WebViewApp.getCurrentApp();
        if (app != null) {
            app.sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_DETACHED );
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Old values are both zero if just added to hierarchy
        if (!(oldw == 0 && oldh == 0)) {
            int left = getLeft();
            int right = getRight();
            float alpha = 1.0f;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                alpha = getAlpha();
            }
            WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_RESIZED, left, right, w, h, alpha);
            // Given our current rect, check that we are still able to be seen in the parent.
            Rect rect = new Rect();
            getHitRect(rect);
            if (((View) getParent()).getLocalVisibleRect(rect)) {
                onVisibilityChanged(this, View.GONE);
            }
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        if (changedView == this) {
            if (_lastVisibility == -1) {
                _lastVisibility = visibility;
            } else {
                if (visibility != View.VISIBLE && _lastVisibility == View.VISIBLE) {
                    WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_VISIBILITY_CHANGED, visibility);
                }
                _lastVisibility = visibility;
            }
        }
    }

    private View getViewForName(String name) {
        if (name.equals(VIEW_BANNER_PLAYER)) {
            return _webPlayer;
        } else if (name.equals(VIEW_WEB_VIEW)) {
            return WebViewApp.getCurrentApp().getWebView();
        } else if (name.equals(VIEW_BANNER)) {
            return this;
        }
        return null;
    }

    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        float alpha = 1.0f;
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            alpha = getAlpha();
        }
        WebViewApp.getCurrentApp().sendEvent(WebViewEventCategory.BANNER, BannerEvent.BANNER_RESIZED, left, top, right, bottom, alpha);
        if (getParent() != null) {
            // Given our current rect, check that we are still able to be seen in the parent.
            Rect rect = new Rect();
            getHitRect(rect);
            if (getParent() instanceof View) {
                if (!((View) getParent()).getLocalVisibleRect(rect)) {
                    onVisibilityChanged(this, View.GONE);
                }
            }
        }
    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);
        // Re-trigger layout change for sending alpha value.
        onLayoutChange(this, getLeft(), getTop(), getRight(), getBottom(), getLeft(), getTop(), getRight(), getBottom());
    }

    public void setBannerDimensions(int width, int height, BannerPosition position) {
        this.width = width;
        this.height = height;
        this.position = position;
    }
}
