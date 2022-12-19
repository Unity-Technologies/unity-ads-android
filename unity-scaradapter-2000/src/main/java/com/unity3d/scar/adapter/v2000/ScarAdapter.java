package com.unity3d.scar.adapter.v2000;

import android.content.Context;

import com.google.android.gms.ads.query.QueryInfo;
import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.IScarAdapter;
import com.unity3d.scar.adapter.common.IScarInterstitialAdListenerWrapper;
import com.unity3d.scar.adapter.common.IScarRewardedAdListenerWrapper;
import com.unity3d.scar.adapter.common.ScarAdapterBase;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.common.signals.SignalsStorage;
import com.unity3d.scar.adapter.v2000.scarads.ScarInterstitialAd;
import com.unity3d.scar.adapter.v2000.scarads.ScarRewardedAd;
import com.unity3d.scar.adapter.v2000.signals.SignalsCollector;

import static com.unity3d.scar.adapter.common.Utils.runOnUiThread;

public class ScarAdapter extends ScarAdapterBase implements IScarAdapter {

	private SignalsStorage<QueryInfo> _signalsStorage;

	public ScarAdapter(IAdsErrorHandler adsErrorHandler) {
		super(adsErrorHandler);
		_signalsStorage = new SignalsStorage();
		_signalCollector = new SignalsCollector(_signalsStorage);
	}

	public void loadInterstitialAd(Context context, final ScarAdMetadata scarAd, final IScarInterstitialAdListenerWrapper adListenerWrapper) {
		final ScarInterstitialAd interstitialAd = new ScarInterstitialAd(context, _signalsStorage.getQueryInfo(scarAd.getPlacementId()), scarAd, _adsErrorHandler, adListenerWrapper);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				interstitialAd.loadAd(new IScarLoadListener() {
					@Override
					public void onAdLoaded() {
						_loadedAds.put(scarAd.getPlacementId(), interstitialAd);
					}
				});
			}
		});
	}

	public void loadRewardedAd(Context context, final ScarAdMetadata scarAd, final IScarRewardedAdListenerWrapper adListenerWrapper) {
		final ScarRewardedAd rewardedAd = new ScarRewardedAd(context, _signalsStorage.getQueryInfo(scarAd.getPlacementId()), scarAd, _adsErrorHandler, adListenerWrapper);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				rewardedAd.loadAd(new IScarLoadListener() {
					@Override
					public void onAdLoaded() {
						_loadedAds.put(scarAd.getPlacementId(), rewardedAd);
					}
				});
			}
		});
	}

}
