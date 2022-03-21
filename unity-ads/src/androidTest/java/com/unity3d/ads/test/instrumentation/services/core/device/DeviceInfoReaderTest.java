package com.unity3d.ads.test.instrumentation.services.core.device;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.services.core.device.reader.DeviceInfoReader;
import com.unity3d.services.core.properties.ClientProperties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class DeviceInfoReaderTest {

	@Test
	public void testDeviceInfoReader() {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getContext());
		DeviceInfoReader deviceInfoReader = new DeviceInfoReader();
		Map<String, Object> deviceData = deviceInfoReader.getDeviceInfoData();
		Assert.assertEquals(47, deviceData.size());
	}
}
