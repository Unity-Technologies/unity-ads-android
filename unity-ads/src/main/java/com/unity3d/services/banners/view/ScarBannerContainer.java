package com.unity3d.services.banners.view;

import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.unity3d.services.banners.bridge.BannerBridge;
import com.unity3d.services.core.misc.Utilities;

public class ScarBannerContainer extends RelativeLayout {

	private String _bannerAdId;

	public ScarBannerContainer(Context context, String bannerAdId) {
		super(context);
		_bannerAdId = bannerAdId;
	}

	public void destroy() {
		final ScarBannerContainer self = this;
		Utilities.runOnUiThread(() -> {
			self.removeAllViews();
			ViewParent parent = self.getParent();
			if (parent instanceof ViewGroup) {
				((ViewGroup) parent).removeView(self);
			}
		});
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		BannerBridge.didAttachScarBanner(_bannerAdId);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		BannerBridge.didDetachScarBanner(_bannerAdId);
	}
}
