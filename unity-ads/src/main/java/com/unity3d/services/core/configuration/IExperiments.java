package com.unity3d.services.core.configuration;

import org.json.JSONObject;

import java.util.Map;

public interface IExperiments {
	boolean shouldNativeTokenAwaitPrivacy();
	boolean isNativeWebViewCacheEnabled();
	boolean isWebAssetAdCaching();
	boolean isWebGestureNotRequired();
	boolean isScarInitEnabled();
	boolean isJetpackLifecycle();
	boolean isOkHttpEnabled();
	boolean isWebMessageEnabled();
	boolean isWebViewAsyncDownloadEnabled();
	boolean isCronetCheckEnabled();
	boolean isNativeShowTimeoutDisabled();
	boolean isNativeLoadTimeoutDisabled();
	boolean isCaptureHDRCapabilitiesEnabled();
	boolean isScarBannerHbEnabled();
	boolean isPCCheckEnabled();

	JSONObject getCurrentSessionExperiments();
	JSONObject getNextSessionExperiments();
	JSONObject getExperimentsAsJson();
	Map<String, String> getExperimentTags();
}
