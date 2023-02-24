package com.unity3d.services.core.configuration;

public abstract class ExperimentsBase implements IExperiments {
	static final String TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY = "tsi_prw";
	static final String EXP_TAG_NATIVE_WEBVIEW_CACHE = "nwc";
	static final String EXP_TAG_WEB_AD_ASSET_CACHING = "wac";
	static final String EXP_TAG_WEB_GESTURE_NOT_REQUIRED = "wgr";
	static final String EXP_TAG_SCAR_INIT = "scar_init";
	static final String EXP_TAG_NEW_INIT_FLOW = "s_init";
	static final String EXP_TAG_SCAR_BIDDING_MANAGER = "scar_bm";
	static final String EXP_TAG_JETPACK_LIFECYCLE = "gjl";

	static final boolean EXP_DEFAULT_VALUE = false;
}
