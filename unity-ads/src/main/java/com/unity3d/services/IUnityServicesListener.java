package com.unity3d.services;

public interface IUnityServicesListener {
	void onUnityServicesError(UnityServices.UnityServicesError error, String message);
}
