package com.unity3d.scar.adapter.common.signals;

import java.util.HashMap;
import java.util.Map;

public class SignalsResult {

	private Map<String, String> _signalsMap;
	private String _errorMessage;

	public SignalsResult() {
		_signalsMap = new HashMap<>();
		_errorMessage = null;
	}

	public void addToSignalsMap(String placementId, String signal) {
		_signalsMap.put(placementId, signal);
	}

	public Map<String, String> getSignalsMap() {
		return _signalsMap;
	}

	public void setErrorMessage(String errorMessage) {
		_errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return _errorMessage;
	}
}
