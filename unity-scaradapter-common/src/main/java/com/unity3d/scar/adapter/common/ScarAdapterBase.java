package com.unity3d.scar.adapter.common;

import android.app.Activity;
import android.content.Context;

import com.unity3d.scar.adapter.common.scarads.IScarFullScreenAd;
import com.unity3d.scar.adapter.common.scarads.UnityAdFormat;
import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;
import com.unity3d.scar.adapter.common.signals.ISignalsCollector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.unity3d.scar.adapter.common.Utils.runOnUiThread;

public abstract class ScarAdapterBase implements IScarAdapter {
	protected ISignalsCollector _signalCollector;
	protected Map<String, IScarFullScreenAd> _loadedAds = new ConcurrentHashMap<>();
	protected IScarFullScreenAd _currentAdReference;
	protected IAdsErrorHandler<WebViewAdsError> _adsErrorHandler;

	public ScarAdapterBase(IAdsErrorHandler<WebViewAdsError> adsErrorHandler) {
		_adsErrorHandler = adsErrorHandler;
	}

	@Override
	public void getSCARBiddingSignals(Context context, boolean isBannerEnabled, ISignalCollectionListener signalCompletionListener) {
		_signalCollector.getSCARBiddingSignals(context, isBannerEnabled, signalCompletionListener);
	}

	@Override
	public void getSCARSignal(Context context, String placementId, UnityAdFormat adFormat, ISignalCollectionListener signalCompletionListener) {
		_signalCollector.getSCARSignal(context, placementId, adFormat, signalCompletionListener);
	}

	@Override
	public void show(final Activity activity, String queryId, String placementId) {
		IScarFullScreenAd scarAd = _loadedAds.get(placementId);

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
