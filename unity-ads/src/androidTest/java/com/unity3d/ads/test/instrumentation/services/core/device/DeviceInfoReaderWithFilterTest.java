package com.unity3d.ads.test.instrumentation.services.core.device;

import com.unity3d.services.core.device.reader.DeviceInfoReaderWithFilter;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoReaderWithFilterTest {
	@Mock
	IDeviceInfoReader _deviceInfoReaderMock;

	@Test
	public void testDeviceInfoReaderSingleWithFilterSingleKey() {
		Mockito.when(_deviceInfoReaderMock.getDeviceInfoData()).thenReturn(getTestDataWithSingleEntry());
		DeviceInfoReaderWithFilter deviceInfoReaderWithFilter = new DeviceInfoReaderWithFilter(_deviceInfoReaderMock, Collections.singletonList("key1"));
		Assert.assertTrue(deviceInfoReaderWithFilter.getDeviceInfoData().isEmpty());
	}

	@Test
	public void testDeviceInfoReaderMultipleWithFilterSingleKey() {
		Mockito.when(_deviceInfoReaderMock.getDeviceInfoData()).thenReturn(getTestDataWithMultipleEntry());
		DeviceInfoReaderWithFilter deviceInfoReaderWithFilter = new DeviceInfoReaderWithFilter(_deviceInfoReaderMock, Collections.singletonList("key1"));
		Assert.assertNull(deviceInfoReaderWithFilter.getDeviceInfoData().get("key1"));
		Assert.assertEquals(2, deviceInfoReaderWithFilter.getDeviceInfoData().size());
	}

	@Test
	public void testDeviceInfoReaderMultipleWithFilterMissingKey() {
		Mockito.when(_deviceInfoReaderMock.getDeviceInfoData()).thenReturn(getTestDataWithMultipleEntry());
		DeviceInfoReaderWithFilter deviceInfoReaderWithFilter = new DeviceInfoReaderWithFilter(_deviceInfoReaderMock, Collections.singletonList("missing"));
		Assert.assertEquals(getTestDataWithMultipleEntry(), deviceInfoReaderWithFilter.getDeviceInfoData());
	}

	@Test
	public void testDeviceInfoReaderMultipleWithFilterEmpty() {
		Mockito.when(_deviceInfoReaderMock.getDeviceInfoData()).thenReturn(getTestDataWithMultipleEntry());
		DeviceInfoReaderWithFilter deviceInfoReaderWithFilter = new DeviceInfoReaderWithFilter(_deviceInfoReaderMock, new ArrayList<String>());
		Assert.assertEquals(getTestDataWithMultipleEntry(), deviceInfoReaderWithFilter.getDeviceInfoData());
	}

	@Test
	public void testDeviceInfoReaderMultipleWithFilterNull() {
		Mockito.when(_deviceInfoReaderMock.getDeviceInfoData()).thenReturn(getTestDataWithMultipleEntry());
		DeviceInfoReaderWithFilter deviceInfoReaderWithFilter = new DeviceInfoReaderWithFilter(_deviceInfoReaderMock, null);
		Assert.assertEquals(getTestDataWithMultipleEntry(), deviceInfoReaderWithFilter.getDeviceInfoData());
	}

	private Map<String, Object> getTestDataWithSingleEntry() {
		return new HashMap<String, Object>() {{
			put("key1", "value1");
		}};
	}

	private Map<String, Object> getTestDataWithMultipleEntry() {
		return new HashMap<String, Object>() {{
			put("key1", "value1");
			put("key2", "value2");
			put("key3", "value3");
		}};
	}
}
