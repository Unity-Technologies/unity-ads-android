package com.unity3d.services.core.request;

public interface IResolveHostListener {
	void onResolve(String host, String address);
	void onFailed(String host, ResolveHostError error, String errorMessage);
}
