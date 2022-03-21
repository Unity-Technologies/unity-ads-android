package com.unity3d.ads.test.instrumentation.services.core.device.pii;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.PRIVACY_MODE_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.PRIVACY_SPM_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_VALUE_ALT_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_VALUE_KEY;

import com.unity3d.services.core.device.reader.pii.PiiPrivacyMode;
import com.unity3d.services.core.device.reader.pii.PiiTrackingStatusReader;
import com.unity3d.services.core.misc.IJsonStorageReader;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PiiTrackingStatusReaderTest {
	private enum PrivacyStorageType {
		SPM,
		USER
	}

	@Mock
	private IJsonStorageReader jsonStorageReaderMock;

	@Test
	public void testPiiTrackingStatusReaderNull() {
		PiiTrackingStatusReader piiTrackingStatusReader = new PiiTrackingStatusReader(jsonStorageReaderMock);
		PiiPrivacyMode privacyMode = piiTrackingStatusReader.getPrivacyMode();
		Assert.assertEquals(PiiPrivacyMode.NULL, privacyMode);
	}

	@Test
	public void testPiiTrackingStatusReaderSpmApp() {
		setAndVerifyPrivacyMode("App", PrivacyStorageType.SPM);
	}

	@Test
	public void testPiiTrackingStatusReaderSpmNone() {
		setAndVerifyPrivacyMode("None", PrivacyStorageType.SPM);
	}

	@Test
	public void testPiiTrackingStatusReaderSpmMixed() {
		setAndVerifyPrivacyMode("Mixed", PrivacyStorageType.SPM);
	}

	@Test
	public void testPiiTrackingStatusReaderUserPrivacyApp() {
		setAndVerifyPrivacyMode("App", PrivacyStorageType.USER);
	}

	@Test
	public void testPiiTrackingStatusReaderUserPrivacyNone() {
		setAndVerifyPrivacyMode("None", PrivacyStorageType.USER);
	}

	@Test
	public void testPiiTrackingStatusReaderUserPrivacyMixed() {
		setAndVerifyPrivacyMode("Mixed", PrivacyStorageType.USER);
	}

	@Test
	public void testPiiTrackingStatusReaderUserNonePrivacySpmMixed() {
		Mockito.when(jsonStorageReaderMock.get(PRIVACY_SPM_KEY)).thenReturn("None");
		Mockito.when(jsonStorageReaderMock.get(PRIVACY_MODE_KEY)).thenReturn("Mixed");
		PiiTrackingStatusReader piiTrackingStatusReader = new PiiTrackingStatusReader(jsonStorageReaderMock);
		PiiPrivacyMode privacyMode = piiTrackingStatusReader.getPrivacyMode();
		Assert.assertEquals(PiiPrivacyMode.MIXED, privacyMode);
	}

	@Test
	public void testPiiTrackingStatusReaderNonBehavioral() {
		mockUserNonBehavioralFlagData(true, false);
		PiiTrackingStatusReader piiTrackingStatusReader = new PiiTrackingStatusReader(jsonStorageReaderMock);
		boolean nonBehavioralFlag = piiTrackingStatusReader.getUserNonBehavioralFlag();
		Assert.assertTrue(nonBehavioralFlag);
	}

	@Test
	public void testPiiTrackingStatusReaderNonBehavioralString() {
		mockUserNonBehavioralFlagData("True", false);
		PiiTrackingStatusReader piiTrackingStatusReader = new PiiTrackingStatusReader(jsonStorageReaderMock);
		boolean nonBehavioralFlag = piiTrackingStatusReader.getUserNonBehavioralFlag();
		Assert.assertTrue(nonBehavioralFlag);
	}

	@Test
	public void testPiiTrackingStatusReaderNonBehavioralAlt() {
		mockUserNonBehavioralFlagData(true, true);
		PiiTrackingStatusReader piiTrackingStatusReader = new PiiTrackingStatusReader(jsonStorageReaderMock);
		boolean nonBehavioralFlag = piiTrackingStatusReader.getUserNonBehavioralFlag();
		Assert.assertTrue(nonBehavioralFlag);
	}

	@Test
	public void testPiiTrackingStatusReaderNonBehavioralFalse() {
		mockUserNonBehavioralFlagData(false, false);
		PiiTrackingStatusReader piiTrackingStatusReader = new PiiTrackingStatusReader(jsonStorageReaderMock);
		boolean nonBehavioralFlag = piiTrackingStatusReader.getUserNonBehavioralFlag();
		Assert.assertFalse(nonBehavioralFlag);
	}

	private void setAndVerifyPrivacyMode(String privacyModeStr, PrivacyStorageType privacyType) {
		Mockito.when(jsonStorageReaderMock.get(privacyType == PrivacyStorageType.SPM ? PRIVACY_SPM_KEY : PRIVACY_MODE_KEY)).thenReturn(privacyModeStr);
		PiiTrackingStatusReader piiTrackingStatusReader = new PiiTrackingStatusReader(jsonStorageReaderMock);
		PiiPrivacyMode privacyMode = piiTrackingStatusReader.getPrivacyMode();
		Assert.assertEquals(PiiPrivacyMode.getPiiPrivacyMode(privacyModeStr), privacyMode);
	}

	private void mockUserNonBehavioralFlagData(Object nonBehavioral, boolean alternateKey) {
		Mockito.when(jsonStorageReaderMock.get(alternateKey ? USER_NON_BEHAVIORAL_VALUE_ALT_KEY : USER_NON_BEHAVIORAL_VALUE_KEY)).thenReturn(nonBehavioral);
	}
}
