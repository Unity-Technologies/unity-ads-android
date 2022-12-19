package com.unity3d.scar.adapter.common.requests;

import android.os.Bundle;

public class RequestExtras {

	public static String QUERY_INFO_TYPE = "query_info_type";
	public static String REQUESTER_TYPE = "requester_type_5";
	public static String VERSION_PREFIX = "UnityScar";

	private String _versionName;

	public RequestExtras(String unityVersion) {
		_versionName = new StringBuilder(VERSION_PREFIX).append(unityVersion).toString();
	}

	public Bundle getExtras() {
		Bundle extras = new Bundle();
		extras.putString(QUERY_INFO_TYPE, REQUESTER_TYPE);
		return extras;
	}

	public String getVersionName() {
		return _versionName;
	}
}