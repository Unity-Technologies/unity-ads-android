package com.unity3d.services.core.configuration;

import com.unity3d.services.core.device.reader.DeviceInfoReaderBuilder;
import com.unity3d.services.core.device.reader.DeviceInfoReaderCompressor;
import com.unity3d.services.core.device.reader.DeviceInfoReaderCompressorWithMetrics;
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
	private final Configuration _configuration;
	private final DeviceInfoReaderBuilder _deviceInfoReaderBuilder;

	public ConfigurationRequestFactory(Configuration configuration, DeviceInfoReaderBuilder deviceInfoReaderBuilder) {
		_configuration = configuration;
		_deviceInfoReaderBuilder = deviceInfoReaderBuilder;
	}

	public Configuration getConfiguration() {
		return _configuration;
	}

	public WebRequest getWebRequest() throws MalformedURLException {
		String configBaseUrl = _configuration.getConfigUrl();
		if (configBaseUrl == null) throw new MalformedURLException("Base URL is null");
		StringBuilder urlBuilder = new StringBuilder(configBaseUrl);
		Experiments experiments = _configuration.getExperiments();
		WebRequest webRequest;
		if (experiments != null && experiments.isTwoStageInitializationEnabled()) {
			Map<String, List<String>> headers = new HashMap<>();
			headers.put("Content-Encoding", Collections.singletonList("gzip"));
			webRequest = new WebRequest(urlBuilder.toString(), "POST", headers);
			DeviceInfoReaderCompressorWithMetrics infoReaderCompressor = new DeviceInfoReaderCompressorWithMetrics(new DeviceInfoReaderCompressor(_deviceInfoReaderBuilder.build()));
			byte[] queryData = infoReaderCompressor.getDeviceData();
			webRequest.setBody(queryData);
		} else {
			urlBuilder.append("?ts=").append(System.currentTimeMillis());
			urlBuilder.append("&sdkVersion=").append(SdkProperties.getVersionCode());
			urlBuilder.append("&sdkVersionName=").append(SdkProperties.getVersionName());
			urlBuilder.append("&gameId=").append(ClientProperties.getGameId());
			webRequest = new WebRequest(urlBuilder.toString(), "GET");
		}
		DeviceLog.debug("Requesting configuration with: " + urlBuilder);
		return webRequest;
	}
}
