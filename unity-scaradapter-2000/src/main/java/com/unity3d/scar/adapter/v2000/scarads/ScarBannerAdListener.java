package com.unity3d.scar.adapter.v2000.scarads;

import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.unity3d.scar.adapter.common.IScarBannerAdListenerWrapper;

public class ScarBannerAdListener extends ScarAdListener {

	private final IScarBannerAdListenerWrapper _adListenerWrapper;
	private final ScarBannerAd _scarBannerAd;

	public ScarBannerAdListener(IScarBannerAdListenerWrapper adListenerWrapper, ScarBannerAd scarBannerAd) {
		_adListenerWrapper = adListenerWrapper;
		_scarBannerAd = scarBannerAd;
	}

	private final AdListener _adListener = new AdListener() {
		@Override
		public void onAdClicked() {
			super.onAdClicked();
			_adListenerWrapper.onAdClicked();
		}

		@Override
		public void onAdClosed() {
			super.onAdClosed();
			_adListenerWrapper.onAdClosed();
		}

		@Override
		public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
			super.onAdFailedToLoad(loadAdError);
			_scarBannerAd.removeAdView();
			_adListenerWrapper.onAdFailedToLoad(loadAdError.getCode(), loadAdError.getMessage());
		}

		@Override
		public void onAdImpression() {
			super.onAdImpression();
			_adListenerWrapper.onAdImpression();
		}

		@Override
		public void onAdLoaded() {
			super.onAdLoaded();
			_adListenerWrapper.onAdLoaded();
		}

		@Override
		public void onAdOpened() {
			super.onAdOpened();
			_adListenerWrapper.onAdOpened();
		}
	};

	public AdListener getAdListener() {
		return _adListener;
	}
}

