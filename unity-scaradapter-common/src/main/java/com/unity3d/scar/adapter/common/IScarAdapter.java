package com.unity3d.scar.adapter.common;

import android.app.Activity;
import android.content.Context;

import android.widget.RelativeLayout;
import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.common.scarads.UnityAdFormat;
import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;

public interface IScarAdapter {
	void getSCARSignal(Context context, String placementId, UnityAdFormat adFormat, ISignalCollectionListener signalCompletionListener);
	void getSCARBiddingSignals(Context context, boolean isBannerEnabled, ISignalCollectionListener signalCompletionListener);
	void loadInterstitialAd(Context context, ScarAdMetadata scarAdMetadata, IScarInterstitialAdListenerWrapper adListener);
	void loadRewardedAd(Context context, ScarAdMetadata scarAdMetadata, IScarRewardedAdListenerWrapper adListener);
	void loadBannerAd(Context context, RelativeLayout bannerView, ScarAdMetadata scarAdMetadata, int width, int height, IScarBannerAdListenerWrapper adListener);
	void show(Activity activity, String queryId, String placementId);
}
