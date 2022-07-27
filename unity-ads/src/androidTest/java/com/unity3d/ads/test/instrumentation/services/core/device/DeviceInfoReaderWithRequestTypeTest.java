package com.unity3d.ads.test.instrumentation.services.core.device;

import com.unity3d.services.core.configuration.InitRequestType;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithRequestType;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoReaderWithRequestTypeTest {

	@Mock
	private IDeviceInfoReader _deviceInfoReaderMock;

	@Test
	public void testDeviceInfoReaderWithRequestTypeToken() {
		DeviceInfoReaderWithRequestType deviceInfoReaderWithRequestType = new DeviceInfoReaderWithRequestType(_deviceInfoReaderMock, InitRequestType.TOKEN);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithRequestType.getDeviceInfoData();
		Assert.assertEquals("token", deviceInfoData.get("callType"));
	}

	@Test
	public void testDeviceInfoReaderWithRequestTypePrivacy() {
		DeviceInfoReaderWithRequestType deviceInfoReaderWithRequestType = new DeviceInfoReaderWithRequestType(_deviceInfoReaderMock, InitRequestType.PRIVACY);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithRequestType.getDeviceInfoData();
		Assert.assertEquals("privacy", deviceInfoData.get("callType"));
	}

	@Test
	public void testDeviceInfoReaderWithRequestTypeNull() {
		DeviceInfoReaderWithRequestType deviceInfoReaderWithRequestType = new DeviceInfoReaderWithRequestType(_deviceInfoReaderMock, null);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithRequestType.getDeviceInfoData();
		Assert.assertNull(deviceInfoData.get("callType"));
	}
}
