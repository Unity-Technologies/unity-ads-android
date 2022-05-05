package com.unity3d.scar.adapter.v1950.scarads;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.unity3d.scar.adapter.common.GMAAdsError;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.IScarInterstitialAdListenerWrapper;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.v1950.signals.QueryInfoMetadata;

public class ScarInterstitialAd extends ScarAdBase {

	private InterstitialAd _interstitialAd;
	private ScarInterstitialAdListener _interstitialAdDelegate;

	public ScarInterstitialAd(Context context, QueryInfoMetadata queryInfoMetadata, ScarAdMetadata scarAdMetadata, IAdsErrorHandler adsErrorHandler, IScarInterstitialAdListenerWrapper adListener) {
		super(context, scarAdMetadata, queryInfoMetadata, adsErrorHandler);
		_interstitialAd = new InterstitialAd(_context);
		_interstitialAd.setAdUnitId(_scarAdMetadata.getAdUnitId());
		_interstitialAdDelegate = new ScarInterstitialAdListener(_interstitialAd, adListener);
	}

	@Override
	public void loadAdInternal(IScarLoadListener loadListener, AdRequest adRequest) {
		_interstitialAd.setAdListener(_interstitialAdDelegate.getAdListener());
		_interstitialAdDelegate.setLoadListener(loadListener);
		_interstitialAd.loadAd(adRequest);
	}

	@Override
	public void show(Activity activity) {
		if (_interstitialAd.isLoaded()) {
			_interstitialAd.show();
		} else {
			_adsErrorHandler.handleError(GMAAdsError.AdNotLoadedError(_scarAdMetadata));
		}
	}
}
