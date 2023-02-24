package com.unity3d.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;
import com.unity3d.scar.adapter.common.signals.SignalsCollectorBase;
import com.unity3d.services.ads.gmascar.listeners.IBiddingSignalsListener;
import com.unity3d.services.ads.gmascar.models.BiddingSignals;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class that wraps {@link IBiddingSignalsListener}, handling GMA SCAR adapter
 * formatted signals into project format {@link BiddingSignals}.
 */
public class BiddingSignalsHandler implements ISignalCollectionListener {

	private final IBiddingSignalsListener listener;

	/**
	 * Constructor that initializes the handler with the passed listener.
	 *
	 * @param listener {@link IBiddingSignalsListener} implementation to notify sender.
	 */
	public BiddingSignalsHandler(IBiddingSignalsListener listener) {
		this.listener = listener;
	}

	@Override
	public void onSignalsCollected(String signalsMap) {
		listener.onSignalsReady(getSignals(signalsMap));
	}

	@Override
	public void onSignalsCollectionFailed(String errorMsg) {
		listener.onSignalsFailure(errorMsg);
	}

	// TODO Refactor onSignalsCollected to expect JSON instead of a string
	private BiddingSignals getSignals(String signalsMap) {
		try {
			JSONObject signalsJson = new JSONObject(signalsMap);
			return new BiddingSignals(
				getSignalFromJson(signalsJson, SignalsCollectorBase.SCAR_RV_SIGNAL),
				getSignalFromJson(signalsJson, SignalsCollectorBase.SCAR_INT_SIGNAL)
			);
		} catch (JSONException e) {
			return null;
		}
	}

	private String getSignalFromJson(JSONObject signalsJson, String key) {
		return signalsJson.optString(key);
	}
}
