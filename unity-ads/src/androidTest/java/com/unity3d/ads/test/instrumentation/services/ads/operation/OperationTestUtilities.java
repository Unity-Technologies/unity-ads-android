package com.unity3d.ads.test.instrumentation.services.ads.operation;

import com.unity3d.services.core.configuration.Configuration;

import org.json.JSONObject;

public class OperationTestUtilities {
	static Configuration createConfigurationWithWebviewTimeout(int timeoutLengthInMilliseconds) {
		Configuration configuration = null;
		JSONObject json = new JSONObject();
		try {
			json.put("url", "fake-url");
			json.put("hash",  "fake-hash");
			json.put("version", "fake-version");
			json.put("wto", timeoutLengthInMilliseconds);
			configuration = new Configuration(json);
		} catch (Exception e) {
		}

		return configuration;
	}
	static Configuration createConfigurationWithLoadTimeout(int timeoutLengthInMilliseconds) {
		Configuration configuration = null;
		JSONObject json = new JSONObject();
		try {
			json.put("url", "fake-url");
			json.put("hash",  "fake-hash");
			json.put("version", "fake-version");
			json.put("lto", timeoutLengthInMilliseconds);
			configuration = new Configuration(json);
		} catch (Exception e) {
		}

		return configuration;
	}

	static Configuration createConfigurationWithShowTimeout(int timeoutLengthInMilliseconds) {
		Configuration configuration = null;
		JSONObject json = new JSONObject();
		try {
			json.put("url", "fake-url");
			json.put("hash",  "fake-hash");
			json.put("version", "fake-version");
			json.put("sto", timeoutLengthInMilliseconds);
			configuration = new Configuration(json);
		} catch (Exception e) {
		}

		return configuration;
	}
}
