package com.unity3d.scar.adapter.v2000.scarads;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.unity3d.scar.adapter.common.IScarInterstitialAdListenerWrapper;

public class ScarInterstitialAdListener extends ScarAdListener {
	private final ScarInterstitialAd _scarInterstitialAd;
	private final IScarInterstitialAdListenerWrapper _adListenerWrapper;

	public ScarInterstitialAdListener(IScarInterstitialAdListenerWrapper adListenerWrapper, ScarInterstitialAd scarInterstitialAd) {
		_adListenerWrapper = adListenerWrapper;
		_scarInterstitialAd = scarInterstitialAd;
	}

	private final InterstitialAdLoadCallback _adLoadCallback = new InterstitialAdLoadCallback() {
		@Override
		public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
			super.onAdLoaded(interstitialAd);
			_adListenerWrapper.onAdLoaded();
			interstitialAd.setFullScreenContentCallback(_fullScreenContentCallback);
			_scarInterstitialAd.setGmaAd(interstitialAd);
			if (_loadListener != null) {
				_loadListener.onAdLoaded();
			}
		}

		@Override
		public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
			super.onAdFailedToLoad(loadAdError);
			_adListenerWrapper.onAdFailedToLoad(loadAdError.getCode(), loadAdError.toString());
		}
	};

	private final FullScreenContentCallback _fullScreenContentCallback = new FullScreenContentCallback() {
		@Override
		public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
			super.onAdFailedToShowFullScreenContent(adError);
			_adListenerWrapper.onAdFailedToShow(adError.getCode(), adError.toString());
		}

		@Override
		public void onAdShowedFullScreenContent() {
			super.onAdShowedFullScreenContent();
			_adListenerWrapper.onAdOpened();
		}

		@Override
		public void onAdDismissedFullScreenContent() {
			super.onAdDismissedFullScreenContent();
			_adListenerWrapper.onAdClosed();
		}

		@Override
		public void onAdImpression() {
			super.onAdImpression();
			_adListenerWrapper.onAdImpression();
		}
	};

	public InterstitialAdLoadCallback getAdLoadListener() {
		return _adLoadCallback;
	}

}
