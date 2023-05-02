package com.unity3d.ads.test.instrumentation.services.core.device;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.SESSION_ID_KEY;

import com.unity3d.services.core.device.reader.DeviceInfoReaderWithSessionId;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.properties.Session;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoReaderWithSessionIdTest {
	private static final String FAKE_SESSION_ID = "fakeSessionId";

	@Mock
	private IDeviceInfoReader _deviceInfoReaderMock;

	@Mock
	private Session _sessionMock;

	@Test
	public void testDeviceInfoReaderWithSessionId() {
		// given
		Mockito.when(_sessionMock.getId()).thenReturn(FAKE_SESSION_ID);

		// when
		DeviceInfoReaderWithSessionId deviceInfoReaderWithBehavioralFlag = new DeviceInfoReaderWithSessionId(_deviceInfoReaderMock, _sessionMock);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithBehavioralFlag.getDeviceInfoData();

		// then
		Assert.assertEquals(FAKE_SESSION_ID, deviceInfoData.get(SESSION_ID_KEY));
	}
}
