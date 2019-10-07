package com.unity3d.services.banners.view;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.unity3d.services.ads.webplayer.WebPlayerViewCache;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.bridge.BannerBridge;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.misc.ViewUtilities;
import com.unity3d.services.ads.webplayer.WebPlayerView;

import org.json.JSONObject;

public class BannerWebPlayerContainer extends RelativeLayout {

	private WebPlayerView _webPlayerView;
	private int _lastVisibility = -1;
	private UnityBannerSize _size;

	private String _bannerAdId;
	private JSONObject _webSettings;
	private JSONObject _webPlayerSettings;
	private JSONObject _webPlayerEventSettings;
	private Runnable _unsubscribeLayoutChange = null;

	public BannerWebPlayerContainer(Context context, String bannerAdId, JSONObject webSettings, JSONObject webPlayerSettings, JSONObject webPlayerEventSettings, UnityBannerSize size) {
		super(context);
		_size = size;
		_bannerAdId = bannerAdId;
		_webSettings = webSettings;
		_webPlayerSettings = webPlayerSettings;
		_webPlayerEventSettings = webPlayerEventSettings;
		_webPlayerView = new WebPlayerView(context, bannerAdId, _webSettings, _webPlayerSettings);
		_webPlayerView.setEventSettings(_webPlayerEventSettings);
		this.subscribeOnLayoutChange();
		this.addView(_webPlayerView);
		this.setupLayoutParams();
	}

	private void subscribeOnLayoutChange() {
		if (_unsubscribeLayoutChange != null) {
			_unsubscribeLayoutChange.run();
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			final OnLayoutChangeListener onLayoutChangeListener = new OnLayoutChangeListener() {
				@Override
				public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
					BannerWebPlayerContainer.this.onLayoutChange(v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom);
				}
			};
			addOnLayoutChangeListener(onLayoutChangeListener);
			_unsubscribeLayoutChange = new Runnable() {
				@Override
				public void run() {
					removeOnLayoutChangeListener(onLayoutChangeListener);
				}
			};
		}
	}

	private void setupLayoutParams() {
		final int width = Math.round(ViewUtilities.pxFromDp(getContext(), this._size.getWidth()));
		final int height = Math.round(ViewUtilities.pxFromDp(getContext(), this._size.getHeight()));
		LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
		this.setLayoutParams(layoutParams);
		ViewGroup.LayoutParams webviewLayoutParams = this._webPlayerView.getLayoutParams();
		webviewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
		webviewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
		this._webPlayerView.setLayoutParams(webviewLayoutParams);
	}

	public void setWebPlayerEventSettings(JSONObject webPlayerEventSettings) {
		_webPlayerEventSettings = webPlayerEventSettings;
	}


	public void setWebPlayerSettings(JSONObject webSettings, JSONObject webPlayerSettings) {
		_webSettings = webSettings;
		_webPlayerSettings = webPlayerSettings;
	}

	public void destroy() {
		if (_unsubscribeLayoutChange != null) {
			_unsubscribeLayoutChange.run();
		}
		final BannerWebPlayerContainer self = this;
		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				self.removeAllViews();
				ViewParent parent = self.getParent();
				if (parent != null && parent instanceof ViewGroup) {
					((ViewGroup) parent).removeView(self);
				}
				if (self._webPlayerView != null) {
					self._webPlayerView.destroy();
				}
				self._webPlayerView = null;
			}
		});
	}

	public WebPlayerView getWebPlayer() {
		return _webPlayerView;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		BannerBridge.didAttach(_bannerAdId);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		BannerBridge.didDetach(_bannerAdId);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// Old values are both zero if just added to hierarchy
		if (!(oldw == 0 && oldh == 0)) {
			int left = getLeft();
			int right = getRight();
			float alpha = 1.0f;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				alpha = getAlpha();
			}
			BannerBridge.resize(_bannerAdId, left, right, w, h, alpha);
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
					BannerBridge.visibilityChanged(_bannerAdId, visibility);
				}
				_lastVisibility = visibility;
			}
		}
	}

	public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
		float alpha = 1.0f;
		if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
			alpha = getAlpha();
		}
		BannerBridge.resize(_bannerAdId, left, top, right, bottom, alpha);
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

}
