package com.unity3d.ads.test.instrumentation.services.core.device;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.GAME_SESSION_ID_NORMALIZED_KEY;

import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.services.core.device.reader.IGameSessionIdReader;
import com.unity3d.services.core.device.reader.MinimalDeviceInfoReader;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class MinimalDeviceInfoReaderTest {

	@Mock
	IGameSessionIdReader gameSessionIdReaderMock;

	private static final String TEST_GAME_ID = "123456";
	private static final Long TEST_GAME_SESSION_ID = 123567891011L;

	@Before
	public void setup() {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
		ClientProperties.setGameId(TEST_GAME_ID);
		Mockito.when(gameSessionIdReaderMock.getGameSessionIdAndStore()).thenReturn(TEST_GAME_SESSION_ID);
	}

	@Test
	public void testMinimalDeviceInfoReader() {
		MinimalDeviceInfoReader minimalDeviceInfoReader = new MinimalDeviceInfoReader(gameSessionIdReaderMock);
		Map<String, Object> deviceInfoData = minimalDeviceInfoReader.getDeviceInfoData();
		Assert.assertEquals("android", deviceInfoData.get("platform"));
		Assert.assertEquals(SdkProperties.getVersionName(), deviceInfoData.get("sdkVersionName"));
		Assert.assertEquals(TEST_GAME_ID, deviceInfoData.get("gameId"));
		Assert.assertEquals(TEST_GAME_SESSION_ID, deviceInfoData.get(GAME_SESSION_ID_NORMALIZED_KEY));
	}

	@Test
	public void testMinimalDeviceInforeaderWithoutGameId() {
		ClientProperties.setGameId(null);
		MinimalDeviceInfoReader minimalDeviceInfoReader = new MinimalDeviceInfoReader(gameSessionIdReaderMock);
		Map<String, Object> deviceInfoData = minimalDeviceInfoReader.getDeviceInfoData();
		Assert.assertEquals(null, deviceInfoData.get("gameId"));
	}
}
