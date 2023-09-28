package com.unity3d.services.banners;

import com.unity3d.ads.UnityAds;

public class BannerErrorInfo {
	public BannerErrorCode errorCode;
	public String errorMessage;
	public BannerErrorInfo (String errorMessage, BannerErrorCode errorCode)
	{
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public UnityAds.UnityAdsLoadError toLoadError() {
		switch (errorCode) {
			case NATIVE_ERROR:
				return UnityAds.UnityAdsLoadError.INVALID_ARGUMENT;
			case NO_FILL:
				return UnityAds.UnityAdsLoadError.NO_FILL;
			case WEBVIEW_ERROR:
				return UnityAds.UnityAdsLoadError.INTERNAL_ERROR;
		}
		return UnityAds.UnityAdsLoadError.INTERNAL_ERROR;
	}

	public static BannerErrorInfo fromLoadError(UnityAds.UnityAdsLoadError error, String message) {
		switch (error) {
			case INITIALIZE_FAILED:
			case INVALID_ARGUMENT:
			case TIMEOUT:
				return new BannerErrorInfo(message, BannerErrorCode.NATIVE_ERROR);
			case INTERNAL_ERROR:
				return new BannerErrorInfo(message, BannerErrorCode.WEBVIEW_ERROR);
			case NO_FILL:
				return new BannerErrorInfo(message, BannerErrorCode.NO_FILL);
			default:
				return new BannerErrorInfo(message, BannerErrorCode.UNKNOWN);
		}
	}
}
