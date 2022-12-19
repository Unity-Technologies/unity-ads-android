package com.unity3d.scar.adapter.common.signals;

public interface ISignalCallbackListener<T> {
	void onSuccess(String placementId, String signal, T queryInfo);
	void onFailure(String errorMessage);
}
