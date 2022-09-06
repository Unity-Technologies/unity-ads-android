package com.unity3d.services.core.configuration;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExperimentObjects extends ExperimentsBase {
	private final JSONObject _experimentObjetsData;
	private final Map<String, ExperimentObject> _experimentObjects = new HashMap<>();

	public ExperimentObjects(JSONObject experimentObjects) {
		if (experimentObjects != null) {
			_experimentObjetsData = experimentObjects;
			for (Iterator<String> it = experimentObjects.keys(); it.hasNext();) {
				String key = it.next();
				_experimentObjects.put(key, new ExperimentObject(experimentObjects.optJSONObject(key)));
			}
		} else {
			_experimentObjetsData = new JSONObject();
		}
	}

	public ExperimentObject getExperimentObject(String experimentName) {
		return _experimentObjects.get(experimentName);
	}

	@Override
	public boolean isTwoStageInitializationEnabled() {
		return getExperimentValue(TSI_TAG_INIT_ENABLED);
	}

	@Override
	public boolean isForwardExperimentsToWebViewEnabled() {
		return getExperimentValue(TSI_TAG_FORWARD_FEATURE_FLAGS);
	}

	@Override
	public boolean isNativeTokenEnabled() {
		return getExperimentValue(TSI_TAG_NATIVE_TOKEN);
	}

	@Override
	public boolean isUpdatePiiFields() {
		return getExperimentValue(TSI_TAG_UPDATE_PII_FIELDS);
	}

	@Override
	public boolean isPrivacyRequestEnabled() {
		return getExperimentValue(TSI_TAG_PRIVACY_REQUEST);
	}

	@Override
	public boolean shouldNativeTokenAwaitPrivacy() {
		return getExperimentValue(TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY);
	}

	@Override
	public boolean isNativeWebViewCacheEnabled() {
		return getExperimentValue(EXP_TAG_NATIVE_WEBVIEW_CACHE);
	}

	@Override
	public boolean isWebAssetAdCaching() {
		return getExperimentValue(EXP_TAG_WEB_AD_ASSET_CACHING);
	}

	@Override
	public boolean isNewLifecycleTimer() {
		return getExperimentValue(EXP_TAG_NEW_LIFECYCLE_TIMER);
	}

	private boolean getExperimentValue(String experimentName) {
		ExperimentObject expo = getExperimentObject(experimentName);
		return (expo != null) ? expo.getBooleanValue() : EXP_DEFAULT_VALUE;
	}

	@Override
	public JSONObject getExperimentsAsJson() {
		return _experimentObjetsData;
	}

	@Override
	public Map<String, String> getExperimentTags() {
		Map<String, String> map = new HashMap<>();
		for (Map.Entry<String, ExperimentObject> entry : _experimentObjects.entrySet()) {
			map.put(entry.getKey(), String.valueOf(entry.getValue().getBooleanValue()));
		}
		return map;
	}

	@Override
	public JSONObject getCurrentSessionExperiments() {
		return getExperimentWithAppliedRule(ExperimentAppliedRule.IMMEDIATE);
	}

	@Override
	public JSONObject getNextSessionExperiments() {
		return getExperimentWithAppliedRule(ExperimentAppliedRule.NEXT);

	}

	private JSONObject getExperimentWithAppliedRule(ExperimentAppliedRule experimentAppliedRule) {
		Map<String, String> map = new HashMap<>();
		for (Map.Entry<String, ExperimentObject> entry : _experimentObjects.entrySet()) {
			if (entry.getValue().getAppliedRule() == experimentAppliedRule) {
				map.put(entry.getKey(), String.valueOf(entry.getValue().getBooleanValue()));
			}
		}
		return new JSONObject(map);
	}
}
