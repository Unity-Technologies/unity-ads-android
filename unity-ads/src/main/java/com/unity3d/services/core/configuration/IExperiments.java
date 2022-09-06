package com.unity3d.services.core.configuration;

import org.json.JSONObject;

import java.util.Map;

public interface IExperiments {
	boolean isTwoStageInitializationEnabled();
	boolean isForwardExperimentsToWebViewEnabled();
	boolean isNativeTokenEnabled();
	boolean isUpdatePiiFields();
	boolean isPrivacyRequestEnabled();
	boolean shouldNativeTokenAwaitPrivacy();
	boolean isNativeWebViewCacheEnabled();
	boolean isWebAssetAdCaching();
	boolean isNewLifecycleTimer();

	JSONObject getCurrentSessionExperiments();
	JSONObject getNextSessionExperiments();
	JSONObject getExperimentsAsJson();
	Map<String, String> getExperimentTags();
}
