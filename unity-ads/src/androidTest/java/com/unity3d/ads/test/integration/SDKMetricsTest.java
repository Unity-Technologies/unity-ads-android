package com.unity3d.ads.test.integration;

import static org.junit.Assert.assertNotNull;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.di.IServicesRegistry;
import com.unity3d.services.core.di.ServiceProvider;
import com.unity3d.services.core.domain.ISDKDispatchers;
import com.unity3d.services.core.properties.InitializationStatusReader;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;
import com.unity3d.services.core.request.metrics.Metric;
import com.unity3d.services.core.request.metrics.MetricSender;
import com.unity3d.services.core.request.metrics.SDKMetrics;

import kotlinx.coroutines.Dispatchers;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(MockitoJUnitRunner.class)
public class SDKMetricsTest {
	@Mock
	InitializationStatusReader _initStatusReaderMock;

	@Mock
	Configuration _configurationMock;

	public MetricSender _metricSenderMock;

	private final HashMap<String, String> TEST_STATE_TAGS = new HashMap<String, String> (){{
		put("state", "initialized");
	}};

	@Before
	public void setup() throws NoSuchFieldException, IllegalAccessException {
		// Ensure internal SDKMetric state is reset.
		Field configurationIsSetField = SDKMetrics.class.getDeclaredField("_configurationIsSet");
		configurationIsSetField.setAccessible(true);
		configurationIsSetField.set(new Object(), new AtomicBoolean(false));
		IServicesRegistry registry = Mockito.mock(IServicesRegistry.class);
		Field serviceRegistry = ServiceProvider.class.getDeclaredField("serviceRegistry");
		serviceRegistry.setAccessible(true);
		serviceRegistry.set(ServiceProvider.class, registry);
		Mockito.doAnswer(invocation -> {
			Class jObject = kotlin.jvm.JvmClassMappingKt.getJavaClass(invocation.getArgument(1));
			if (jObject.getName().contains("ISDKDispatchers")) {
				ISDKDispatchers dispatchers = Mockito.mock(ISDKDispatchers.class);
				Mockito.when(dispatchers.getIo()).thenReturn(Dispatchers.getIO());
				return dispatchers;
			}
			return Mockito.mock(jObject);
		}).when(registry).getService(Mockito.any(), Mockito.any());
		_metricSenderMock = Mockito.spy(new MetricSender(_configurationMock, _initStatusReaderMock));
	}

	@Test
	public void testGetInstance() {
		assertNotNull("getInstance should never return null", SDKMetrics.getInstance());
	}

	@Test
	public void testUsingEmptyEvents() {
		SDKMetricsSender metrics = SDKMetrics.getInstance();
		metrics.sendEvent("");
	}

	@Test
	public void testUsingInstanceAfterShutdown() {
		SDKMetricsSender metrics = SDKMetrics.getInstance();
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

	@Test
	public void testSendMetricWithInitStateNotDiscardedWithNullTags() {
		final ArgumentCaptor<Metric> metricCapture = ArgumentCaptor.forClass(Metric.class);
		Mockito.when(_initStatusReaderMock.getInitializationStateString(Mockito.<SdkProperties.InitializationState>any())).thenReturn("initialized");

		_metricSenderMock.sendMetricWithInitState(new Metric("native_test", 14));

		Mockito.verify(_metricSenderMock).sendMetric(metricCapture.capture());
		Assert.assertEquals(TEST_STATE_TAGS, metricCapture.getValue().getTags());
	}

	@Test
	public void testSendMetricWithInitStateNotDiscardedWithNonNullTags() {
		final ArgumentCaptor<Metric> metricCapture = ArgumentCaptor.forClass(Metric.class);
		Mockito.when(_initStatusReaderMock.getInitializationStateString(Mockito.<SdkProperties.InitializationState>any())).thenReturn("initialized");

		_metricSenderMock.sendMetricWithInitState(new Metric("native_test", 14, TEST_STATE_TAGS));

		Mockito.verify(_metricSenderMock).sendMetric(metricCapture.capture());
		Assert.assertEquals(TEST_STATE_TAGS, metricCapture.getValue().getTags());
	}
}
