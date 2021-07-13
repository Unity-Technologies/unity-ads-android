package com.unity3d.scar.adapter.common.signals;

public interface ISignalCollectionListener {
	void onSignalsCollected(String signalsMap);
	void onSignalsCollectionFailed(String errorMsg);
}
