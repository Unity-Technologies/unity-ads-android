package com.wds.ads.request;

public interface IResolveHostListener {
	void onResolve(String host, String address);
	void onFailed(String host, ResolveHostError error, String errorMessage);
}
