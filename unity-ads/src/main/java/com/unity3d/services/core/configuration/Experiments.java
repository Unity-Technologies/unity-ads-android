package com.unity3d.services.core.configuration;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Experiments extends ExperimentsBase {

	private static final Set<String> NEXT_SESSION_FLAGS = new HashSet<>(Arrays.asList("tsi", "tsi_upii", "tsi_p", "tsi_nt", "tsi_prr", "tsi_prw"));

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

	@Override
	public boolean isTwoStageInitializationEnabled() {
		return _experimentData.optBoolean(TSI_TAG_INIT_ENABLED, true);
	}

	@Override
	public boolean isForwardExperimentsToWebViewEnabled() {
		return _experimentData.optBoolean(TSI_TAG_FORWARD_FEATURE_FLAGS, false);
	}

	@Override
	public boolean isNativeTokenEnabled() {
		return _experimentData.optBoolean(TSI_TAG_NATIVE_TOKEN, true);
	}

	@Override
	public boolean isUpdatePiiFields() {
		return _experimentData.optBoolean(TSI_TAG_UPDATE_PII_FIELDS, false);
	}

	@Override
	public boolean isPrivacyRequestEnabled() {
		return _experimentData.optBoolean(TSI_TAG_PRIVACY_REQUEST, true);
	}

	@Override
	public boolean shouldNativeTokenAwaitPrivacy() {
		return _experimentData.optBoolean(TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY, false);
	}

	@Override
	public boolean isNativeWebViewCacheEnabled() {
		return _experimentData.optBoolean(EXP_TAG_NATIVE_WEBVIEW_CACHE, false);
	}

	@Override
	public boolean isWebAssetAdCaching() {
		return _experimentData.optBoolean(EXP_TAG_WEB_AD_ASSET_CACHING, false);
	}

	@Override
	public boolean isWebGestureNotRequired() {
		return _experimentData.optBoolean(EXP_TAG_WEB_GESTURE_NOT_REQUIRED, false);
	}


		@Override
	public boolean isNewLifecycleTimer() {
		return _experimentData.optBoolean(EXP_TAG_NEW_LIFECYCLE_TIMER, false);
	}

	public JSONObject getExperimentsAsJson() {
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

	@Override
	public JSONObject getNextSessionExperiments() {
		if (_experimentData == null) return null;
		Map<String, String> nextSessionFlags = new HashMap<>();
		for (Iterator<String> it = _experimentData.keys(); it.hasNext();) {
			String currentKey = it.next();
			if (NEXT_SESSION_FLAGS.contains(currentKey)) {
				nextSessionFlags.put(currentKey, String.valueOf(_experimentData.optBoolean(currentKey)));
			}
		}
		return new JSONObject(nextSessionFlags);
	}

	@Override
	public JSONObject getCurrentSessionExperiments() {
		if (_experimentData == null) return null;
		Map<String, String> currentSessionFlags = new HashMap<>();
		for (Iterator<String> it = _experimentData.keys(); it.hasNext();) {
			String currentKey = it.next();
			if (!NEXT_SESSION_FLAGS.contains(currentKey)) {
				currentSessionFlags.put(currentKey, String.valueOf(_experimentData.optBoolean(currentKey)));
			}
		}
		return new JSONObject(currentSessionFlags);
	}

}
