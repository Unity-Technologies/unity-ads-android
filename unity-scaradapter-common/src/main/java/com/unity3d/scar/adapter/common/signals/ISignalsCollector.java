package com.unity3d.scar.adapter.common.signals;

import android.content.Context;

import com.unity3d.scar.adapter.common.DispatchGroup;
import com.unity3d.scar.adapter.common.scarads.UnityAdFormat;

public interface ISignalsCollector {
	void getSCARSignal(Context context, String placementId, UnityAdFormat adFormat,
							   ISignalCollectionListener signalCompletionListener);
	void getSCARSignal(Context context, String placementId, UnityAdFormat adFormat, final DispatchGroup dispatchGroup,
					   final SignalsResult signalsResult);
	void getSCARSignalForHB(Context context, UnityAdFormat adFormat, final DispatchGroup dispatchGroup,
					   final SignalsResult signalsResult);
	void getSCARBiddingSignals(Context context, boolean isBannerEnabled, ISignalCollectionListener signalCompletionListener);
}
