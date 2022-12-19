package com.unity3d.scar.adapter.common.signals;

import com.unity3d.scar.adapter.common.DispatchGroup;
import com.unity3d.scar.adapter.common.signals.ISignalCallbackListener;
import com.unity3d.scar.adapter.common.signals.SignalsResult;
import com.unity3d.scar.adapter.common.signals.SignalsStorage;

public class SignalCallbackListener<T> implements ISignalCallbackListener<T> {

	private DispatchGroup _dispatchGroup;
	private SignalsStorage<T> _signalsStorage;
	private SignalsResult _signalsResult;

	public SignalCallbackListener(final DispatchGroup dispatchGroup,
								  final SignalsResult signalsResult) {
		this(dispatchGroup, null, signalsResult);
	}

	public SignalCallbackListener(final DispatchGroup dispatchGroup,
								  final SignalsStorage<T> signalsStorage,
								  final SignalsResult signalsResult) {
		_dispatchGroup = dispatchGroup;
		_signalsStorage = signalsStorage;
		_signalsResult = signalsResult;
	}

	@Override
	public void onSuccess(String placementId, String signal, T queryInfo) {
		_signalsResult.addToSignalsMap(placementId, signal);
		if (_signalsStorage != null) {
			_signalsStorage.put(placementId, queryInfo);
		}
		_dispatchGroup.leave();
	}

	@Override
	public void onFailure(String errorMessage) {
		_signalsResult.setErrorMessage(errorMessage);
		_dispatchGroup.leave();
	}
}