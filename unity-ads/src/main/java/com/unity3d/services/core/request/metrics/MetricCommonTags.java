package com.unity3d.services.core.request.metrics;

import java.util.HashMap;
import java.util.Map;

public class MetricCommonTags {

	private static final String METRIC_COMMON_TAG_COUNTRY_ISO = "iso";
	private static final String METRIC_COMMON_TAG_PLATFORM = "plt";
	private static final String METRIC_COMMON_TAG_SDK_VERSION = "sdk";
	private static final String METRIC_COMMON_TAG_SYSTEM_VERSION = "system";

	public static final String METRIC_COMMON_TAG_PLATFORM_ANDROID = "android";

	private String _countryISO;
	private String _platform;
	private String _sdkVersion;
	private String _systemVersion;

	public MetricCommonTags(String countryISO, String platform, String sdkVersion, String systemVersion) {
		this._countryISO = countryISO;
		this._platform = platform;
		this._sdkVersion = sdkVersion;
		this._systemVersion = systemVersion;
	}

	public Map<String, String> asMap() {
		Map<String, String> result = new HashMap<>();

		result.put(METRIC_COMMON_TAG_COUNTRY_ISO, _countryISO);
		result.put(METRIC_COMMON_TAG_PLATFORM, _platform);
		result.put(METRIC_COMMON_TAG_SDK_VERSION, _sdkVersion);
		result.put(METRIC_COMMON_TAG_SYSTEM_VERSION, _systemVersion);

		return result;
	}
}
