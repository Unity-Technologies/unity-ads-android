package com.unity3d.ads.test.instrumentation.services.core.device;

import android.util.Base64;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.core.device.reader.DeviceInfoReaderCompressor;
import com.unity3d.services.core.device.reader.DeviceInfoReaderUrlEncoder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@RunWith(AndroidJUnit4.class)
public class DeviceInfoReaderUrlEncoderTest {
	@Test
	public void testDeviceInfoReaderUrlEncoderDecorator() throws UnsupportedEncodingException {
		DeviceInfoReaderCompressor deviceInfoReaderCompressor = Mockito.mock(DeviceInfoReaderCompressor.class);
		Mockito.when(deviceInfoReaderCompressor.getDeviceData()).thenReturn("test".getBytes());
		DeviceInfoReaderUrlEncoder deviceInfoReaderUrlEncoder = new DeviceInfoReaderUrlEncoder(deviceInfoReaderCompressor);
		String urlQueryString = deviceInfoReaderUrlEncoder.getUrlEncodedData();
		String decodedValue = URLDecoder.decode(urlQueryString, "UTF-8");
		byte[] base64Decoded = Base64.decode(decodedValue, Base64.NO_WRAP);
		Assert.assertEquals("dGVzdA%3D%3D", urlQueryString);
		Assert.assertArrayEquals("test".getBytes(), base64Decoded);
	}
}
