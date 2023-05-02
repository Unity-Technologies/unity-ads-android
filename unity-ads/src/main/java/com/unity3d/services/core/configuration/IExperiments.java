package com.unity3d.services.core.configuration;

import org.json.JSONObject;

import java.util.Map;

public interface IExperiments {
	boolean shouldNativeTokenAwaitPrivacy();
	boolean isNativeWebViewCacheEnabled();
	boolean isWebAssetAdCaching();
	boolean isWebGestureNotRequired();
	boolean isScarInitEnabled();
	String getScarBiddingManager();
	boolean isJetpackLifecycle();
	boolean isOkHttpEnabled();
	boolean isWebMessageEnabled();
	boolean isWebViewAsyncDownloadEnabled();

	JSONObject getCurrentSessionExperiments();
	JSONObject getNextSessionExperiments();
	JSONObject getExperimentsAsJson();
	Map<String, String> getExperimentTags();
}
