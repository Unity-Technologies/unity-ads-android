package com.unity3d.ads;

public class UnityAdsLoadOptions extends UnityAdsBaseOptions {
	private String AD_MARKUP = "adMarkup";

	public UnityAdsLoadOptions() {
		super();
	}

	public void setAdMarkup(String adMarkup) {
		set(AD_MARKUP, adMarkup);
	}
}
