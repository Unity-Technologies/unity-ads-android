package com.unity3d.services.ads.gmascar.handlers;

import com.unity3d.scar.adapter.common.signals.ISignalCollectionListener;
import com.unity3d.scar.adapter.common.GMAEvent;
import com.unity3d.services.ads.gmascar.utils.GMAEventSender;

public class SignalsHandler implements ISignalCollectionListener {

	private GMAEventSender _gmaEventSender;

	public SignalsHandler(GMAEventSender gmaEventSender) {
		_gmaEventSender = gmaEventSender;
	}

	@Override
	public void onSignalsCollected(String signalsMap) {
		_gmaEventSender.send(GMAEvent.SIGNALS, signalsMap);
	}

	@Override
	public void onSignalsCollectionFailed(String errorMsg) {
		_gmaEventSender.send(GMAEvent.SIGNALS_ERROR, errorMsg);
	}
}
