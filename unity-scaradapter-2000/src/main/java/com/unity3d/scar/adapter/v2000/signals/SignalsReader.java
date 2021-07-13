package com.unity3d.scar.adapter.v2000.signals;

import android.content.Context;

import com.google.android.gms.ads.AdFormat;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.query.QueryInfo;
import com.unity3d.scar.adapter.common.DispatchGroup;
import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;
import com.unity3d.scar.adapter.common.signals.ISignalsReader;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignalsReader implements ISignalsReader {
	private static Map<String, String> _placementSignalMap;
	private static SignalsStorage _signalsStorage;

	public SignalsReader(SignalsStorage signalsStorage) {
		_signalsStorage = signalsStorage;
	}

	@Override
	public void getSCARSignals(Context context, String[] interstitialList, String[] rewardedList,
							   ISignalCollectionListener signalCompletionListener) {
		DispatchGroup dispatchGroup = new DispatchGroup();

		for (String interstitialId : interstitialList) {
			dispatchGroup.enter();
			getSCARSignal(context, interstitialId, AdFormat.INTERSTITIAL, dispatchGroup);
		}

		for (String rewardedId : rewardedList) {
			dispatchGroup.enter();
			getSCARSignal(context, rewardedId, AdFormat.REWARDED, dispatchGroup);
		}

		dispatchGroup.notify(new GMAScarDispatchCompleted(signalCompletionListener));
	}

	private void getSCARSignal(Context context, String placementId, AdFormat adType, DispatchGroup dispatchGroup) {
		AdRequest request = new AdRequest.Builder().build();
		QueryInfoMetadata gmaQueryInfoMetadata = new QueryInfoMetadata(placementId);
		QueryInfoCallback gmaQueryInfoCallback = new QueryInfoCallback(gmaQueryInfoMetadata, dispatchGroup);
		// Callback on the QueryInfoCallback is async here.
		_signalsStorage.put(placementId, gmaQueryInfoMetadata);
		QueryInfo.generate(context, adType, request, gmaQueryInfoCallback);
	}

	private class GMAScarDispatchCompleted implements Runnable {

		private ISignalCollectionListener _signalListener;

		public GMAScarDispatchCompleted(ISignalCollectionListener signalListener) {
			_signalListener = signalListener;
		}

		@Override
		public void run() {
			// Called once every dispatched thread has returned.
			// Build up the JSON response since we received all the signals.
			_placementSignalMap = new HashMap<>();
			String errorMessage = null;
			for(Map.Entry<String, QueryInfoMetadata> queryInfoMetadata : _signalsStorage.getPlacementQueryInfoMap().entrySet()) {
				QueryInfoMetadata currentQueryMetadata = queryInfoMetadata.getValue();
				_placementSignalMap.put(currentQueryMetadata.getPlacementId(), currentQueryMetadata.getQueryStr());
				if (currentQueryMetadata.getError() != null) {
					// There is an error with one of the signals.
					errorMessage = currentQueryMetadata.getError();
				}
			}

			if (_placementSignalMap.size() > 0) {
				JSONObject placementJSON = new JSONObject(_placementSignalMap);
				_signalListener.onSignalsCollected(placementJSON.toString());
			} else if (errorMessage == null){
				_signalListener.onSignalsCollected("");
			} else {
				// If no signals could be generated, send SIGNALS_ERROR with the last error message
				_signalListener.onSignalsCollectionFailed(errorMessage);
			}
		}
	}
}
