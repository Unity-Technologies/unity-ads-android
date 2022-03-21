package com.unity3d.ads.test.instrumentation.services.core.device;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.ADVERTISING_TRACKING_ID_NORMALIZED_KEY;

import com.unity3d.services.core.device.reader.DeviceInfoReaderWithPII;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.device.reader.pii.DataSelectorResult;
import com.unity3d.services.core.device.reader.pii.PiiDataProvider;
import com.unity3d.services.core.device.reader.pii.PiiDataSelector;
import com.unity3d.services.core.device.reader.pii.PiiDecisionData;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoReaderWithPIITest {

	private static final String PII_STORAGE_IDFA = "test-advertiser-id";
	private static final String PII_DEVICE_IDFA = "test-device-advertiser-id";

	@Mock
	private IDeviceInfoReader _deviceInfoReaderMock;

	@Mock
	private PiiDecisionData _piiDecisionDataMock;

	@Mock
	private PiiDataSelector _piiDataSelectorMock;

	@Mock
	private PiiDataProvider _piiDataProviderMock;

	@Before
	public void setup() {
		Mockito.when(_deviceInfoReaderMock.getDeviceInfoData()).thenReturn(getMockDeviceData());
		Mockito.when(_piiDataSelectorMock.whatToDoWithPII()).thenReturn(_piiDecisionDataMock);
		Mockito.when(_piiDataProviderMock.getAdvertisingTrackingId()).thenReturn(PII_DEVICE_IDFA);
	}

	@Test
	public void testDeviceInfoReaderWithPiiUpdate() {
		Mockito.when(_piiDecisionDataMock.getResultType()).thenReturn(DataSelectorResult.UPDATE);
		DeviceInfoReaderWithPII deviceInfoReaderWithPII = new DeviceInfoReaderWithPII(_deviceInfoReaderMock, _piiDataSelectorMock, _piiDataProviderMock);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithPII.getDeviceInfoData();
		Assert.assertEquals(PII_DEVICE_IDFA, deviceInfoData.get(ADVERTISING_TRACKING_ID_NORMALIZED_KEY));
	}

	@Test
	public void testDeviceInfoReaderWithPiiInclude() {
		Mockito.when(_piiDecisionDataMock.getResultType()).thenReturn(DataSelectorResult.INCLUDE);
		Mockito.when(_piiDecisionDataMock.getAttributes()).thenReturn(getPiiTestData());
		DeviceInfoReaderWithPII deviceInfoReaderWithPII = new DeviceInfoReaderWithPII(_deviceInfoReaderMock, _piiDataSelectorMock, _piiDataProviderMock);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithPII.getDeviceInfoData();
		Assert.assertEquals(PII_STORAGE_IDFA, deviceInfoData.get(ADVERTISING_TRACKING_ID_NORMALIZED_KEY));
	}

	@Test
	public void testDeviceInfoReaderWithPiiExclude() {
		Mockito.when(_piiDecisionDataMock.getResultType()).thenReturn(DataSelectorResult.EXCLUDE);
		Mockito.when(_piiDecisionDataMock.getAttributes()).thenReturn(getPiiTestData());
		DeviceInfoReaderWithPII deviceInfoReaderWithPII = new DeviceInfoReaderWithPII(_deviceInfoReaderMock, _piiDataSelectorMock, _piiDataProviderMock);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithPII.getDeviceInfoData();
		Assert.assertNull(deviceInfoData.get(ADVERTISING_TRACKING_ID_NORMALIZED_KEY));
		Assert.assertEquals(getMockDeviceData(), deviceInfoData);
	}

	private Map<String, Object> getPiiTestData() {
		Map<String, Object> piiTestData = new HashMap<>();
		piiTestData.put(ADVERTISING_TRACKING_ID_NORMALIZED_KEY, PII_STORAGE_IDFA);
		return piiTestData;
	}

	private Map<String, Object> getMockDeviceData() {
		Map<String, Object> piiTestData = new HashMap<>();
		piiTestData.put("deviceKey", "deviceValue");
		return piiTestData;
	}
}
