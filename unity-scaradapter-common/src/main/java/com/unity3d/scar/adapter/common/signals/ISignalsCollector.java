package com.unity3d.scar.adapter.common.signals;

import android.content.Context;

import com.unity3d.scar.adapter.common.DispatchGroup;

public interface ISignalsCollector {
	void getSCARSignals(Context context, String[] interstitialList, String[] rewardedList,
							   ISignalCollectionListener signalCompletionListener);
	void getSCARSignal(Context context, String placementId, boolean isInterstitial,
					   DispatchGroup dispatchGroup, SignalsResult signalsResult);
}
