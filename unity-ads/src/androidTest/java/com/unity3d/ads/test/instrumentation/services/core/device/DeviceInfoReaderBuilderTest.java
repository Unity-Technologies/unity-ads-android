package com.unity3d.ads.test.instrumentation.services.core.device;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_KEY;

import android.app.Application;

import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.configuration.PrivacyConfig;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.reader.DeviceInfoReaderBuilder;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.lifecycle.CachedLifecycle;
import com.unity3d.services.core.properties.ClientProperties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoReaderBuilderTest {
	private static String TEST_GAME_ID = "123456";

	@Mock
	Experiments _experimentsMock;

	@Mock
	Configuration _configurationMock;

	@Mock
	ConfigurationReader _configReaderMock;

	@Mock
	PrivacyConfig _privacyConfigMock;

	@Mock
	PrivacyConfigStorage _privacyConfigStorageMock;

	@Before
	public void setup() {
		ClientProperties.setApplication((Application) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext());
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
		ClientProperties.setGameId(TEST_GAME_ID);
		CachedLifecycle.register();
		Mockito.when(_privacyConfigStorageMock.getPrivacyConfig()).thenReturn(_privacyConfigMock);
		Mockito.when(_experimentsMock.isPrivacyRequestEnabled()).thenReturn(true);
		Mockito.when(_configurationMock.getExperiments()).thenReturn(_experimentsMock);
		Mockito.when(_configReaderMock.getCurrentConfiguration()).thenReturn(_configurationMock);
	}

	@Test
	public void testDeviceInfoReaderBuilderWithPrivacy() {
		Mockito.when(_privacyConfigMock.allowedToSendPii()).thenReturn(true);
		DeviceInfoReaderBuilder deviceInfoReaderBuilder = new DeviceInfoReaderBuilder(_configReaderMock, _privacyConfigStorageMock);
		IDeviceInfoReader deviceInfoReader = deviceInfoReaderBuilder.build();
		Map<String, Object> deviceInfoData = deviceInfoReader.getDeviceInfoData();
		Assert.assertNotNull(deviceInfoData.get(USER_NON_BEHAVIORAL_KEY));
	}
}
