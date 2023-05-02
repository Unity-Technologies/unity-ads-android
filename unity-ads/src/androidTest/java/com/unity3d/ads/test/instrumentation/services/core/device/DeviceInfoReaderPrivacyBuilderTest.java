package com.unity3d.ads.test.instrumentation.services.core.device;

import android.app.Application;

import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.ads.metadata.MetaData;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.configuration.PrivacyConfig;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.Storage;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.device.reader.IGameSessionIdReader;
import com.unity3d.services.core.device.reader.builder.DeviceInfoReaderPrivacyBuilder;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoReaderPrivacyBuilderTest {
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
		Mockito.when(_privacyConfigStorageMock.getPrivacyConfig()).thenReturn(_privacyConfigMock);
		Mockito.when(_configurationMock.getExperiments()).thenReturn(_experimentsMock);
		Mockito.when(_configReaderMock.getCurrentConfiguration()).thenReturn(_configurationMock);
	}

	@After
	public void tearDown() {
		StorageManager.getStorage(StorageManager.StorageType.PUBLIC).clearStorage();
		StorageManager.getStorage(StorageManager.StorageType.PUBLIC).initStorage();
	}

	@Test
	public void testDeviceInfoReaderPrivacyBuilder() {
		DeviceInfoReaderPrivacyBuilder deviceInfoReaderPrivacyBuilder = new DeviceInfoReaderPrivacyBuilder(_configReaderMock, _privacyConfigStorageMock, _gameSessionIdReaderMock);
		IDeviceInfoReader deviceInfoReader = deviceInfoReaderPrivacyBuilder.build();
		Map<String, Object> deviceInfoData = deviceInfoReader.getDeviceInfoData();
		validatePrivacyGenerics(deviceInfoData);
	}

	@Test
	public void testDeviceInfoReaderPrivacyBuilderSettingNonBehavioralTrue() {
		setUserNonBehavioralFlag(true);
		DeviceInfoReaderPrivacyBuilder deviceInfoReaderPrivacyBuilder = new DeviceInfoReaderPrivacyBuilder(_configReaderMock, _privacyConfigStorageMock, _gameSessionIdReaderMock);
		IDeviceInfoReader deviceInfoReader = deviceInfoReaderPrivacyBuilder.build();
		Map<String, Object> deviceInfoData = deviceInfoReader.getDeviceInfoData();
		validatePrivacyGenerics(deviceInfoData);
		Assert.assertTrue((Boolean) deviceInfoData.get("user.nonBehavioral"));
	}

	@Test
	public void testDeviceInfoReaderPrivacyBuilderSettingNonBehavioralFalse() {
		setUserNonBehavioralFlag(false);
		DeviceInfoReaderPrivacyBuilder deviceInfoReaderPrivacyBuilder = new DeviceInfoReaderPrivacyBuilder(_configReaderMock, _privacyConfigStorageMock, _gameSessionIdReaderMock);
		IDeviceInfoReader deviceInfoReader = deviceInfoReaderPrivacyBuilder.build();
		Map<String, Object> deviceInfoData = deviceInfoReader.getDeviceInfoData();
		validatePrivacyGenerics(deviceInfoData);
		Assert.assertFalse((Boolean) deviceInfoData.get("user.nonBehavioral"));
	}

	@Test
	public void testDeviceInfoReaderPrivacyBuilderSettingNonBehavioralMissing() {
		DeviceInfoReaderPrivacyBuilder deviceInfoReaderPrivacyBuilder = new DeviceInfoReaderPrivacyBuilder(_configReaderMock, _privacyConfigStorageMock, _gameSessionIdReaderMock);
		IDeviceInfoReader deviceInfoReader = deviceInfoReaderPrivacyBuilder.build();
		Map<String, Object> deviceInfoData = deviceInfoReader.getDeviceInfoData();
		validatePrivacyGenerics(deviceInfoData);
		Assert.assertNull(deviceInfoData.get("user.nonBehavioral"));
	}

	private void validatePrivacyGenerics(Map<String, Object> deviceInfoData) {
		Assert.assertEquals("privacy", deviceInfoData.get("callType"));
		Assert.assertEquals(SdkProperties.getVersionName(), deviceInfoData.get("sdkVersionName"));
	}

	private void setUserNonBehavioralFlag(boolean flagValue) {
		MetaData userMetaData = new MetaData(InstrumentationRegistry.getInstrumentation().getContext());
		userMetaData.set("user.nonbehavioral", flagValue);
		userMetaData.commit();
	}
}
