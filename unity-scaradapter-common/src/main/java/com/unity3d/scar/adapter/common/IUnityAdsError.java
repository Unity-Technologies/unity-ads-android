package com.unity3d.scar.adapter.common;

public interface IUnityAdsError {
	String getDomain();//domain to represent a custom error or a class
	String getDescription(); // description of the error, localized message
	int getCode();// unique code per domain
}
