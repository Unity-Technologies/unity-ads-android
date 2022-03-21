package com.unity3d.services.core.configuration;

import com.unity3d.services.core.log.DeviceLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Experiments {

	private static final String TSI_TAG_INIT_ENABLED = "tsi";
	private static final String TSI_TAG_INIT_POST = "tsi_p";
	private static final String TSI_TAG_FORWARD_FEATURE_FLAGS = "fff";
	private static final String TSI_TAG_UPDATE_PII_FIELDS  = "tsi_upii";
	private static final String TSI_TAG_DEVELOPER_CONSENT = "tsi_dc";
	private static final String TSI_TAG_NATIVE_TOKEN = "tsi_nt";

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

	public void setTwoStageInitializationEnabled(boolean tsiEnabled) {
		try {
			_experimentData.put(TSI_TAG_INIT_ENABLED, tsiEnabled);
		} catch (JSONException e) {
			DeviceLog.warning("Could not set TSI flag to " + tsiEnabled);
		}
	}

	public boolean isTwoStageInitializationEnabled() {
		return _experimentData.optBoolean(TSI_TAG_INIT_ENABLED, false);
	}

	public boolean isPOSTMethodInConfigRequestEnabled() {
		return _experimentData.optBoolean(TSI_TAG_INIT_POST, false);
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

	public boolean isHandleDeveloperConsent() {
		return _experimentData.optBoolean(TSI_TAG_DEVELOPER_CONSENT, false);
	}

	public JSONObject getExperimentData() {
		return _experimentData;
	}

	public Map<String, String> getExperimentTags() {
		Map<String, String> map = new HashMap<>();

		map.put(TSI_TAG_INIT_ENABLED, String.valueOf(isTwoStageInitializationEnabled()));
		map.put(TSI_TAG_INIT_POST, String.valueOf(isPOSTMethodInConfigRequestEnabled()));
		map.put(TSI_TAG_FORWARD_FEATURE_FLAGS, String.valueOf(isForwardExperimentsToWebViewEnabled()));
		map.put(TSI_TAG_UPDATE_PII_FIELDS, String.valueOf(isUpdatePiiFields()));
		map.put(TSI_TAG_DEVELOPER_CONSENT, String.valueOf(isHandleDeveloperConsent()));
		map.put(TSI_TAG_NATIVE_TOKEN, String.valueOf(isNativeTokenEnabled()));

		return map;
	}

}
