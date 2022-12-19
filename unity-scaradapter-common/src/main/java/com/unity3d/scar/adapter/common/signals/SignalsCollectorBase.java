package com.unity3d.scar.adapter.common.signals;

import android.content.Context;

import com.unity3d.scar.adapter.common.DispatchGroup;

import org.json.JSONObject;

import java.util.Map;

public abstract class SignalsCollectorBase implements ISignalsCollector {

	public SignalsCollectorBase() {}

	@Override
	public void getSCARSignals(Context context, String[] interstitialList, String[] rewardedList,
							   ISignalCollectionListener signalCompletionListener) {
		DispatchGroup dispatchGroup = new DispatchGroup();
		SignalsResult signalsResult = new SignalsResult();

		for (String interstitialId : interstitialList) {
			dispatchGroup.enter();
			getSCARSignal(context, interstitialId, true, dispatchGroup, signalsResult);
		}

		for (String rewardedId : rewardedList) {
			dispatchGroup.enter();
			getSCARSignal(context, rewardedId, false, dispatchGroup, signalsResult);
		}

		dispatchGroup.notify(new GMAScarDispatchCompleted(signalCompletionListener, signalsResult));
	}

	private class GMAScarDispatchCompleted implements Runnable {

		private ISignalCollectionListener _signalListener;
		private SignalsResult _signalsResult;

		public GMAScarDispatchCompleted(ISignalCollectionListener signalListener, SignalsResult signalsResult) {
			_signalListener = signalListener;
			_signalsResult = signalsResult;
		}

		@Override
		public void run() {
			// Called once every dispatched thread has returned.
			// Build up the JSON response since we received all the signals.
			Map<String, String> signalsMap = _signalsResult.getSignalsMap();
			if (signalsMap.size() > 0) {
				JSONObject placementJSON = new JSONObject(signalsMap);
				_signalListener.onSignalsCollected(placementJSON.toString());
			} else if (_signalsResult.getErrorMessage() == null) {
				_signalListener.onSignalsCollected("");
			} else {
				// If no signals could be generated, send SIGNALS_ERROR with the last error message
				_signalListener.onSignalsCollectionFailed(_signalsResult.getErrorMessage());
			}
		}
	}
}
