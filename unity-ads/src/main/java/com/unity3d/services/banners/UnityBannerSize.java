package com.unity3d.services.banners;

import android.content.Context;
import android.content.res.Resources;

import com.unity3d.services.core.misc.ViewUtilities;

public class UnityBannerSize {

	private int width;
	private int height;

	public UnityBannerSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public static UnityBannerSize getDynamicSize(Context context) {
		return new UnityBannerSize(BannerSize.BANNER_SIZE_DYNAMIC.getWidth(context), BannerSize.BANNER_SIZE_DYNAMIC.getHeight(context));
	}

	private enum BannerSize {
		BANNER_SIZE_STANDARD,
		BANNER_SIZE_LEADERBOARD,
		BANNER_SIZE_IAB_STANDARD,
		BANNER_SIZE_DYNAMIC;

		private static final int LEADERBOARD_WIDTH = 728;
		private static final int LEADERBOARD_HEIGHT = 90;
		private static final int IAB_STANDARD_WIDTH = 468;
		private static final int IAB_STANDARD_HEIGHT = 60;
		private static final int STANDARD_WIDTH = 320;
		private static final int STANDARD_HEIGHT = 50;

		private BannerSize getNonDynamicSize(final Context context) {
			if (this == BANNER_SIZE_DYNAMIC) {
				// convert absolute px width to dp
				int screenWidth = Math.round(ViewUtilities.dpFromPx(context, Resources.getSystem().getDisplayMetrics().widthPixels));
				if (screenWidth >= LEADERBOARD_WIDTH) {
					return BANNER_SIZE_LEADERBOARD;
				} else if (screenWidth >= IAB_STANDARD_WIDTH) {
					return BANNER_SIZE_IAB_STANDARD;
				} else {
					return BANNER_SIZE_STANDARD;
				}
			} else {
				return this;
			}
		}

		// returns width in dp
		private int getWidth(final Context context) {
			final BannerSize bannerSize = this.getNonDynamicSize(context);
			switch (bannerSize) {
				case BANNER_SIZE_STANDARD:
					return STANDARD_WIDTH;
				case BANNER_SIZE_LEADERBOARD:
					return LEADERBOARD_WIDTH;
				case BANNER_SIZE_IAB_STANDARD:
					return IAB_STANDARD_WIDTH;
				default:
					return STANDARD_WIDTH;
			}
		}

		// returns height in dp
		private int getHeight(final Context context) {
			final BannerSize bannerSize = this.getNonDynamicSize(context);
			switch (bannerSize) {
				case BANNER_SIZE_STANDARD:
					return STANDARD_HEIGHT;
				case BANNER_SIZE_LEADERBOARD:
					return LEADERBOARD_HEIGHT;
				case BANNER_SIZE_IAB_STANDARD:
					return IAB_STANDARD_HEIGHT;
				default:
					return STANDARD_HEIGHT;
			}
		}
	}

}
