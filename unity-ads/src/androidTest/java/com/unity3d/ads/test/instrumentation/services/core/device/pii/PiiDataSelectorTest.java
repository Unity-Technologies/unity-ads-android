package com.unity3d.ads.test.instrumentation.services.core.device.pii;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.ADVERTISING_TRACKING_ID_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.UNIFIED_CONFIG_PII_KEY;
import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_KEY;

import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.device.reader.pii.DataSelectorResult;
import com.unity3d.services.core.device.reader.pii.PiiDataSelector;
import com.unity3d.services.core.device.reader.pii.PiiDecisionData;
import com.unity3d.services.core.device.reader.pii.PiiPrivacyMode;
import com.unity3d.services.core.device.reader.pii.PiiTrackingStatusReader;
import com.unity3d.services.core.misc.IJsonStorageReader;
import com.unity3d.services.core.misc.JsonFlattener;
import com.unity3d.services.core.misc.Utilities;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
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
public class PiiDataSelectorTest {
	private static final String PII_STORAGE_IDFA = "test-advertiser-id";

	@Mock
	private PiiTrackingStatusReader _piiTrackingStatusReaderMock;

	@Mock
	private IJsonStorageReader _jsonStorageMock;

	@Mock
	private Experiments _experimentsMock;

	@Before
	public void setup() throws JSONException {
		Mockito.when(_jsonStorageMock.get(UNIFIED_CONFIG_PII_KEY)).thenReturn(getPiiInternalData());
	}

	@Test
	public void testPiiDataSelectorNoUpdatePrivacyNoneNonBehaviorTrue() throws JSONException {
		PiiDecisionData expectedDecision = new PiiDecisionData(DataSelectorResult.INCLUDE, getFlattenedTestData(false, false));
		runTest(PiiPrivacyMode.NONE, false, false, false, expectedDecision);
	}

	@Test
	public void testPiiDataSelectorForceUpdatePrivacyMixedNonBehariorFalse() throws JSONException {
		PiiDecisionData expectedDecision = new PiiDecisionData(DataSelectorResult.UPDATE, getFlattenedTestData(true, false));
		runTest(PiiPrivacyMode.MIXED, false, true, false, expectedDecision);
	}

	@Test
	public void testPiiDataSelectorNoUpdatePrivacyMixedNonBehariorFalse() throws JSONException {
		PiiDecisionData expectedDecision = new PiiDecisionData(DataSelectorResult.INCLUDE, getFlattenedTestData(true, false));
		runTest(PiiPrivacyMode.MIXED, false, false, false, expectedDecision);
	}

	@Test
	public void testPiiDataSelectorNoUpdatePrivacyMixedNonBehaviorTrue() throws JSONException {
		PiiDecisionData expectedDecision = new PiiDecisionData(DataSelectorResult.INCLUDE, new HashMap<String, Object>(){{put(USER_NON_BEHAVIORAL_KEY, true);}});
		runTest(PiiPrivacyMode.MIXED, true, false, true, expectedDecision);
	}

	@Test
	public void testPiiDataSelectorNoUpdatePrivacyAppNonBehaviorFalse() throws JSONException {
		PiiDecisionData expectedDecision = new PiiDecisionData(DataSelectorResult.EXCLUDE, new HashMap<String, Object>());
		runTest(PiiPrivacyMode.APP, false, false, false, expectedDecision);
	}

	@Test
	public void testPiiDataSelectorUpdatePrivacyNoneNonBehaviorFalse() throws JSONException {
		PiiDecisionData expectedDecision = new PiiDecisionData(DataSelectorResult.UPDATE, getFlattenedTestData(false, false));
		runTest(PiiPrivacyMode.NONE, false, true, false, expectedDecision);
	}

	@Test
	public void testPiiDataSelectorNoUpdatePrivacyUndefinedNonBehaviorFalse() throws JSONException {
		PiiDecisionData expectedDecision = new PiiDecisionData(DataSelectorResult.EXCLUDE, new HashMap<String, Object>());
		runTest(PiiPrivacyMode.UNDEFINED, false, false, false, expectedDecision);
	}

	private void helpSetMocks(boolean updatePiiFieldsExp, PiiPrivacyMode piiPrivacyMode, boolean userNonBehavioralFlag) {
		Mockito.when(_experimentsMock.isUpdatePiiFields()).thenReturn(updatePiiFieldsExp);
		Mockito.when(_piiTrackingStatusReaderMock.getPrivacyMode()).thenReturn(piiPrivacyMode);
		Mockito.when(_piiTrackingStatusReaderMock.getUserNonBehavioralFlag()).thenReturn(userNonBehavioralFlag);
	}

	private void runTest(PiiPrivacyMode piiPrivacyMode, boolean userNonBehavioral, boolean forcedUpdate, boolean expectNonBehavioural, PiiDecisionData expectedDecision) {
		helpSetMocks(forcedUpdate, piiPrivacyMode, userNonBehavioral);
		PiiDataSelector piiDataSelector = new PiiDataSelector(_piiTrackingStatusReaderMock, _jsonStorageMock, _experimentsMock);
		PiiDecisionData piiDecisionData = piiDataSelector.whatToDoWithPII();
		Assert.assertEquals(expectedDecision.getResultType(), piiDecisionData.getResultType());
		Assert.assertEquals(expectedDecision.getAttributes(), piiDecisionData.getAttributes());
		if (piiDecisionData.getUserNonBehavioralFlag() == null && !expectNonBehavioural) {
			Assert.assertNull(piiDecisionData.getUserNonBehavioralFlag());
		} else {
			Assert.assertEquals(expectNonBehavioural, piiDecisionData.getUserNonBehavioralFlag());
		}
	}

	private Map<String, Object> getFlattenedTestData(boolean includeNonBehavioralFlag, boolean expectedFlag) throws JSONException {
		JsonFlattener jsonFlattener = new JsonFlattener(getPiiTestData());
		JSONObject flattenedTestData;
		flattenedTestData = jsonFlattener.flattenJson(".", Collections.singletonList(UNIFIED_CONFIG_PII_KEY), new ArrayList<String>(), new ArrayList<String>());
		if (includeNonBehavioralFlag) {
			flattenedTestData.put(USER_NON_BEHAVIORAL_KEY, expectedFlag);
		}
		return Utilities.convertJsonToMap(flattenedTestData);
	}

	private JSONObject getPiiTestData() throws JSONException {
		JSONObject piiTestData = new JSONObject();
		piiTestData.put(UNIFIED_CONFIG_PII_KEY, getPiiInternalData());
		return piiTestData;
	}

	private JSONObject getPiiInternalData() throws JSONException {
		JSONObject piiInternalData = new JSONObject();
		piiInternalData.put(ADVERTISING_TRACKING_ID_KEY, PII_STORAGE_IDFA);
		return piiInternalData;
	}

}
