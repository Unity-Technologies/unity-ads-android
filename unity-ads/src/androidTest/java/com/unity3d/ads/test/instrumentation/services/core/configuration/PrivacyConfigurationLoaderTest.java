package com.unity3d.ads.test.instrumentation.services.core.configuration;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ConfigurationRequestFactory;
import com.unity3d.services.core.configuration.IConfigurationLoader;
import com.unity3d.services.core.configuration.IConfigurationLoaderListener;
import com.unity3d.services.core.configuration.PrivacyConfig;
import com.unity3d.services.core.configuration.PrivacyConfigStatus;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.configuration.PrivacyConfigurationLoader;
import com.unity3d.services.core.request.WebRequest;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.net.MalformedURLException;

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
		Mockito.when(_webRequestMock.getResponseCode()).thenReturn(200);
		Mockito.when(_privacyConfigMock.getPrivacyStatus()).thenReturn(PrivacyConfigStatus.UNKNOWN);
		Mockito.when(_privacyConfigStorageMock.getPrivacyConfig()).thenReturn(_privacyConfigMock);
		Mockito.when(_configRequestFactoryMock.getWebRequest()).thenReturn(_webRequestMock);
		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				_configLoaderListener.onSuccess(new Configuration());
				return null;
			}
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
		Mockito.when(_webRequestMock.makeRequest()).thenReturn("{invalid}");

		PrivacyConfigurationLoader privacyConfigurationLoader = new PrivacyConfigurationLoader(_configLoaderMock, _configRequestFactoryMock, _privacyConfigStorageMock);
		privacyConfigurationLoader.loadConfiguration(_configLoaderListener);

		Mockito.verify(_configLoaderListener, times(1)).onSuccess(Mockito.any(Configuration.class));
		Mockito.verify(_configLoaderListener, times(0)).onError(Mockito.anyString());
		Mockito.verify(_configLoaderMock, times(1)).loadConfiguration(_configLoaderListener);
	}

	@Test
	public void testPrivacyConfigLoaderWhenConfigRequestReturnNotOk() throws Exception {
		Mockito.when(_webRequestMock.getResponseCode()).thenReturn(400);
		privacyConfigLoaderValidation(true, false);
	}

	private void privacyConfigLoaderValidation(final boolean pasReponse, final boolean expectedIsAllowed) throws Exception {
		Mockito.when(_webRequestMock.makeRequest()).thenReturn("{\"pas\":" + pasReponse + "}");

		doAnswer(new Answer<Object>() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				PrivacyConfig privacyConfig = invocation.getArgument(0);
				Assert.assertEquals(expectedIsAllowed, privacyConfig.allowedToSendPii());
				return null;
			}
		}).when(_privacyConfigStorageMock).setPrivacyConfig(Mockito.<PrivacyConfig>any());

		PrivacyConfigurationLoader privacyConfigurationLoader = new PrivacyConfigurationLoader(_configLoaderMock, _configRequestFactoryMock, _privacyConfigStorageMock);
		privacyConfigurationLoader.loadConfiguration(_configLoaderListener);

		Mockito.verify(_configLoaderMock, times(1)).loadConfiguration(_configLoaderListener);
		Mockito.verify(_configLoaderListener, times(1)).onSuccess(Mockito.any(Configuration.class));

	}

	private void privacyConfigLoaderValidation(final boolean isAllowed) throws Exception {
		privacyConfigLoaderValidation(isAllowed, isAllowed);
	}
}
