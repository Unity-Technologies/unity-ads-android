package com.unity3d.services.core.configuration;

import com.unity3d.services.core.device.reader.IDeviceInfoDataContainer;
import com.unity3d.services.core.request.WebRequest;

import org.junit.Assert;
import org.junit.Before;
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
	Configuration _configurationMock;

	@Mock
	IDeviceInfoDataContainer _deviceInfoDataContainerMock;

	static final byte[] DUMMY_DEVICE_DATA = "{\"testKey\":\"testData\"}".getBytes();

	static final String CONFIG_URL = "http://configurl/";

	@Before
	public void setup() {
		Mockito.when(_deviceInfoDataContainerMock.getDeviceData()).thenReturn(DUMMY_DEVICE_DATA);
		Mockito.when(_configurationMock.getConfigUrl()).thenReturn(CONFIG_URL);
	}

	@Test
	public void testConfigurationRequestFactory() throws MalformedURLException {
		ConfigurationRequestFactory configurationRequestFactory = new ConfigurationRequestFactory(_configurationMock, _deviceInfoDataContainerMock);
		WebRequest webRequest = configurationRequestFactory.getWebRequest();
		Assert.assertEquals("POST", webRequest.getRequestType());
		Map<String, List<String>> headers = webRequest.getHeaders();
		Assert.assertEquals("gzip", headers.get("Content-Encoding").get(0));
		Assert.assertEquals(DUMMY_DEVICE_DATA, webRequest.getBody());
	}
}
