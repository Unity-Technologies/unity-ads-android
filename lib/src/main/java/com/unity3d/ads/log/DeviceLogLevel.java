package com.unity3d.ads.log;

public class DeviceLogLevel {

	private String _receivingMethodName = null;
	private static final String LOG_TAG = "UnityAds";

	public DeviceLogLevel(String receivingMethodName) {
		_receivingMethodName = receivingMethodName;
	}

	public String getLogTag () {
		return LOG_TAG;
	}

	public String getReceivingMethodName () {
		return _receivingMethodName;
	}
}
