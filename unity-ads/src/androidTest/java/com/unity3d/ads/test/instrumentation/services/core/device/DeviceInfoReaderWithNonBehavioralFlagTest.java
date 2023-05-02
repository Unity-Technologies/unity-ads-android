package com.unity3d.ads.test.instrumentation.services.core.device;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_KEY;

import com.unity3d.services.core.device.reader.DeviceInfoReaderWithBehavioralFlag;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.device.reader.pii.NonBehavioralFlag;
import com.unity3d.services.core.device.reader.pii.NonBehavioralFlagReader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoReaderWithNonBehavioralFlagTest {
	@Mock
	private IDeviceInfoReader _deviceInfoReaderMock;

	@Mock
	private NonBehavioralFlagReader _nonBehavioralFlagReaderMock;

	@Test
	public void testDeviceInfoReaderWithBehavioralFlagWhenBehavioralFlagFalse() {
		// given
		Mockito.when(_nonBehavioralFlagReaderMock.getUserNonBehavioralFlag()).thenReturn(NonBehavioralFlag.FALSE);

		// when
		DeviceInfoReaderWithBehavioralFlag deviceInfoReaderWithBehavioralFlag = new DeviceInfoReaderWithBehavioralFlag(_deviceInfoReaderMock, _nonBehavioralFlagReaderMock);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithBehavioralFlag.getDeviceInfoData();

		// then
		Assert.assertEquals(false, deviceInfoData.get(USER_NON_BEHAVIORAL_KEY));
	}

	@Test
	public void testDeviceInfoReaderWithBehavioralFlagWhenBehavioralFlagTrue() {
		// given
		Mockito.when(_nonBehavioralFlagReaderMock.getUserNonBehavioralFlag()).thenReturn(NonBehavioralFlag.TRUE);

		// when
		DeviceInfoReaderWithBehavioralFlag deviceInfoReaderWithBehavioralFlag = new DeviceInfoReaderWithBehavioralFlag(_deviceInfoReaderMock, _nonBehavioralFlagReaderMock);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithBehavioralFlag.getDeviceInfoData();

		// then
		Assert.assertEquals(true, deviceInfoData.get(USER_NON_BEHAVIORAL_KEY));
	}

	@Test
	public void testDeviceInfoReaderWithBehavioralFlagWhenBehavioralFlagUnknown() {
		// given
		Mockito.when(_nonBehavioralFlagReaderMock.getUserNonBehavioralFlag()).thenReturn(NonBehavioralFlag.UNKNOWN);

		// when
		DeviceInfoReaderWithBehavioralFlag deviceInfoReaderWithBehavioralFlag = new DeviceInfoReaderWithBehavioralFlag(_deviceInfoReaderMock, _nonBehavioralFlagReaderMock);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithBehavioralFlag.getDeviceInfoData();

		// then
		Assert.assertNull(deviceInfoData.get(USER_NON_BEHAVIORAL_KEY));
	}

}
