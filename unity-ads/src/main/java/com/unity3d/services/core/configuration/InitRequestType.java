package com.unity3d.services.core.configuration;

public enum InitRequestType {
	PRIVACY("privacy"),
	TOKEN("token_srr");

	InitRequestType(String callType) { _callType = callType;}

	private String _callType;
	public String getCallType() { return _callType; }
}
