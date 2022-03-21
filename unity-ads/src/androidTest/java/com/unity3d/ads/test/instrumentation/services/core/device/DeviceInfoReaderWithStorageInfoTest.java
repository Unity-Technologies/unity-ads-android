package com.unity3d.ads.test.instrumentation.services.core.device;

import com.unity3d.services.core.device.reader.DeviceInfoReaderWithStorageInfo;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.misc.IJsonStorageReader;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class DeviceInfoReaderWithStorageInfoTest {
	private final static Map<String, Object> DEVICE_INFO_TEST_DATA = new HashMap<String, Object>() {{
		put("test", true);
	}};

	private final static JSONObject PUBLIC_STORAGE_TEST_DATA = new JSONObject() {{
		try {
			put("privacy", true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}};

	private final static JSONObject PRIVATE_STORAGE_TEST_DATA = new JSONObject() {{
		try {
			put("gdpr", true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}};


	@Test
	public void testDeviceInfoReaderWithStorageInfoNoStorage() {
		IDeviceInfoReader deviceInfoReader = Mockito.mock(IDeviceInfoReader.class);
		Mockito.when(deviceInfoReader.getDeviceInfoData()).thenReturn(DEVICE_INFO_TEST_DATA);
		DeviceInfoReaderWithStorageInfo deviceInfoReaderWithStorageInfo = new DeviceInfoReaderWithStorageInfo(deviceInfoReader);
		Map<String, Object> resultData = deviceInfoReaderWithStorageInfo.getDeviceInfoData();
		Assert.assertEquals(DEVICE_INFO_TEST_DATA.get("test"), resultData.get("test"));
	}

	@Test
	public void testDeviceInfoReaderWithStorageInfoWithSingleStorage() {
		IDeviceInfoReader deviceInfoReader = Mockito.mock(IDeviceInfoReader.class);
		Mockito.when(deviceInfoReader.getDeviceInfoData()).thenReturn(DEVICE_INFO_TEST_DATA);
		IJsonStorageReader jsonStorageReaderPublic = Mockito.mock(IJsonStorageReader.class);
		Mockito.when(jsonStorageReaderPublic.getData()).thenReturn(PUBLIC_STORAGE_TEST_DATA);
		DeviceInfoReaderWithStorageInfo deviceInfoReaderWithStorageInfo = new DeviceInfoReaderWithStorageInfo(deviceInfoReader, jsonStorageReaderPublic);
		Map<String, Object> resultData = deviceInfoReaderWithStorageInfo.getDeviceInfoData();
		Assert.assertEquals(DEVICE_INFO_TEST_DATA.get("test"), resultData.get("test"));
		Assert.assertEquals(PUBLIC_STORAGE_TEST_DATA.opt("privacy"), resultData.get("privacy"));
	}

	@Test
	public void testDeviceInfoReaderWithStorageInfoWithMultipleStorage() {
		IDeviceInfoReader deviceInfoReader = Mockito.mock(IDeviceInfoReader.class);
		Mockito.when(deviceInfoReader.getDeviceInfoData()).thenReturn(DEVICE_INFO_TEST_DATA);
		IJsonStorageReader jsonStorageReaderPublic = Mockito.mock(IJsonStorageReader.class);
		Mockito.when(jsonStorageReaderPublic.getData()).thenReturn(PUBLIC_STORAGE_TEST_DATA);
		IJsonStorageReader jsonStorageReaderPrivate = Mockito.mock(IJsonStorageReader.class);
		Mockito.when(jsonStorageReaderPrivate.getData()).thenReturn(PRIVATE_STORAGE_TEST_DATA);
		DeviceInfoReaderWithStorageInfo deviceInfoReaderWithStorageInfo = new DeviceInfoReaderWithStorageInfo(deviceInfoReader, jsonStorageReaderPublic, jsonStorageReaderPrivate);
		Map<String, Object> resultData = deviceInfoReaderWithStorageInfo.getDeviceInfoData();
		Assert.assertEquals(DEVICE_INFO_TEST_DATA.get("test"), resultData.get("test"));
		Assert.assertEquals(PUBLIC_STORAGE_TEST_DATA.opt("privacy"), resultData.get("privacy"));
		Assert.assertEquals(PRIVATE_STORAGE_TEST_DATA.opt("gdpr"), resultData.get("gdpr"));
	}
}
