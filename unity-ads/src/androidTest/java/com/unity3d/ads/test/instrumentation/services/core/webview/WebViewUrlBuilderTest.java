package com.unity3d.ads.test.instrumentation.services.core.webview;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.webview.WebViewUrlBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

@RunWith(MockitoJUnitRunner.class)
public class WebViewUrlBuilderTest {
	private final static String TEST_BASE_URL = "file://testbase";

	@Mock
	private Configuration _configMock;

	@Before
	public void setup() throws NoSuchFieldException, IllegalAccessException {
		Field configurationIsSetField = SDKMetrics.class.getDeclaredField("_configurationIsSet");
		configurationIsSetField.setAccessible(true);
		configurationIsSetField.set(new Object(), new AtomicBoolean(false));

		Mockito.when(_configMock.getMetricSampleRate()).thenReturn(0.0);
		Mockito.when(_configMock.getMetricsUrl()).thenReturn(TEST_BASE_URL);
		SDKMetrics.setConfiguration(_configMock);

		// Ensure internal SDKMetric state is reset
		configurationIsSetField.set(new Object(), new AtomicBoolean(false));
	}

	@Test
	public void testWebViewUrlBuilder() {
		WebViewUrlBuilder webViewUrlBuilder = new WebViewUrlBuilder(TEST_BASE_URL, _configMock);
		String result = webViewUrlBuilder.getUrlWithQueryString();
		Assert.assertEquals(TEST_BASE_URL + "?platform=android&isNativeCollectingMetrics=false", result);
	}

	@Test
	public void testWebViewUrlBuilderWithoutExperiments() {
		Mockito.when(_configMock.getExperiments()).thenReturn(null);
		WebViewUrlBuilder webViewUrlBuilder = new WebViewUrlBuilder(TEST_BASE_URL, _configMock);
		String result = webViewUrlBuilder.getUrlWithQueryString();
		Assert.assertEquals(TEST_BASE_URL + "?platform=android&isNativeCollectingMetrics=false", result);
	}

	@Test
	public void testWebViewUrlBuilderWithMetricsEnabled() {
		Mockito.when(_configMock.getExperiments()).thenReturn(null);
		Mockito.when(_configMock.getMetricSampleRate()).thenReturn(100.0);
		Mockito.when(_configMock.getMetricsUrl()).thenReturn(TEST_BASE_URL);
		SDKMetrics.setConfiguration(_configMock);
		WebViewUrlBuilder webViewUrlBuilder = new WebViewUrlBuilder(TEST_BASE_URL, _configMock);
		String result = webViewUrlBuilder.getUrlWithQueryString();
		Assert.assertEquals(TEST_BASE_URL + "?platform=android&isNativeCollectingMetrics=true", result);
	}

	@Test
	public void testWebViewUrlBuilderWithWebviewOrigin() {
		Mockito.when(_configMock.getWebViewUrl()).thenReturn("someplace.html");
		WebViewUrlBuilder webViewUrlBuilder = new WebViewUrlBuilder(TEST_BASE_URL, _configMock);
		String result = webViewUrlBuilder.getUrlWithQueryString();
		Assert.assertEquals(TEST_BASE_URL + "?platform=android&origin=someplace.html&isNativeCollectingMetrics=false", result);
	}

	@Test
	public void testWebViewUrlBuilderWithWebviewVersion() {
		Mockito.when(_configMock.getWebViewVersion()).thenReturn("1.0");
		WebViewUrlBuilder webViewUrlBuilder = new WebViewUrlBuilder(TEST_BASE_URL, _configMock);
		String result = webViewUrlBuilder.getUrlWithQueryString();
		Assert.assertEquals(TEST_BASE_URL + "?platform=android&version=1.0&isNativeCollectingMetrics=false", result);
	}
}
