package com.unity3d.scar.adapter.common;

public interface IAdsErrorHandler<T extends IUnityAdsError> {
	void handleError(T unityError);
}
