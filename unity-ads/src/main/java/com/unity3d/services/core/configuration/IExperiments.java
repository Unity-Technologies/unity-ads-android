package com.unity3d.services.core.configuration;

import org.json.JSONObject;

import java.util.Map;

public interface IExperiments {
	boolean shouldNativeTokenAwaitPrivacy();
	boolean isNativeWebViewCacheEnabled();
	boolean isWebAssetAdCaching();
	boolean isWebGestureNotRequired();
	boolean isScarInitEnabled();
	boolean isNewInitFlowEnabled();
	String getScarBiddingManager();
	boolean isJetpackLifecycle();

	JSONObject getCurrentSessionExperiments();
	JSONObject getNextSessionExperiments();
	JSONObject getExperimentsAsJson();
	Map<String, String> getExperimentTags();
}
