package com.unity3d.ads.test.instrumentation.services.core.webview;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.webview.WebViewUrlBuilder;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
public class WebViewUrlBuilderTest {
	private final static String TEST_BASE_URL = "file://testbase";

	@Test
	public void testWebViewUrlBuilder() throws JSONException {
		Configuration configMock = Mockito.mock(Configuration.class);
		Mockito.when(configMock.getExperiments()).thenReturn(new Experiments(new JSONObject("{\"fff\":true}")));
		WebViewUrlBuilder webViewUrlBuilder = new WebViewUrlBuilder(TEST_BASE_URL, configMock);
		String result = webViewUrlBuilder.getUrlWithQueryString();
		Assert.assertEquals(TEST_BASE_URL + "?platform=android&experiments=%7B%22fff%22%3Atrue%7D", result);
	}

	@Test
	public void testWebViewUrlBuilderWithoutExperiments() {
		Configuration configMock = Mockito.mock(Configuration.class);
		Mockito.when(configMock.getExperiments()).thenReturn(null);
		WebViewUrlBuilder webViewUrlBuilder = new WebViewUrlBuilder(TEST_BASE_URL, configMock);
		String result = webViewUrlBuilder.getUrlWithQueryString();
		Assert.assertEquals(TEST_BASE_URL + "?platform=android", result);
	}

	@Test
	public void testWebViewUrlBuilderWithExperimentsButFeatureDisabled() throws JSONException {
		Configuration configMock = Mockito.mock(Configuration.class);
		Mockito.when(configMock.getExperiments()).thenReturn(new Experiments(new JSONObject("{\"fff\":false, \"tsi\":true}")));
		WebViewUrlBuilder webViewUrlBuilder = new WebViewUrlBuilder(TEST_BASE_URL, configMock);
		String result = webViewUrlBuilder.getUrlWithQueryString();
		Assert.assertEquals(TEST_BASE_URL + "?platform=android", result);
	}

	@Test
	public void testWebViewUrlBuilderWithWebviewOrigin() {
		Configuration configMock = Mockito.mock(Configuration.class);
		Mockito.when(configMock.getWebViewUrl()).thenReturn("someplace.html");
		WebViewUrlBuilder webViewUrlBuilder = new WebViewUrlBuilder(TEST_BASE_URL, configMock);
		String result = webViewUrlBuilder.getUrlWithQueryString();
		Assert.assertEquals(TEST_BASE_URL + "?platform=android&origin=someplace.html", result);
	}

	@Test
	public void testWebViewUrlBuilderWithWebviewVersion() {
		Configuration configMock = Mockito.mock(Configuration.class);
		Mockito.when(configMock.getWebViewVersion()).thenReturn("1.0");
		WebViewUrlBuilder webViewUrlBuilder = new WebViewUrlBuilder(TEST_BASE_URL, configMock);
		String result = webViewUrlBuilder.getUrlWithQueryString();
		Assert.assertEquals(TEST_BASE_URL + "?platform=android&version=1.0", result);
	}
}
