package com.unity3d.services.core.configuration;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Experiments {

	private static final String TSI_TAG_INIT_ENABLED = "tsi";
	private static final String TSI_TAG_FORWARD_FEATURE_FLAGS = "fff";
	private static final String TSI_TAG_UPDATE_PII_FIELDS  = "tsi_upii";
	private static final String TSI_TAG_NATIVE_TOKEN = "tsi_nt";
	private static final String TSI_TAG_PRIVACY_REQUEST = "tsi_prr";
	private static final String TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY = "tsi_prw";
	private static final String EXP_TAG_NATIVE_WEBVIEW_CACHE = "nwc";
	private static final String EXP_TAG_WEB_AD_ASSET_CACHING = "wac";
	private static final String EXP_TAG_NEW_LIFECYCLE_TIMER = "nlt";

	private final JSONObject _experimentData;

	public Experiments() {
		this(null);
	}

	public Experiments(JSONObject experimentData) {
		if (experimentData == null) {
			_experimentData = new JSONObject();
		} else {
			_experimentData = experimentData;
		}
	}

	public boolean isTwoStageInitializationEnabled() {
		return _experimentData.optBoolean(TSI_TAG_INIT_ENABLED, false);
	}

	public boolean isForwardExperimentsToWebViewEnabled() {
		return _experimentData.optBoolean(TSI_TAG_FORWARD_FEATURE_FLAGS, false);
	}

	public boolean isNativeTokenEnabled() {
		return _experimentData.optBoolean(TSI_TAG_NATIVE_TOKEN, false);
	}

	public boolean isUpdatePiiFields() {
		return _experimentData.optBoolean(TSI_TAG_UPDATE_PII_FIELDS, false);
	}

	public boolean isPrivacyRequestEnabled() {
		return _experimentData.optBoolean(TSI_TAG_PRIVACY_REQUEST, false);
	}

	public boolean shouldNativeTokenAwaitPrivacy() {
		return _experimentData.optBoolean(TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY, false);
	}

	public boolean isNativeWebViewCacheEnabled() {
		return _experimentData.optBoolean(EXP_TAG_NATIVE_WEBVIEW_CACHE, false);
	}

	public boolean isWebAssetAdCaching() {
		return _experimentData.optBoolean(EXP_TAG_WEB_AD_ASSET_CACHING, false);
	}

	public boolean isNewLifecycleTimer() {
		return _experimentData.optBoolean(EXP_TAG_NEW_LIFECYCLE_TIMER, false);
	}

	public JSONObject getExperimentData() {
		return _experimentData;
	}

	public Map<String, String> getExperimentTags() {
		Map<String, String> map = new HashMap<>();
		for (Iterator<String> keyItor = _experimentData.keys(); keyItor.hasNext(); ) {
			String key = keyItor.next();
			map.put(key, String.valueOf(_experimentData.opt(key)));
		}
		return map;
	}

}
