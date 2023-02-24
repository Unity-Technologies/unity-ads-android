package com.unity3d.ads.test.instrumentation.services.core.device;

import com.unity3d.services.core.device.reader.DeviceInfoReaderWithExtras;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoReaderWithExtrasTest {
	@Mock
	IDeviceInfoReader _deviceInfoReaderMock;

	@Test
	public void testDeviceInfoReaderWithExtras() {
		Mockito.when(_deviceInfoReaderMock.getDeviceInfoData()).thenReturn(getTestData());
		DeviceInfoReaderWithExtras deviceInfoReaderWithExtras = new DeviceInfoReaderWithExtras(_deviceInfoReaderMock, new HashMap<String, String>() {{
			put("extraKey", "extraValue");
		}});
		Assert.assertEquals(deviceInfoReaderWithExtras.getDeviceInfoData(),
			new HashMap<String, String>() {{
				put("extraKey", "extraValue");
				put("key1", "value1");
			}
		});
	}

	@Test
	public void testDeviceInfoReaderWithNull() {
		Mockito.when(_deviceInfoReaderMock.getDeviceInfoData()).thenReturn(getTestData());
		DeviceInfoReaderWithExtras deviceInfoReaderWithExtras = new DeviceInfoReaderWithExtras(_deviceInfoReaderMock, null);
		Assert.assertEquals(deviceInfoReaderWithExtras.getDeviceInfoData(), getTestData());
	}

	private Map<String, Object> getTestData() {
		return new HashMap<String, Object>() {{
			put("key1", "value1");
		}};
	}

}