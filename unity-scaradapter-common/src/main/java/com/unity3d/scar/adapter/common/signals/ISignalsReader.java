package com.unity3d.scar.adapter.common.signals;

import android.content.Context;

public interface ISignalsReader {
	void getSCARSignals(Context context, String[] interstitialList, String[] rewardedList,
							   ISignalCollectionListener signalCompletionListener);
}
