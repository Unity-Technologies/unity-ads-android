package com.unity3d.ads.test.instrumentation.services.core.device;

import android.app.Application;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.services.core.configuration.InitRequestType;
import com.unity3d.services.core.device.reader.DeviceInfoDataFactory;
import com.unity3d.services.core.device.reader.IDeviceInfoDataContainer;
import com.unity3d.services.core.lifecycle.CachedLifecycle;
import com.unity3d.services.core.properties.ClientProperties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeviceInfoDataFactoryTest {
	private static String TEST_GAME_ID = "123456";

	@Before
	public void setup() {
		ClientProperties.setApplication((Application) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext());
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
		ClientProperties.setGameId(TEST_GAME_ID);
		CachedLifecycle.register();
	}

	@After
	public void tearDown() {
		CachedLifecycle.unregister();
	}

	@Test
	public void testDeviceInfoDataFactoryForToken() {
		DeviceInfoDataFactory deviceInfoDataFactory = new DeviceInfoDataFactory();
		IDeviceInfoDataContainer deviceInfoDataContainer = deviceInfoDataFactory.getDeviceInfoData(InitRequestType.TOKEN);
		Assert.assertEquals(InitRequestType.TOKEN.getCallType(), deviceInfoDataContainer.getDeviceInfo().get("callType"));
	}

	@Test
	public void testDeviceInfoDataFactoryForPrivacy() {
		DeviceInfoDataFactory deviceInfoDataFactory = new DeviceInfoDataFactory();
		IDeviceInfoDataContainer deviceInfoDataContainer = deviceInfoDataFactory.getDeviceInfoData(InitRequestType.PRIVACY);
		Assert.assertEquals(InitRequestType.PRIVACY.getCallType(), deviceInfoDataContainer.getDeviceInfo().get("callType"));
	}
}
