package com.unity3d.services.core.configuration;

import com.unity3d.services.core.device.reader.DeviceInfoReaderCompressor;
import com.unity3d.services.core.device.reader.DeviceInfoReaderCompressorWithMetrics;
import com.unity3d.services.core.device.reader.DeviceInfoReaderUrlEncoder;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.WebRequest;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationRequestFactory {
	private final String _configUrl;
	private final Configuration _configuration;
	private final IDeviceInfoReader _deviceInfoReader;


	public ConfigurationRequestFactory(Configuration configuration, IDeviceInfoReader deviceInfoReader , String configUrl) {
		_configuration = configuration;
		_deviceInfoReader = deviceInfoReader;
		_configUrl = configUrl;
	}

	public WebRequest getWebRequest() throws MalformedURLException {
		String url = _configUrl + buildQueryString();
		DeviceLog.debug("Requesting configuration with: " + url);
		Experiments experiments = _configuration.getExperiments();
		WebRequest webRequest;
		if (experiments != null && experiments.isTwoStageInitializationEnabled() && experiments.isPOSTMethodInConfigRequestEnabled()) {
			Map<String, List<String>> headers = new HashMap<>();
			headers.put("Content-Encoding", Collections.singletonList("gzip"));
			webRequest = new WebRequest(url, "POST", headers);
			DeviceInfoReaderCompressorWithMetrics infoReaderCompressor = new DeviceInfoReaderCompressorWithMetrics(new DeviceInfoReaderCompressor(_deviceInfoReader), experiments);
			byte[] queryData = infoReaderCompressor.getDeviceData();
			webRequest.setBody(queryData);
		} else {
			webRequest = new WebRequest(url, "GET");
		}
		return webRequest;
	}

	private String buildQueryString () {
		StringBuilder queryString = new StringBuilder();
		queryString.append("?");
		Experiments experiments = _configuration.getExperiments();
		if (experiments != null && experiments.isTwoStageInitializationEnabled()) {
			queryString.append(buildCompressedQueryStringIfNeeded());
		} else {
			queryString.append("ts=").append(System.currentTimeMillis());
			queryString.append("&sdkVersion=").append(SdkProperties.getVersionCode());
			queryString.append("&sdkVersionName=").append(SdkProperties.getVersionName());
			queryString.append("&gameId=").append(ClientProperties.getGameId());
		}
		return queryString.toString();
	}

	private String buildCompressedQueryStringIfNeeded() {
		String compressedQueryString = "";
		Experiments experiments = _configuration.getExperiments();
		if (experiments != null) {
			if (experiments.isTwoStageInitializationEnabled() && !experiments.isPOSTMethodInConfigRequestEnabled()) {
				String queryData = new DeviceInfoReaderUrlEncoder(new DeviceInfoReaderCompressorWithMetrics(new DeviceInfoReaderCompressor(_deviceInfoReader), experiments)).getUrlEncodedData();
				compressedQueryString = "c=" + queryData;
			}
		}
		return compressedQueryString;
	}
}
