package com.unity3d.scar.adapter.common;

import android.app.Activity;
import android.content.Context;

import com.unity3d.scar.adapter.common.scarads.IScarAd;
import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;
import com.unity3d.scar.adapter.common.signals.ISignalsCollector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.unity3d.scar.adapter.common.Utils.runOnUiThread;

public abstract class ScarAdapterBase implements IScarAdapter {
	protected ISignalsCollector _signalCollector;
	protected Map<String, IScarAd> _loadedAds = new ConcurrentHashMap<>();
	protected IScarAd _currentAdReference;
	protected IAdsErrorHandler _adsErrorHandler;

	public ScarAdapterBase(IAdsErrorHandler adsErrorHandler) {
		_adsErrorHandler = adsErrorHandler;
	}

	@Override
	public void getSCARSignals(Context context, String[] interstitialList, String[] rewardedList, ISignalCollectionListener signalCompletionListener) {
		_signalCollector.getSCARSignals(context, interstitialList, rewardedList, signalCompletionListener);
	}

	@Override
	public void show(final Activity activity, String queryId, String placementId) {
		IScarAd scarAd = _loadedAds.get(placementId);

		if (scarAd == null) {
			_adsErrorHandler.handleError(GMAAdsError.NoAdsError(placementId, queryId, "Could not find ad for placement '" + placementId + "'."));
		} else {
			// We keep a reference to the ad so that we have it if a user leaves the app during show
			_currentAdReference = scarAd;
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					_currentAdReference.show(activity);
				}
			});
		}
	}
}
