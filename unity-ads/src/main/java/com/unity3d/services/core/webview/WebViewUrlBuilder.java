package com.unity3d.services.core.webview;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class WebViewUrlBuilder {
	private final String _urlWithQueryString;
	private final SDKMetricsSender _sdkMetricsSender = Utilities.getService(SDKMetricsSender.class);

	public WebViewUrlBuilder(String baseUrl, Configuration configuration) {
		String queryString = "?platform=android";

		queryString += buildQueryParam("origin", configuration.getWebViewUrl());
		queryString += buildQueryParam("version", configuration.getWebViewVersion());
		queryString += buildQueryParam("isNativeCollectingMetrics", String.valueOf(_sdkMetricsSender.areMetricsEnabledForCurrentSession()));

		_urlWithQueryString = baseUrl + queryString;
	}

	public String getUrlWithQueryString() {
		return _urlWithQueryString;
	}

	private String buildQueryParam(String fieldName, String fieldData) {
		String queryString = "";
		try {
			if (fieldData != null) {
				queryString += "&" + fieldName + "=" + URLEncoder.encode(fieldData, "UTF-8");
			}
		} catch (UnsupportedEncodingException e) {
			DeviceLog.exception(String.format("Unsupported charset when encoding %s", fieldName), e);
		}
		return queryString;
	}
}
