package com.unity3d.ads.test.instrumentation.services.core.device;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.AUID_ID_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.SESSION_ID_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_KEY;

import android.app.Application;

import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.configuration.PrivacyConfig;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.reader.builder.DeviceInfoReaderBuilder;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.device.reader.IGameSessionIdReader;
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

	@Mock
	IGameSessionIdReader _gameSessionIdReaderMock;

	@Before
	public void setup() {
		ClientProperties.setApplication((Application) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext());
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
		ClientProperties.setGameId(TEST_GAME_ID);
		CachedLifecycle.register();
		Mockito.when(_privacyConfigStorageMock.getPrivacyConfig()).thenReturn(_privacyConfigMock);
		Mockito.when(_configurationMock.getExperiments()).thenReturn(_experimentsMock);
		Mockito.when(_configReaderMock.getCurrentConfiguration()).thenReturn(_configurationMock);
	}

	@Test
	public void testDeviceInfoReaderBuilderWithoutSnb() {
		Mockito.when(_privacyConfigMock.allowedToSendPii()).thenReturn(true);
		Mockito.when(_privacyConfigMock.shouldSendNonBehavioral()).thenReturn(false);
		DeviceInfoReaderBuilder deviceInfoReaderBuilder = new DeviceInfoReaderBuilder(_configReaderMock, _privacyConfigStorageMock, _gameSessionIdReaderMock);
		IDeviceInfoReader deviceInfoReader = deviceInfoReaderBuilder.build();
		Map<String, Object> deviceInfoData = deviceInfoReader.getDeviceInfoData();
		Assert.assertNull(deviceInfoData.get(USER_NON_BEHAVIORAL_KEY));
		Assert.assertNotNull(deviceInfoData.get(SESSION_ID_KEY));
		Assert.assertNotNull(deviceInfoData.get(AUID_ID_KEY));
	}

	@Test
	public void testDeviceInfoReaderBuilderWithSnb() {
		Mockito.when(_privacyConfigMock.allowedToSendPii()).thenReturn(true);
		Mockito.when(_privacyConfigMock.shouldSendNonBehavioral()).thenReturn(true);
		DeviceInfoReaderBuilder deviceInfoReaderBuilder = new DeviceInfoReaderBuilder(_configReaderMock, _privacyConfigStorageMock, _gameSessionIdReaderMock);
		IDeviceInfoReader deviceInfoReader = deviceInfoReaderBuilder.build();
		Map<String, Object> deviceInfoData = deviceInfoReader.getDeviceInfoData();
		Assert.assertNotNull(deviceInfoData.get(USER_NON_BEHAVIORAL_KEY));
		Assert.assertNotNull(deviceInfoData.get(SESSION_ID_KEY));
		Assert.assertNotNull(deviceInfoData.get(AUID_ID_KEY));
	}
}
