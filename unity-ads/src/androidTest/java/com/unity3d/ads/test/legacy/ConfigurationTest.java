package com.unity3d.ads.test.legacy;

import android.support.test.runner.AndroidJUnit4;

import com.unity3d.services.core.configuration.Configuration;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class ConfigurationTest {
	private Configuration config;

	@After
	public void cleanup() {
		config = null;
	}

	private JSONObject getAllConfigDataJSON(String webViewUrl, String hash, String version,
											boolean delayUpdate, int resetTimeout, int maxRetries,
											long retryDelay, double scalingFactor, int connectEventThreshold,
											int maxConnectedEvents, long networkErrorTimeout, int showTimeout,
											int loadTimeout, int noFillTimeout, String sdkVersion, String metricsUrl,
											double metricSampleRate, long webviewCreateTimeout) {
		JSONObject json = new JSONObject();

		try {
			json.put("url", webViewUrl);
			json.put("hash", hash);
			json.put("version", version);
			json.put("dwu", delayUpdate);
			json.put("rwt", resetTimeout);
			json.put("mr", maxRetries);
			json.put("rd", retryDelay);
			json.put("rcf", scalingFactor);
			json.put("cet", connectEventThreshold);
			json.put("mce", maxConnectedEvents);
			json.put("net", networkErrorTimeout);
			json.put("sto", showTimeout);
			json.put("lto", loadTimeout);
			json.put("nft", noFillTimeout);
			json.put("sdkv", sdkVersion);
			json.put("murl", metricsUrl);
			json.put("msr", metricSampleRate);
			json.put("wct", webviewCreateTimeout);
		} catch (Exception e) {
			return json;
		}

		return json;
	}

	@Test
	public void testInitForOptionalParameterFallback() {
		try {
			config = new Configuration();
		} catch (Exception e) {
			Assert.fail();
			return;
		}

		assertNull("Config URL should not be set", config.getConfigUrl());
		assertNull("Webview URL does not equal what is expected", config.getWebViewUrl());
		assertNull("Webview Hash does not equal what is expected", config.getWebViewHash());
		assertNull("Webview Version does not equal what is expected", config.getWebViewVersion());
		assertFalse( "Delay Webview Update URL does not equal what is expected", config.getDelayWebViewUpdate());
		assertEquals("Reset Webapp Timeout does not equal what is expected", 10000, config.getResetWebappTimeout());
		assertEquals("Max retries does not equal what is expected", 6, config.getMaxRetries());
		assertEquals("Retry delay does not equal what is expected", 5000L, config.getRetryDelay());
		assertEquals("Retry scaling factor does not equal what is expected", 2.0d, config.getRetryScalingFactor(), 0);
		assertEquals("Connected Event threshold does not equal what is expected", 10000, config.getConnectedEventThreshold());
		assertEquals("Maximum Connected Events does not equal what is expected", 500, config.getMaximumConnectedEvents());
		assertEquals("Network Error Timeout does not equal what is expected", 60000L, config.getNetworkErrorTimeout());
		assertEquals("Show Timeout does not equal what is expected", 5000, config.getShowTimeout());
		assertEquals("Load Timeout does not equal what is expected", 5000, config.getLoadTimeout());
		assertEquals("No fill Timeout does not equal what is expected",30000, config.getNoFillTimeout());
		assertEquals("SDK Version should not be set", "", config.getSdkVersion());
		assertEquals("Metric URL does not equal what is expected", "", config.getMetricsUrl());
    	assertEquals("Metric Sample Rate does not equal what is expected", 100, config.getMetricSampleRate(), 0);
    	assertEquals("WebviewApp Create Timeout does not equal what is expected", 60000L, config.getWebViewAppCreateTimeout());
	}

	@Test
	public void testInitWithAllOptionalParameters() {
		String webViewUrl = "url.com";
		String hash = "hash";
		String version = "version";
		boolean delayUpdate = true;
		int resetTimeout = 1;
		int maxRetries = 2;
		long retryDelay = 3;
		double scalingFactor = 4;
		int connectEventThreshold = 5;
		int maxConnectedEvents = 6;
		long networkErrorTimeout = 7;
		int showTimeout = 8;
		int loadTimeout = 9;
		int noFillTimeout = 10;
		String sdkVersion = "sdkVersion";
		String metricsUrl = "unity3d.com";
		double metricSampleRate = 22.2;
		long webviewCreateTimeout = 11;

		try {
			config = new Configuration(getAllConfigDataJSON(webViewUrl, hash, version,
				delayUpdate, resetTimeout, maxRetries, retryDelay, scalingFactor, connectEventThreshold,
				maxConnectedEvents, networkErrorTimeout, showTimeout, loadTimeout, noFillTimeout, sdkVersion, metricsUrl,
				metricSampleRate, webviewCreateTimeout));
		} catch (Exception e) {
			Assert.fail();
			return;
		}

		assertNull("Config URL should not be set", config.getConfigUrl());
		assertEquals("Webview URL does not equal what is expected", webViewUrl, config.getWebViewUrl());
		assertEquals("Webview Hash does not equal what is expected", hash, config.getWebViewHash());
		assertEquals("Webview Version does not equal what is expected", version, config.getWebViewVersion());
		assertEquals( "Delay Webview Update URL does not equal what is expected", delayUpdate, config.getDelayWebViewUpdate());
		assertEquals("Reset Webapp Timeout does not equal what is expected", resetTimeout, config.getResetWebappTimeout());
		assertEquals("Max retries does not equal what is expected", maxRetries, config.getMaxRetries());
		assertEquals("Retry delay does not equal what is expected", retryDelay, config.getRetryDelay());
		assertEquals("Retry scaling factor does not equal what is expected", scalingFactor, config.getRetryScalingFactor(), 0);
		assertEquals("Connected Event threshold does not equal what is expected", connectEventThreshold, config.getConnectedEventThreshold());
		assertEquals("Maximum Connected Events does not equal what is expected", maxConnectedEvents, config.getMaximumConnectedEvents());
		assertEquals("Network Error Timeout does not equal what is expected", networkErrorTimeout, config.getNetworkErrorTimeout());
		assertEquals("Show Timeout does not equal what is expected", showTimeout, config.getShowTimeout());
		assertEquals("Load Timeout does not equal what is expected", loadTimeout, config.getLoadTimeout());
		assertEquals("No fill Timeout does not equal what is expected", noFillTimeout, config.getNoFillTimeout());
		assertEquals("SDK Version does not equal what is expected", sdkVersion, config.getSdkVersion());
		assertEquals("Metrics URL does not equal what is expected", metricsUrl, config.getMetricsUrl());
    	assertEquals("Metrics Sample Rate does not equal what is expected", metricSampleRate, config.getMetricSampleRate(), 0);
    	assertEquals("WebviewApp Create Timeout does not equal what is expected", webviewCreateTimeout, config.getWebViewAppCreateTimeout());
	}
}
