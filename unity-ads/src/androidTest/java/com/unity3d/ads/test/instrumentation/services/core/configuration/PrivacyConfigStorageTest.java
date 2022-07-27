package com.unity3d.ads.test.instrumentation.services.core.configuration;

import com.unity3d.services.core.configuration.PrivacyConfig;
import com.unity3d.services.core.configuration.PrivacyConfigStatus;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.misc.IObserver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PrivacyConfigStorageTest {
	@Mock
	PrivacyConfig _privacyConfigMock;

	@Mock
	IObserver<PrivacyConfig> _privacyConfigObserver;

	@Before
	public void setup() throws NoSuchFieldException, IllegalAccessException {
		Mockito.when(_privacyConfigMock.getPrivacyStatus()).thenReturn(PrivacyConfigStatus.ALLOWED);
	}

	@Test
	public void testPrivacyConfigStorageSingleObserver() {
		PrivacyConfigStorage.getInstance().registerObserver(_privacyConfigObserver);
		PrivacyConfigStorage.getInstance().setPrivacyConfig(_privacyConfigMock);
		Mockito.verify(_privacyConfigObserver, Mockito.times(1)).updated(_privacyConfigMock);
	}

	@Test
	public void testPrivacyConfigStorageSingleObserverTwice() {
		PrivacyConfigStorage.getInstance().registerObserver(_privacyConfigObserver);
		PrivacyConfigStorage.getInstance().registerObserver(_privacyConfigObserver);
		PrivacyConfigStorage.getInstance().setPrivacyConfig(_privacyConfigMock);
		Mockito.verify(_privacyConfigObserver, Mockito.times(1)).updated(_privacyConfigMock);
	}

	@Test
	public void testPrivacyConfigStorageSingleObserverAfterSetPrivacy() {
		PrivacyConfigStorage.getInstance().setPrivacyConfig(_privacyConfigMock);
		PrivacyConfigStorage.getInstance().registerObserver(_privacyConfigObserver);
		Mockito.verify(_privacyConfigObserver, Mockito.times(1)).updated(_privacyConfigMock);
	}

	@Test
	public void testPrivacyConfigStorageTwoObserver() {
		IObserver<PrivacyConfig> privacyConfigSecondObserverMock = Mockito.mock(IObserver.class);
		PrivacyConfigStorage.getInstance().registerObserver(_privacyConfigObserver);
		PrivacyConfigStorage.getInstance().registerObserver(privacyConfigSecondObserverMock);
		PrivacyConfigStorage.getInstance().setPrivacyConfig(_privacyConfigMock);
		Mockito.verify(_privacyConfigObserver, Mockito.times(1)).updated(_privacyConfigMock);
		Mockito.verify(privacyConfigSecondObserverMock, Mockito.times(1)).updated(_privacyConfigMock);
	}

	@Test
	public void testPrivacyConfigStorageSingleObserverRegisterUnregister() {
		PrivacyConfigStorage.getInstance().registerObserver(_privacyConfigObserver);
		PrivacyConfigStorage.getInstance().unregisterObserver(_privacyConfigObserver);
		PrivacyConfigStorage.getInstance().setPrivacyConfig(_privacyConfigMock);
		Mockito.verify(_privacyConfigObserver, Mockito.times(0)).updated(_privacyConfigMock);
	}

	@Test
	public void testPrivacyConfigStorageSingleObserverUnregisterUnknown() {
		PrivacyConfigStorage.getInstance().unregisterObserver(_privacyConfigObserver);
		Mockito.verify(_privacyConfigObserver, Mockito.times(0)).updated(_privacyConfigMock);
	}
}
