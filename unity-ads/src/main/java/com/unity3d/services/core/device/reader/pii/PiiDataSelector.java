package com.unity3d.services.core.device.reader.pii;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.UNIFIED_CONFIG_PII_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_KEY;

import com.unity3d.services.core.configuration.IExperiments;
import com.unity3d.services.core.misc.IJsonStorageReader;
import com.unity3d.services.core.misc.Utilities;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PiiDataSelector {
	private final PiiTrackingStatusReader _piiTrackingStatusReader;
	private final IJsonStorageReader _jsonStorageReader;
	private final IExperiments _experiments;

	public PiiDataSelector(PiiTrackingStatusReader piiTrackingStatusReader, IJsonStorageReader jsonStorageReader, IExperiments experiments) {
		_piiTrackingStatusReader = piiTrackingStatusReader;
		_jsonStorageReader = jsonStorageReader;
		_experiments = experiments;
	}

	public PiiDecisionData whatToDoWithPII() {
		switch (_piiTrackingStatusReader.getPrivacyMode()) {
			case NONE:
			case NULL:
				return allowTrackingDecision();
			case MIXED:
				return mixedModeDecision();
			default:
				return notAllowedDecision();
		}
	}

	private PiiDecisionData allowTrackingDecision() {
		return new PiiDecisionData(_experiments.isUpdatePiiFields() ? DataSelectorResult.UPDATE : DataSelectorResult.INCLUDE, getPiiContentFromStorage());
	}

	private PiiDecisionData notAllowedDecision() {
		return new PiiDecisionData(DataSelectorResult.EXCLUDE);
	}

	private PiiDecisionData mixedModeDecision() {
		if (_piiTrackingStatusReader.getUserNonBehavioralFlag()) {
			return new PiiDecisionData(DataSelectorResult.INCLUDE, getUserBehavioralAttribute());
		} else {
			PiiDecisionData mixedDecision = allowTrackingDecision();
			mixedDecision.appendData(getUserBehavioralAttribute());
			return mixedDecision;
		}
	}

	private HashMap<String, Object> getUserBehavioralAttribute() {
		return new HashMap<String, Object>() {{
			put(USER_NON_BEHAVIORAL_KEY, _piiTrackingStatusReader.getUserNonBehavioralFlag());
		}};
	}

	private Map<String, Object> getPiiContentFromStorage() {
		Map<String, Object> piiDataMap = new HashMap<>();
		if (_jsonStorageReader != null) {
			Object piiData = _jsonStorageReader.get(UNIFIED_CONFIG_PII_KEY);
			if (piiData instanceof JSONObject) {
				piiDataMap = Utilities.combineJsonIntoMap(piiDataMap, (JSONObject) piiData, UNIFIED_CONFIG_PII_KEY + ".");
			}
		}
		return piiDataMap;
	}

}
