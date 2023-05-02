package com.unity3d.ads.test.instrumentation.services.core.device;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_KEY;

import com.unity3d.services.core.configuration.PrivacyConfig;
import com.unity3d.services.core.configuration.PrivacyConfigStorage;
import com.unity3d.services.core.device.reader.DeviceInfoReaderWithPrivacy;
import com.unity3d.services.core.device.reader.IDeviceInfoReader;
import com.unity3d.services.core.device.reader.pii.PiiDataProvider;
import com.unity3d.services.core.device.reader.pii.PiiTrackingStatusReader;

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
public class DeviceInfoReaderWithPrivacyTest {
	private static final String FAKE_ADVERTISER_ID = "fakeSessionId";
	private static final boolean FAKE_NONBEHAVIORAL = true;

	@Mock
	private PrivacyConfig _privacyConfigMock;

	@Mock
	private IDeviceInfoReader _deviceInfoReaderMock;

	@Mock
	private PrivacyConfigStorage _privacyConfigStorageMock;

	@Mock
	private PiiDataProvider _piiDataProviderMock;

	@Mock
	private PiiTrackingStatusReader _piiTrackingStatusReaderMock;

	@Before
	public void setup() {
		Mockito.when(_deviceInfoReaderMock.getDeviceInfoData()).thenReturn(new HashMap<>());
		Mockito.when(_privacyConfigStorageMock.getPrivacyConfig()).thenReturn(_privacyConfigMock);
		Mockito.when(_piiDataProviderMock.getAdvertisingTrackingId()).thenReturn(FAKE_ADVERTISER_ID);
		Mockito.when(_piiTrackingStatusReaderMock.getUserNonBehavioralFlag()).thenReturn(FAKE_NONBEHAVIORAL);
	}

	@Test
	public void testDeviceInfoReaderWithPrivacySnbDisabled() {
		// given
		Mockito.when(_privacyConfigMock.shouldSendNonBehavioral()).thenReturn(false);

		// when
		DeviceInfoReaderWithPrivacy deviceInfoReaderWithPrivacy = new DeviceInfoReaderWithPrivacy(_deviceInfoReaderMock, _privacyConfigStorageMock, _piiDataProviderMock, _piiTrackingStatusReaderMock);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithPrivacy.getDeviceInfoData();

		// then
		Assert.assertNull(deviceInfoData.get(USER_NON_BEHAVIORAL_KEY));
	}

	@Test
	public void testDeviceInfoReaderWithPrivacySnbEnabledNbTrue() {
		// given
		Mockito.when(_privacyConfigMock.shouldSendNonBehavioral()).thenReturn(true);
		Mockito.when(_piiTrackingStatusReaderMock.getUserNonBehavioralFlag()).thenReturn(true);

		// when
		DeviceInfoReaderWithPrivacy deviceInfoReaderWithPrivacy = new DeviceInfoReaderWithPrivacy(_deviceInfoReaderMock, _privacyConfigStorageMock, _piiDataProviderMock, _piiTrackingStatusReaderMock);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithPrivacy.getDeviceInfoData();

		// then
		Assert.assertEquals(true, deviceInfoData.get(USER_NON_BEHAVIORAL_KEY));
	}

	@Test
	public void testDeviceInfoReaderWithPrivacySnbEnabledNbFalse() {
		// given
		Mockito.when(_privacyConfigMock.shouldSendNonBehavioral()).thenReturn(true);
		Mockito.when(_piiTrackingStatusReaderMock.getUserNonBehavioralFlag()).thenReturn(false);

		// when
		DeviceInfoReaderWithPrivacy deviceInfoReaderWithPrivacy = new DeviceInfoReaderWithPrivacy(_deviceInfoReaderMock, _privacyConfigStorageMock, _piiDataProviderMock, _piiTrackingStatusReaderMock);
		Map<String, Object> deviceInfoData = deviceInfoReaderWithPrivacy.getDeviceInfoData();

		// then
		Assert.assertEquals(false, deviceInfoData.get(USER_NON_BEHAVIORAL_KEY));
	}
}
