package com.unity3d.ads.test.integration;

import static org.junit.Assert.assertNotNull;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.request.metrics.ISDKMetrics;
import com.unity3d.services.core.request.metrics.MetricSender;
import com.unity3d.services.core.request.metrics.SDKMetrics;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

public class SDKMetricsTest {
	@Before
	public void setup() throws NoSuchFieldException, IllegalAccessException {
		// Ensure internal SDKMetric state is reset.
		Field configurationIsSetField = SDKMetrics.class.getDeclaredField("_configurationIsSet");
		configurationIsSetField.setAccessible(true);
		configurationIsSetField.set(new Object(), new AtomicBoolean(false));
	}

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
	public void testSettingMsr100Then0() throws NoSuchFieldException, IllegalAccessException {
		validateAndTestChangingSampleRate("testUrl", 100.0, 0.0, MetricSender.class);
	}

	@Test
	public void testSettingMsr0Then100() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		Class expectedClass = Class.forName("com.unity3d.services.core.request.metrics.SDKMetrics$NullInstance");
		validateAndTestChangingSampleRate("testUrl", 0.0, 100.0, expectedClass);
	}

	@Test
	public void testSettingMsrWithNullUrl() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		Class expectedClass = Class.forName("com.unity3d.services.core.request.metrics.SDKMetrics$NullInstance");
		validateAndTestChangingSampleRate(null, 100.0, 100.0, expectedClass);
	}

	@Test
	public void testSettingMsrWithEmptyUrl() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
		Class expectedClass = Class.forName("com.unity3d.services.core.request.metrics.SDKMetrics$NullInstance");
		validateAndTestChangingSampleRate("", 100.0, 100.0, expectedClass);
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

	private void validateAndTestChangingSampleRate(String metricsUrl, double oldMsr, double newMsr, Class expectedMetricsClass) throws NoSuchFieldException, IllegalAccessException {
		Configuration mockConfiguration = Mockito.mock(Configuration.class);
		Mockito.when(mockConfiguration.getMetricsUrl()).thenReturn(metricsUrl);
		Mockito.when(mockConfiguration.getMetricSampleRate()).thenReturn(oldMsr);
		SDKMetrics.setConfiguration(mockConfiguration);
		SDKMetrics.getInstance();
		Field instanceField = SDKMetrics.class.getDeclaredField("_instance");
		instanceField.setAccessible(true);
		Object instanceFieldObj = instanceField.get(ISDKMetrics.class);
		Assert.assertEquals(expectedMetricsClass, instanceFieldObj.getClass());
		Mockito.when(mockConfiguration.getMetricSampleRate()).thenReturn(newMsr);
		SDKMetrics.setConfiguration(mockConfiguration);
		instanceFieldObj = instanceField.get(ISDKMetrics.class);
		Assert.assertEquals(expectedMetricsClass, instanceFieldObj.getClass());
	}
}
