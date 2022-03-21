package com.unity3d.services.core.device.reader.pii;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_KEY;

import java.util.HashMap;
import java.util.Map;

public class PiiDecisionData {
	private final Map<String, Object> _attributes;
	private final DataSelectorResult _resultType;

	public PiiDecisionData(DataSelectorResult resultType) {
		this(resultType, new HashMap<String, Object>());
	}

	public PiiDecisionData(DataSelectorResult resultType, Map<String, Object> attributes) {
		_resultType = resultType;
		_attributes = attributes;
	}

	public void appendData(Map<String, Object> appendedAttributes) {
		if (_attributes != null) {
			_attributes.putAll(appendedAttributes);
		}
	}

	public DataSelectorResult getResultType() {
		return _resultType;
	}

	public Map<String, Object> getAttributes() {
		return _attributes;
	}

	public Boolean getUserNonBehavioralFlag() {
		Boolean userNonBehavioralFlag = null;
		if (_attributes != null) {
			Object userNonBehavioralFlagObj = _attributes.get(USER_NON_BEHAVIORAL_KEY);
			if (userNonBehavioralFlagObj instanceof Boolean) {
				userNonBehavioralFlag = (Boolean) userNonBehavioralFlagObj;
			}
		}
		return userNonBehavioralFlag;
	}

}
