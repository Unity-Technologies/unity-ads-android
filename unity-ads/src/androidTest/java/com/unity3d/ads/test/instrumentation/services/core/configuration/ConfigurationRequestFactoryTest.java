package com.unity3d.ads.test.instrumentation.services.core.configuration;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ConfigurationRequestFactory;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.request.WebRequest;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationRequestFactoryTest {

	@Mock
	Experiments _experimentsMock;

	@Mock
	Configuration _configurationMock;

	@Mock
	IDeviceInfoReader _deviceInfoReaderMock;

	static final String CONFIG_URL = "http://configurl/";

	@Test
	public void testConfigurationRequestFactoryPost() throws MalformedURLException {
		Mockito.when(_experimentsMock.isTwoStageInitializationEnabled()).thenReturn(true);
		Mockito.when(_experimentsMock.isPOSTMethodInConfigRequestEnabled()).thenReturn(true);
		Mockito.when(_configurationMock.getExperiments()).thenReturn(_experimentsMock);

		ConfigurationRequestFactory configurationRequestFactory = new ConfigurationRequestFactory(_configurationMock, _deviceInfoReaderMock, CONFIG_URL);
		WebRequest webRequest = configurationRequestFactory.getWebRequest();
		Assert.assertEquals("POST", webRequest.getRequestType());
		Map<String, List<String>> headers = webRequest.getHeaders();
		Assert.assertEquals("gzip", headers.get("Content-Encoding").get(0));
	}

	@Test
	public void testConfigurationRequestFactoryGetIfTsiDisabled() throws MalformedURLException {
		Mockito.when(_experimentsMock.isTwoStageInitializationEnabled()).thenReturn(false);
		Mockito.when(_experimentsMock.isPOSTMethodInConfigRequestEnabled()).thenReturn(true);
		Mockito.when(_configurationMock.getExperiments()).thenReturn(_experimentsMock);

		ConfigurationRequestFactory configurationRequestFactory = new ConfigurationRequestFactory(_configurationMock, _deviceInfoReaderMock, CONFIG_URL);
		WebRequest webRequest = configurationRequestFactory.getWebRequest();
		Assert.assertEquals("GET", webRequest.getRequestType());
		Assert.assertTrue("ts missing from query", webRequest.getQuery().contains("ts"));
		Assert.assertTrue("sdkVersion missing from query", webRequest.getQuery().contains("sdkVersion"));
		Assert.assertTrue("sdkVersionName missing from query", webRequest.getQuery().contains("sdkVersionName"));
		Assert.assertTrue("gameId missing from query", webRequest.getQuery().contains("gameId"));
	}

	@Test
	public void testConfigurationRequestFactoryGetIfTsiEnabled() throws MalformedURLException {
		Mockito.when(_experimentsMock.isTwoStageInitializationEnabled()).thenReturn(true);
		Mockito.when(_experimentsMock.isPOSTMethodInConfigRequestEnabled()).thenReturn(false);
		Mockito.when(_configurationMock.getExperiments()).thenReturn(_experimentsMock);

		ConfigurationRequestFactory configurationRequestFactory = new ConfigurationRequestFactory(_configurationMock, _deviceInfoReaderMock, CONFIG_URL);
		WebRequest webRequest = configurationRequestFactory.getWebRequest();
		Assert.assertEquals("GET", webRequest.getRequestType());
		Assert.assertEquals("c=H4sIAAAAAAAAAKuuBQBDv6ajAgAAAA%3D%3D", webRequest.getQuery());
	}

	@Test
	public void testConfigurationRequestFactoryGetIfPostDisabled() throws MalformedURLException {
		Mockito.when(_experimentsMock.isTwoStageInitializationEnabled()).thenReturn(true);
		Mockito.when(_experimentsMock.isPOSTMethodInConfigRequestEnabled()).thenReturn(false);
		Mockito.when(_configurationMock.getExperiments()).thenReturn(_experimentsMock);

		ConfigurationRequestFactory configurationRequestFactory = new ConfigurationRequestFactory(_configurationMock, _deviceInfoReaderMock, CONFIG_URL);
		WebRequest webRequest = configurationRequestFactory.getWebRequest();
		Assert.assertEquals("GET", webRequest.getRequestType());
	}

	@Test
	public void testConfigurationRequestFactoryGetNullExperiments() throws MalformedURLException {
		Mockito.when(_configurationMock.getExperiments()).thenReturn(null);

		ConfigurationRequestFactory configurationRequestFactory = new ConfigurationRequestFactory(_configurationMock, _deviceInfoReaderMock, CONFIG_URL);
		WebRequest webRequest = configurationRequestFactory.getWebRequest();
		Assert.assertEquals("GET", webRequest.getRequestType());
	}
}
