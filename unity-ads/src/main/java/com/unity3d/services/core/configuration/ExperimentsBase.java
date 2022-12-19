package com.unity3d.services.core.configuration;

public abstract class ExperimentsBase implements IExperiments {
	static final String TSI_TAG_INIT_ENABLED = "tsi";
	static final String TSI_TAG_FORWARD_FEATURE_FLAGS = "fff";
	static final String TSI_TAG_UPDATE_PII_FIELDS  = "tsi_upii";
	static final String TSI_TAG_NATIVE_TOKEN = "tsi_nt";
	static final String TSI_TAG_PRIVACY_REQUEST = "tsi_prr";
	static final String TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY = "tsi_prw";
	static final String EXP_TAG_NATIVE_WEBVIEW_CACHE = "nwc";
	static final String EXP_TAG_WEB_AD_ASSET_CACHING = "wac";
	static final String EXP_TAG_WEB_GESTURE_NOT_REQUIRED = "wgr";
	static final String EXP_TAG_NEW_LIFECYCLE_TIMER = "nilt";
	static final String EXP_TAG_SCAR_INIT = "scar_init";
	static final String EXP_TAG_NEW_INIT_FLOW = "s_init";

	static final boolean EXP_DEFAULT_VALUE = false;
}
