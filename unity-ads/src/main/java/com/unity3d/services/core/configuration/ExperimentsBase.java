package com.unity3d.services.core.configuration;

public abstract class ExperimentsBase implements IExperiments {
	static final String TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY = "tsi_prw";
	static final String EXP_TAG_NATIVE_WEBVIEW_CACHE = "nwc";
	static final String EXP_TAG_WEB_AD_ASSET_CACHING = "wac";
	static final String EXP_TAG_WEB_GESTURE_NOT_REQUIRED = "wgr";
	static final String EXP_TAG_SCAR_INIT = "scar_init";
	static final String EXP_TAG_JETPACK_LIFECYCLE = "gjl";
	static final String EXP_TAG_OK_HTTP = "okhttp";
	static final String EXP_TAG_WEB_MESSAGE = "jwm";
	static final String EXP_TAG_WEBVIEW_ASYNC_DOWNLOAD = "wad";
	static final String EXP_TAG_CRONET_CHECK = "cce";
	static final String EXP_TAG_SHOW_TIMEOUT_DISABLED = "nstd";
	static final String EXP_TAG_LOAD_TIMEOUT_DISABLED = "nltd";
	static final String EXP_TAG_HDR_CAPABILITIES = "hdrc";
	static final String EXP_TAG_SCAR_HB_BN = "scar_bn";
	static final String EXP_TAG_IS_PC_CHECK_ENABLED = "pc_check";
	static final boolean EXP_DEFAULT_VALUE = false;
}
