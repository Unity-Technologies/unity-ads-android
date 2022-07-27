package com.unity3d.ads.test.instrumentation.services.core.device;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.services.core.device.reader.DeviceInfoReaderExtended;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.properties.ClientProperties;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoReaderExtendedTest {
	@Mock
	IDeviceInfoReader _deviceInfoReaderMock;

	@Test
	public void testDeviceInfoReaderExtended() {
		Mockito.when(_deviceInfoReaderMock.getDeviceInfoData()).thenReturn(new HashMap<String, Object>());
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getContext());
		DeviceInfoReaderExtended deviceInfoReader = new DeviceInfoReaderExtended(_deviceInfoReaderMock);
		Map<String, Object> deviceData = deviceInfoReader.getDeviceInfoData();
		Assert.assertEquals(42, deviceData.size());
	}
}
