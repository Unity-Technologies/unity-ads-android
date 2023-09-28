package com.unity3d.scar.adapter.v2000.signals;

import android.content.Context;

import com.google.android.gms.ads.AdFormat;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.query.QueryInfo;
import com.unity3d.scar.adapter.common.DispatchGroup;
import com.unity3d.scar.adapter.common.scarads.UnityAdFormat;
import com.unity3d.scar.adapter.common.signals.ISignalsCollector;
import com.unity3d.scar.adapter.common.signals.SignalCallbackListener;
import com.unity3d.scar.adapter.common.signals.SignalsCollectorBase;
import com.unity3d.scar.adapter.common.signals.SignalsResult;
import com.unity3d.scar.adapter.common.signals.SignalsStorage;

public class SignalsCollector extends SignalsCollectorBase implements ISignalsCollector {
	private SignalsStorage<QueryInfo> _signalsStorage;

	public SignalsCollector(SignalsStorage<QueryInfo> signalsStorage) {
		_signalsStorage = signalsStorage;
	}

	@Override
	public void getSCARSignal(final Context context, final String placementId, final UnityAdFormat adFormat, final DispatchGroup dispatchGroup, final SignalsResult signalsResult) {
		AdRequest request = new AdRequest.Builder().build();
		QueryInfoCallback queryInfoCallback = new QueryInfoCallback(placementId, new SignalCallbackListener(dispatchGroup, _signalsStorage, signalsResult));
		QueryInfo.generate(context, getAdFormat(adFormat), request, queryInfoCallback);
	}

	@Override
	public void getSCARSignalForHB(final Context context, final UnityAdFormat adFormat, final DispatchGroup dispatchGroup, final SignalsResult signalsResult) {
		onOperationNotSupported("GMA v2000 - SCAR signal retrieval without a placementId not relevant",
			dispatchGroup, signalsResult);
	}

	public AdFormat getAdFormat(UnityAdFormat adFormat) {
		switch (adFormat) {
			case BANNER:
				return AdFormat.BANNER;
			case INTERSTITIAL:
				return AdFormat.INTERSTITIAL;
			case REWARDED:
				return AdFormat.REWARDED;
		}
		return AdFormat.BANNER;
	}
}
