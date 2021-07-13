package com.unity3d.scar.adapter.v2000;

import android.content.Context;

import com.unity3d.scar.adapter.common.IAdsErrorHandler;
import com.unity3d.scar.adapter.common.IScarAdapter;
import com.unity3d.scar.adapter.common.IScarInterstitialAdListenerWrapper;
import com.unity3d.scar.adapter.common.IScarRewardedAdListenerWrapper;
import com.unity3d.scar.adapter.common.ScarAdapterBase;
import com.unity3d.scar.adapter.common.scarads.IScarLoadListener;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.v2000.scarads.ScarInterstitialAd;
import com.unity3d.scar.adapter.v2000.scarads.ScarRewardedAd;
import com.unity3d.scar.adapter.v2000.signals.SignalsReader;
import com.unity3d.scar.adapter.v2000.signals.SignalsStorage;

import static com.unity3d.scar.adapter.common.Utils.runOnUiThread;

public class ScarAdapter extends ScarAdapterBase implements IScarAdapter {

	private SignalsStorage _scarSignalStorage;

	public ScarAdapter(IAdsErrorHandler adsErrorHandler) {
		super(adsErrorHandler);
		_scarSignalStorage = new SignalsStorage();
		_scarSignalReader = new SignalsReader(_scarSignalStorage);
	}

	public void loadInterstitialAd(Context context, final ScarAdMetadata scarAd, final IScarInterstitialAdListenerWrapper adListenerWrapper) {
		final ScarInterstitialAd interstitialAd = new ScarInterstitialAd(context, _scarSignalStorage.getQueryInfoMetadata(scarAd.getPlacementId()), scarAd, _adsErrorHandler, adListenerWrapper);
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
		final ScarRewardedAd rewardedAd = new ScarRewardedAd(context, _scarSignalStorage.getQueryInfoMetadata(scarAd.getPlacementId()), scarAd, _adsErrorHandler, adListenerWrapper);
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
