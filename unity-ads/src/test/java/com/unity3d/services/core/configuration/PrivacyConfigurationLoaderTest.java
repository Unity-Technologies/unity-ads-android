package com.unity3d.services.core.configuration;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

import com.unity3d.services.core.extensions.AbortRetryException;
import com.unity3d.services.core.network.core.HttpClient;
import com.unity3d.services.core.network.model.HttpRequest;
import com.unity3d.services.core.network.model.HttpResponse;
import com.unity3d.services.core.request.WebRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

@RunWith(MockitoJUnitRunner.class)
public class PrivacyConfigurationLoaderTest {
	@Mock
	IConfigurationLoader _configLoaderMock;

	@Mock
	ConfigurationRequestFactory _configRequestFactoryMock;

	@Mock
	IConfigurationLoaderListener _configLoaderListener;

	@Mock
	PrivacyConfig _privacyConfigMock;

	@Mock
	PrivacyConfigStorage _privacyConfigStorageMock;

	@Mock
	WebRequest _webRequestMock;

	@Before
	public void setup() throws Exception {
		Mockito.when(_privacyConfigMock.getPrivacyStatus()).thenReturn(PrivacyConfigStatus.UNKNOWN);
		Mockito.when(_privacyConfigStorageMock.getPrivacyConfig()).thenReturn(_privacyConfigMock);
		Mockito.when(_webRequestMock.getUrl()).thenReturn(new URL("https://www.unity3d.com"));
		Mockito.when(_webRequestMock.getRequestType()).thenReturn("GET");
		Mockito.when(_configRequestFactoryMock.getWebRequest()).thenReturn(_webRequestMock);
		doAnswer((Answer<Object>) invocation -> {
			_configLoaderListener.onSuccess(new Configuration());
			return null;
		}).when(_configLoaderMock).loadConfiguration(_configLoaderListener);
	}

	@Test
	public void testPrivacyConfigLoaderAllowed() throws Exception {
		privacyConfigLoaderValidation(true);
	}

	@Test
	public void testPrivacyConfigLoaderNotAllowed() throws Exception {
		privacyConfigLoaderValidation(false);
	}

	@Test
	public void testPrivacyConfigLoaderWhenConfigRequestThrows() throws Exception {
		Mockito.when(_configRequestFactoryMock.getWebRequest()).thenThrow(new MalformedURLException());

		PrivacyConfigurationLoader privacyConfigurationLoader = new PrivacyConfigurationLoader(_configLoaderMock, _configRequestFactoryMock, _privacyConfigStorageMock);
		privacyConfigurationLoader.loadConfiguration(_configLoaderListener);

		Mockito.verify(_configLoaderListener, times(1)).onSuccess(Mockito.any(Configuration.class));
		Mockito.verify(_configLoaderListener, times(0)).onError(Mockito.anyString());
		Mockito.verify(_configLoaderMock, times(1)).loadConfiguration(_configLoaderListener);
	}

	@Test
	public void testPrivacyConfigLoaderSkippedIfPrivacyAlreadySet() throws Exception {
		Mockito.when(_privacyConfigMock.getPrivacyStatus()).thenReturn(PrivacyConfigStatus.ALLOWED);

		PrivacyConfigurationLoader privacyConfigurationLoader = new PrivacyConfigurationLoader(_configLoaderMock, _configRequestFactoryMock, _privacyConfigStorageMock);
		privacyConfigurationLoader.loadConfiguration(_configLoaderListener);

		Mockito.verify(_privacyConfigStorageMock, times(0)).setPrivacyConfig(Mockito.<PrivacyConfig>any());
		Mockito.verify(_configRequestFactoryMock, times(0)).getWebRequest();
		Mockito.verify(_configLoaderListener, times(1)).onSuccess(Mockito.any(Configuration.class));
		Mockito.verify(_configLoaderListener, times(0)).onError(Mockito.anyString());
		Mockito.verify(_configLoaderMock, times(1)).loadConfiguration(_configLoaderListener);
	}

	@Test
	public void testPrivacyConfigLoaderWhenConfigRequestInvalidJson() throws Exception {
		PrivacyConfigurationLoader privacyConfigurationLoader = new PrivacyConfigurationLoader(_configLoaderMock, _configRequestFactoryMock, _privacyConfigStorageMock);
		privacyConfigurationLoader.loadConfiguration(_configLoaderListener);

		Mockito.verify(_configLoaderListener, times(1)).onSuccess(Mockito.any(Configuration.class));
		Mockito.verify(_configLoaderListener, times(0)).onError(Mockito.anyString());
		Mockito.verify(_configLoaderMock, times(1)).loadConfiguration(_configLoaderListener);
	}

	@Test(expected = AbortRetryException.class)
	public void testPrivacyConfigLoaderWhenConfigRequestReturnsLocked() throws Exception {
		PrivacyConfigurationLoader privacyConfigurationLoader = new PrivacyConfigurationLoader(_configLoaderMock, _configRequestFactoryMock, _privacyConfigStorageMock);
		HttpClient httpClient = Mockito.mock(HttpClient.class);
		Mockito.when(httpClient.executeBlocking(Mockito.any(HttpRequest.class))).thenReturn(new HttpResponse("{}", 423));

		Field field = PrivacyConfigurationLoader.class.getDeclaredField("_httpClient");
		field.setAccessible(true);
		field.set(privacyConfigurationLoader, httpClient);

		privacyConfigurationLoader.loadConfiguration(_configLoaderListener);
	}

	@Test
	public void testPrivacyConfigLoaderWhenConfigRequestReturnNotOk() throws Exception {
		privacyConfigLoaderValidation(true, false, 400);
	}

	private void privacyConfigLoaderValidation(final boolean pasResponse, final boolean expectedIsAllowed, final int statusCode) throws Exception {
		HttpClient httpClient = Mockito.mock(HttpClient.class);
		Mockito.when(httpClient.executeBlocking(Mockito.any(HttpRequest.class))).thenReturn(new HttpResponse("{\"pas\":" + pasResponse + "}", statusCode));

		doAnswer((Answer<Object>) invocation -> {
			PrivacyConfig privacyConfig = invocation.getArgument(0);
			Assert.assertEquals(expectedIsAllowed, privacyConfig.allowedToSendPii());
			return null;
		}).when(_privacyConfigStorageMock).setPrivacyConfig(Mockito.any());

		PrivacyConfigurationLoader privacyConfigurationLoader = new PrivacyConfigurationLoader(_configLoaderMock, _configRequestFactoryMock, _privacyConfigStorageMock);
		Field field = PrivacyConfigurationLoader.class.getDeclaredField("_httpClient");
		field.setAccessible(true);
		field.set(privacyConfigurationLoader, httpClient);

		privacyConfigurationLoader.loadConfiguration(_configLoaderListener);

		Mockito.verify(_configLoaderMock, times(1)).loadConfiguration(_configLoaderListener);
		Mockito.verify(_configLoaderListener, times(1)).onSuccess(Mockito.any(Configuration.class));

	}

	private void privacyConfigLoaderValidation(final boolean isAllowed) throws Exception {
		privacyConfigLoaderValidation(isAllowed, isAllowed, 200);
	}
}
