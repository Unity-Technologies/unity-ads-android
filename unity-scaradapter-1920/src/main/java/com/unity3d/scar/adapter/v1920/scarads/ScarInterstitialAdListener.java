package com.unity3d.scar.adapter.v1920.scarads;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.IScarInterstitialAdListenerWrapper;

public class ScarInterstitialAdListener {

	private InterstitialAd _interstitialAd;
	private IScarInterstitialAdListenerWrapper _adListenerWrapper;
	private IScarLoadListener _loadListener;

	public ScarInterstitialAdListener(InterstitialAd interstitialAd, IScarInterstitialAdListenerWrapper adListenerWrapper) {
		_interstitialAd = interstitialAd;
		_adListenerWrapper = adListenerWrapper;
	}

	private AdListener _adListener = new AdListener() {
			@Override
			public void onAdLoaded() {
				_adListenerWrapper.onAdLoaded();
				if (_loadListener != null) {
					_loadListener.onAdLoaded();
				}
			}

			@Override
			public void onAdFailedToLoad(int adErrorCode) {
				_adListenerWrapper.onAdFailedToLoad(adErrorCode, "SCAR ad failed to load");
			}

			@Override
			public void onAdOpened() {
				_adListenerWrapper.onAdOpened();
			}

			@Override
			public void onAdClicked() {
				_adListenerWrapper.onAdClicked();
			}

			@Override
			public void onAdLeftApplication() {
				_adListenerWrapper.onAdLeftApplication();
			}

			@Override
			public void onAdClosed() {
				_adListenerWrapper.onAdClosed();
			}
	};

	public AdListener getAdListener() {
		return _adListener;
	}

	public void setLoadListener(IScarLoadListener loadListener) {
		_loadListener = loadListener;
	}

}
