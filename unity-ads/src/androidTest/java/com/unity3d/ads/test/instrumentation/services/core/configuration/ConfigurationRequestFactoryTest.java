package com.unity3d.ads.test.instrumentation.services.core.configuration;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ConfigurationRequestFactory;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.device.reader.DeviceInfoReaderBuilder;
import com.unity3d.services.core.device.reader.IDeviceInfoDataContainer;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.request.WebRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationRequestFactoryTest {

	@Mock
	Experiments _experimentsMock;

	@Mock
	Configuration _configurationMock;

	@Mock
	IDeviceInfoDataContainer _deviceInfoDataContainerMock;

	static final byte[] DUMMY_DEVICE_DATA = "{\"testKey\":\"testData\"}".getBytes();

	static final String CONFIG_URL = "http://configurl/";

	@Before
	public void setup() {
		Mockito.when(_deviceInfoDataContainerMock.getDeviceData()).thenReturn(DUMMY_DEVICE_DATA);
		Mockito.when(_configurationMock.getExperiments()).thenReturn(_experimentsMock);
		Mockito.when(_configurationMock.getConfigUrl()).thenReturn(CONFIG_URL);
	}

	@Test
	public void testConfigurationRequestFactoryGetIfTsiEnabled() throws MalformedURLException {
		Mockito.when(_experimentsMock.isTwoStageInitializationEnabled()).thenReturn(true);
		ConfigurationRequestFactory configurationRequestFactory = new ConfigurationRequestFactory(_configurationMock, _deviceInfoDataContainerMock);
		WebRequest webRequest = configurationRequestFactory.getWebRequest();
		Assert.assertEquals("POST", webRequest.getRequestType());
		Map<String, List<String>> headers = webRequest.getHeaders();
		Assert.assertEquals("gzip", headers.get("Content-Encoding").get(0));
		Assert.assertEquals(DUMMY_DEVICE_DATA, webRequest.getBody());
	}

	@Test
	public void testConfigurationRequestFactoryGetIfTsiDisabled() throws MalformedURLException {
		Mockito.when(_experimentsMock.isTwoStageInitializationEnabled()).thenReturn(false);
		ConfigurationRequestFactory configurationRequestFactory = new ConfigurationRequestFactory(_configurationMock, _deviceInfoDataContainerMock);
		WebRequest webRequest = configurationRequestFactory.getWebRequest();
		validateGetRequest(webRequest);
	}

	@Test
	public void testConfigurationRequestFactoryGetNullExperiments() throws MalformedURLException {
		Mockito.when(_configurationMock.getExperiments()).thenReturn(null);
		ConfigurationRequestFactory configurationRequestFactory = new ConfigurationRequestFactory(_configurationMock, _deviceInfoDataContainerMock);
		WebRequest webRequest = configurationRequestFactory.getWebRequest();
		validateGetRequest(webRequest);
	}

	private void validateGetRequest(WebRequest webRequest) {
		Assert.assertEquals("GET", webRequest.getRequestType());
		Assert.assertTrue("base url missing from query", webRequest.getUrl().toString().contains(CONFIG_URL));
		Assert.assertTrue("ts missing from query", webRequest.getQuery().contains("ts"));
		Assert.assertTrue("sdkVersion missing from query", webRequest.getQuery().contains("sdkVersion"));
		Assert.assertTrue("sdkVersionName missing from query", webRequest.getQuery().contains("sdkVersionName"));
		Assert.assertTrue("gameId missing from query", webRequest.getQuery().contains("gameId"));
	}
}
