package com.unity3d.scar.adapter.common.signals;

import android.content.Context;

import com.unity3d.scar.adapter.common.DispatchGroup;

import com.unity3d.scar.adapter.common.scarads.UnityAdFormat;
import org.json.JSONObject;

import java.util.Map;

public abstract class SignalsCollectorBase implements ISignalsCollector {

	public static final String SCAR_RV_SIGNAL = "gmaScarBiddingRewardedSignal";
	public static final String SCAR_INT_SIGNAL = "gmaScarBiddingInterstitialSignal";
	public static final String SCAR_BAN_SIGNAL = "gmaScarBiddingBannerSignal";

	public SignalsCollectorBase() {}

	@Override
	public void getSCARSignal(Context context, String placementId, UnityAdFormat adFormat,
							   ISignalCollectionListener signalCompletionListener) {
		DispatchGroup dispatchGroup = new DispatchGroup();
		SignalsResult signalsResult = new SignalsResult();

		dispatchGroup.enter();
		getSCARSignal(context, placementId, adFormat, dispatchGroup, signalsResult);

		dispatchGroup.notify(new GMAScarDispatchCompleted(signalCompletionListener, signalsResult));
	}

	@Override
	public void getSCARBiddingSignals(Context context, boolean isBannerEnabled, ISignalCollectionListener signalCompletionListener) {
		DispatchGroup dispatchGroup = new DispatchGroup();
		SignalsResult signalsResult = new SignalsResult();

		dispatchGroup.enter();
		getSCARSignalForHB(context, UnityAdFormat.INTERSTITIAL, dispatchGroup, signalsResult);

		dispatchGroup.enter();
		getSCARSignalForHB(context, UnityAdFormat.REWARDED, dispatchGroup, signalsResult);

		if (isBannerEnabled) {
			dispatchGroup.enter();
			getSCARSignalForHB(context, UnityAdFormat.BANNER, dispatchGroup, signalsResult);
		}

		dispatchGroup.notify(new GMAScarDispatchCompleted(signalCompletionListener, signalsResult));
	}

	public void onOperationNotSupported(String msg, DispatchGroup dispatchGroup, SignalsResult signalsResult) {
		signalsResult.setErrorMessage(String.format("Operation Not supported: %s.", msg));
		dispatchGroup.leave();
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

	public String getAdKey(UnityAdFormat adFormat) {
		switch (adFormat) {
			case BANNER:
				return SCAR_BAN_SIGNAL;
			case INTERSTITIAL:
				return SCAR_INT_SIGNAL;
			case REWARDED:
				return SCAR_RV_SIGNAL;
		}
		return "";
	}
}
