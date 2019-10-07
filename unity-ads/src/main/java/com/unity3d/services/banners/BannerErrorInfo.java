package com.unity3d.services.banners;

public class BannerErrorInfo {
	public BannerErrorCode errorCode;
	public String errorMessage;
	public BannerErrorInfo (String errorMessage, BannerErrorCode errorCode)
	{
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
}
