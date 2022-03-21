package com.unity3d.ads.test.integration;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.request.metrics.ISDKMetrics;
import com.unity3d.services.core.request.metrics.SDKMetrics;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SDKMetricsTest {

	@Test
	public void testGetInstance() {
		assertNotNull("getInstance should never return null", SDKMetrics.getInstance());
	}

	@Test
	public void testUsingNullAndEmptyEvents() {
		ISDKMetrics metrics = SDKMetrics.getInstance();
		metrics.sendEvent(null);
		metrics.sendEvent("");
	}

	@Test
	public void testUsingInstanceAfterShutdown() {
		ISDKMetrics metrics = SDKMetrics.getInstance();
		metrics.sendEvent("test_event");
		SDKMetrics.setConfiguration(new Configuration());
		metrics.sendEvent("test_event_2");
	}

	@Test
	public void testNullConfiguration() {
		SDKMetrics.setConfiguration(null);
	}

	@Test
	public void testEmptyUrlFromConfiguration() throws Exception {
		JSONObject json = new JSONObject();
		json.put("url", "fakeUrl");
		json.put("hash", "fakeHash");
		json.put("murl", "");

		Configuration config = new Configuration(json);
		SDKMetrics.setConfiguration(config);
		SDKMetrics.getInstance().sendEvent("test_event");
	}


	@Test
	public void testMalformedUrlFromConfiguration() throws Exception {
		JSONObject json = new JSONObject();
		json.put("url", "fakeUrl");
		json.put("hash", "fakeHash");
		json.put("murl", "........fakeMalformedUrl");

		Configuration config = new Configuration(json);

		SDKMetrics.setConfiguration(config);
		SDKMetrics.getInstance().sendEvent("test_event");
	}
}
