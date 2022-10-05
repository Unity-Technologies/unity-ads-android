package com.unity3d.services.core.configuration;

import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.log.DeviceLog;

import org.json.JSONObject;

import java.util.Locale;

public class ExperimentObject {
	private static final String VALUE_KEY = "value";
	private static final String APPLIED_KEY = "applied";

	private final JSONObject _experimentData;

	public ExperimentObject(JSONObject experimentData) {
		_experimentData = (experimentData != null) ? experimentData : new JSONObject();
	}

	public boolean getBooleanValue() {
		return _experimentData.optBoolean(VALUE_KEY);
	}

	public ExperimentAppliedRule getAppliedRule() {
		ExperimentAppliedRule experimentAppliedRule = ExperimentAppliedRule.NEXT;
		String appliedRules = _experimentData.optString(APPLIED_KEY);
		// If the applied rule is missing (empty) we just don't try to parse it and fallback to NEXT
		if (!appliedRules.isEmpty()) {
			try {
				experimentAppliedRule = ExperimentAppliedRule.valueOf(appliedRules.toUpperCase());
			} catch (IllegalArgumentException ex) {
				DeviceLog.warning("Invalid rule %s for experiment", appliedRules);
			}
		}
		return experimentAppliedRule;
	}
}
