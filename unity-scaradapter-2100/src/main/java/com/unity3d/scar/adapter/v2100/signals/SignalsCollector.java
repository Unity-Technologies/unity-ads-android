package com.unity3d.scar.adapter.v2100.signals;

import android.content.Context;

import com.google.android.gms.ads.AdFormat;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.query.QueryInfo;
import com.unity3d.scar.adapter.common.DispatchGroup;
import com.unity3d.scar.adapter.common.scarads.UnityAdFormat;
import com.unity3d.scar.adapter.common.signals.*;
import com.unity3d.scar.adapter.v2100.requests.AdRequestFactory;

public class SignalsCollector extends SignalsCollectorBase implements ISignalsCollector {
	private AdRequestFactory _adRequestFactory;

	public SignalsCollector(AdRequestFactory adRequestFactory) {
		_adRequestFactory = adRequestFactory;
	}

	@Override
	public void getSCARSignal(final Context context, final String placementId, final UnityAdFormat adFormat, final DispatchGroup dispatchGroup, final SignalsResult signalsResult) {
		AdRequest request = _adRequestFactory.buildAdRequest();
		QueryInfoCallback queryInfoCallback = new QueryInfoCallback(placementId, new SignalCallbackListener(dispatchGroup, signalsResult));
		QueryInfo.generate(context, getAdFormat(adFormat), request, queryInfoCallback);
	}

	@Override
	public void getSCARSignalForHB(Context context, UnityAdFormat adFormat, DispatchGroup dispatchGroup, SignalsResult signalsResult) {
		getSCARSignal(context, getAdKey(adFormat), adFormat, dispatchGroup, signalsResult);
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
		return AdFormat.UNKNOWN;
	}
}
