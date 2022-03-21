package com.unity3d.ads.test.instrumentation.services.core.device;

import com.unity3d.services.core.device.reader.DeviceInfoReaderFilterProvider;
import com.unity3d.services.core.misc.IJsonStorageReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoReaderFilterProviderTest {
	@Mock
	IJsonStorageReader _jsonStorageReaderMock;

	@Test
	public void testDeviceInfoReaderFilterProvider() throws JSONException {
		Mockito.when(_jsonStorageReaderMock.getData()).thenReturn(getStorageSimpleMockData());
		DeviceInfoReaderFilterProvider deviceInfoReaderFilterProvider = new DeviceInfoReaderFilterProvider(_jsonStorageReaderMock);
		List<String> filteredKeys = deviceInfoReaderFilterProvider.getFilterList();
		Assert.assertNotNull(filteredKeys);
		Assert.assertEquals(Collections.singletonList("field1"), filteredKeys);
	}

	@Test
	public void testDeviceInfoReaderFilterProviderWithComplex() throws JSONException {
		Mockito.when(_jsonStorageReaderMock.getData()).thenReturn(getStorageComplexMockData());
		DeviceInfoReaderFilterProvider deviceInfoReaderFilterProvider = new DeviceInfoReaderFilterProvider(_jsonStorageReaderMock);
		List<String> filteredKeys = deviceInfoReaderFilterProvider.getFilterList();
		Assert.assertNotNull(filteredKeys);
		Assert.assertEquals(Collections.singletonList("field1"), filteredKeys);
	}

	@Test
	public void testDeviceInfoReaderFilterProviderWithEmptyList() {
		Mockito.when(_jsonStorageReaderMock.getData()).thenReturn(new JSONObject());
		DeviceInfoReaderFilterProvider deviceInfoReaderFilterProvider = new DeviceInfoReaderFilterProvider(_jsonStorageReaderMock);
		List<String> filteredKeys = deviceInfoReaderFilterProvider.getFilterList();
		Assert.assertNotNull(filteredKeys);
		Assert.assertEquals(0, filteredKeys.size());
	}

	@Test
	public void testDeviceInfoReaderFilterProviderWithNullData() {
		Mockito.when(_jsonStorageReaderMock.getData()).thenReturn(null);
		DeviceInfoReaderFilterProvider deviceInfoReaderFilterProvider = new DeviceInfoReaderFilterProvider(_jsonStorageReaderMock);
		List<String> filteredKeys = deviceInfoReaderFilterProvider.getFilterList();
		Assert.assertNotNull(filteredKeys);
		Assert.assertEquals(0, filteredKeys.size());
	}

	@Test
	public void testDeviceInfoReaderFilterProviderWithCommas() throws JSONException {
		Mockito.when(_jsonStorageReaderMock.getData()).thenReturn(getStorageCommaSeparatedMockData());
		DeviceInfoReaderFilterProvider deviceInfoReaderFilterProvider = new DeviceInfoReaderFilterProvider(_jsonStorageReaderMock);
		List<String> filteredKeys = deviceInfoReaderFilterProvider.getFilterList();
		Assert.assertNotNull(filteredKeys);
		Assert.assertEquals(Arrays.asList("field1", "field2"), filteredKeys);
	}

	private JSONObject getStorageSimpleMockData() throws JSONException {
		return new JSONObject("{\"unifiedconfig\": {\"exclude\": \"field1\"}}");
	}

	private JSONObject getStorageCommaSeparatedMockData() throws JSONException {
		return new JSONObject("{\"unifiedconfig\": {\"exclude\": \"field1, field2\"}}");
	}

	private JSONObject getStorageComplexMockData() throws JSONException {
		return new JSONObject("{\"key1\": \"value1\", \"unifiedconfig\": {\"exclude\": \"field1\"}}");
	}
}
