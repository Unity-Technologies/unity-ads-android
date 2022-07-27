package com.unity3d.services.core.misc;

import java.util.List;

public class JsonFlattenerRules {
	List<String> _topLevelToInclude;
	List<String> _reduceKeys;
	List<String> _skipKeys;

	public JsonFlattenerRules(List<String> topLevelToInclude, List<String> reduceKeys, List<String> skipKeys) {
		_topLevelToInclude = topLevelToInclude;
		_reduceKeys = reduceKeys;
		_skipKeys = skipKeys;
	}

	public List<String> getTopLevelToInclude() {
		return _topLevelToInclude;
	}

	public List<String> getReduceKeys() {
		return _reduceKeys;
	}

	public List<String> getSkipKeys() {
		return _skipKeys;
	}
}
