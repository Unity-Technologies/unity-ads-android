package com.unity3d.ads.test.instrumentation.services.core.device;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.core.device.reader.DeviceInfoReaderCompressor;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class DeviceInfoReaderCompressorTest {
	private static final Map<String, Object> TESTDATA = new HashMap<String, Object>() {{
		put("Key", 0);
	}};

	private static byte[] TESTDATA_COMPRESSED = new byte[] {31, -117, 8, 0, 0, 0, 0, 0, 0, 0, -85, 86, -14, 78, -83, 84, -78, 50, -88, 5, 0, -40, 45, 110, 23, 9, 0, 0, 0};

	@Test
	public void testDeviceInfoReaderCompressor() {
		IDeviceInfoReader deviceInfoReaderMock = Mockito.mock(IDeviceInfoReader.class);
		Mockito.when(deviceInfoReaderMock.getDeviceInfoData()).thenReturn(TESTDATA);
		DeviceInfoReaderCompressor deviceInfoReaderCompressor = new DeviceInfoReaderCompressor(deviceInfoReaderMock);
		byte[] compressedData = deviceInfoReaderCompressor.getDeviceData();
		Assert.assertArrayEquals(TESTDATA_COMPRESSED, compressedData);
	}

	@Test
	public void testDeviceInfoReaderCompressorWithEmptyMap() {
		IDeviceInfoReader deviceInfoReaderMock = Mockito.mock(IDeviceInfoReader.class);
		Mockito.when(deviceInfoReaderMock.getDeviceInfoData()).thenReturn(new HashMap<String, Object>());
		DeviceInfoReaderCompressor deviceInfoReaderCompressor = new DeviceInfoReaderCompressor(deviceInfoReaderMock);
		byte[] compressedData = deviceInfoReaderCompressor.getDeviceData();
		Assert.assertNotNull(compressedData);
	}

	@Test
	public void testDeviceInfoReaderCompressorWithNullDeviceData() {
		IDeviceInfoReader deviceInfoReaderMock = Mockito.mock(IDeviceInfoReader.class);
		Mockito.when(deviceInfoReaderMock.getDeviceInfoData()).thenReturn(null);
		DeviceInfoReaderCompressor deviceInfoReaderCompressor = new DeviceInfoReaderCompressor(deviceInfoReaderMock);
		byte[] compressedData = deviceInfoReaderCompressor.getDeviceData();
		Assert.assertNull(compressedData);
	}
}
