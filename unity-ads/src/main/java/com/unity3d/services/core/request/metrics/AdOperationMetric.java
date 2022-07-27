package com.unity3d.services.core.request.metrics;

import com.unity3d.ads.UnityAds;

import java.util.HashMap;

public class AdOperationMetric {

	private static final String AD_LOAD_START = "native_load_started";
	private static final String AD_SHOW_START = "native_show_started";
	private static final String AD_LOAD_SUCCESS = "native_load_time_success";
	private static final String AD_SHOW_SUCCESS = "native_show_time_success";
	private static final String AD_LOAD_FAIL = "native_load_time_failure";
	private static final String AD_SHOW_FAIL = "native_show_time_failure";

	private static final String UNKNOWN = "unknown";
	private static final String REASON = "reason";
	private static final String INIT_STATE = "state";

	public static Metric newAdLoadStart() {
		return new Metric(
			AD_LOAD_START,
			null,
			null);
	}

	public static Metric newAdShowStart() {
		return new Metric(
			AD_SHOW_START,
			null,
			null);
	}

	public static Metric newAdLoadSuccess(Long durationMs) {
		return new Metric(
			AD_LOAD_SUCCESS,
			durationMs,
			null);
	}

	public static Metric newAdShowSuccess(Long durationMs) {
		return new Metric(
			AD_SHOW_SUCCESS,
			durationMs,
			null);
	}

	public static Metric newAdLoadFailure(AdOperationError error, Long durationMs) {
		return new Metric(
			AD_LOAD_FAIL,
			durationMs,
			getTags(error));
	}

	public static Metric newAdLoadFailure(UnityAds.UnityAdsLoadError error, Long durationMs) {
		return newAdLoadFailure(mapUnityAdsLoadError(error), durationMs);
	}

	public static Metric newAdShowFailure(AdOperationError error, Long durationMs) {
		return new Metric(
			AD_SHOW_FAIL,
			durationMs,
			getTags(error));
	}

	public static Metric newAdShowFailure(UnityAds.UnityAdsShowError error, Long durationMs) {
		return newAdShowFailure(mapUnityAdsShowError(error), durationMs);
	}

	private static HashMap<String, String> getTags(AdOperationError error) {
		final String errorMetric = error == null ? UNKNOWN : error.toString();
		return new HashMap<String, String>(){{
			put(REASON, errorMetric);
		}};
	}

	private static AdOperationError mapUnityAdsLoadError(UnityAds.UnityAdsLoadError error) {
		switch (error) {
			case INITIALIZE_FAILED:
				return AdOperationError.init_failed;
			case INTERNAL_ERROR:
				return AdOperationError.internal;
			case INVALID_ARGUMENT:
				return AdOperationError.invalid;
			case NO_FILL:
				return AdOperationError.no_fill;
			case TIMEOUT:
				return AdOperationError.timeout;
			default:
				return null;
		}
	}

	private static AdOperationError mapUnityAdsShowError(UnityAds.UnityAdsShowError error) {
		switch (error) {
			case NOT_INITIALIZED:
				return AdOperationError.init_failed;
			case NOT_READY:
				return AdOperationError.not_ready;
			case VIDEO_PLAYER_ERROR:
				return AdOperationError.player;
			case INVALID_ARGUMENT:
				return AdOperationError.invalid;
			case NO_CONNECTION:
				return AdOperationError.no_connection;
			case ALREADY_SHOWING:
				return AdOperationError.already_showing;
			case INTERNAL_ERROR:
				return AdOperationError.internal;
			default:
				return null;
		}
	}


}
