package com.unity3d.scar.adapter.common;

import android.app.Activity;
import android.content.Context;

import com.unity3d.scar.adapter.common.scarads.ScarAdMetadata;
import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;

public interface IScarAdapter {

	void getSCARSignals(Context context, String[] interstitialList, String[] rewardedList, ISignalCollectionListener signalCompletionListener);
	void getSCARBiddingSignals(Context context, ISignalCollectionListener signalCompletionListener);
	void loadInterstitialAd(Context context, ScarAdMetadata scarAdMetadata, IScarInterstitialAdListenerWrapper adListener);
	void loadRewardedAd(Context context, ScarAdMetadata scarAdMetadata, IScarRewardedAdListenerWrapper adListener);
	void show(Activity activity, String queryId, String placementId);
}
