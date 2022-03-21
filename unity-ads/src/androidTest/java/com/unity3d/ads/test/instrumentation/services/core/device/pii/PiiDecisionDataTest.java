package com.unity3d.ads.test.instrumentation.services.core.device.pii;

import static com.unity3d.services.core.device.reader.JsonStorageKeyNames.USER_NON_BEHAVIORAL_KEY;

import com.unity3d.services.core.device.reader.pii.DataSelectorResult;
import com.unity3d.services.core.device.reader.pii.PiiDecisionData;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class PiiDecisionDataTest {

	@Test
	public void testPiiDecisionData() {
		PiiDecisionData piiDecisionData = new PiiDecisionData(DataSelectorResult.EXCLUDE, getOriginalDummyData());
		Assert.assertEquals(DataSelectorResult.EXCLUDE, piiDecisionData.getResultType());
		Assert.assertEquals(getOriginalDummyData(), piiDecisionData.getAttributes());
	}

	@Test
	public void testPiiDecisionAppendData() {
		PiiDecisionData piiDecisionData = new PiiDecisionData(DataSelectorResult.INCLUDE, getOriginalDummyData());
		piiDecisionData.appendData(new HashMap<String, Object>() {{
			put("key3", "value3");
		}});
		Assert.assertEquals(3, piiDecisionData.getAttributes().size());
	}

	@Test
	public void testPiiDecisionUserNonBehaviouralFlagTrue() {
		PiiDecisionData piiDecisionData = new PiiDecisionData(DataSelectorResult.EXCLUDE, getOriginalDummyDataWithNonBehavioralFlag(true));
		Assert.assertEquals(DataSelectorResult.EXCLUDE, piiDecisionData.getResultType());
		Assert.assertTrue(piiDecisionData.getUserNonBehavioralFlag());
	}

	@Test
	public void testPiiDecisionUserNonBehaviouralFlagFalse() {
		PiiDecisionData piiDecisionData = new PiiDecisionData(DataSelectorResult.EXCLUDE, getOriginalDummyDataWithNonBehavioralFlag(false));
		Assert.assertEquals(DataSelectorResult.EXCLUDE, piiDecisionData.getResultType());
		Assert.assertFalse(piiDecisionData.getUserNonBehavioralFlag());
	}

	@Test
	public void testPiiDecisionUserNonBehaviouralFlagMissing() {
		PiiDecisionData piiDecisionData = new PiiDecisionData(DataSelectorResult.EXCLUDE, getOriginalDummyData());
		Assert.assertEquals(DataSelectorResult.EXCLUDE, piiDecisionData.getResultType());
		Assert.assertNull(piiDecisionData.getUserNonBehavioralFlag());
	}

	private Map<String, Object> getOriginalDummyData() {
		return new HashMap<String, Object>() {{
			put("key1", "value1");
			put("key2", "value2");
		}};
	}

	private Map<String, Object> getOriginalDummyDataWithNonBehavioralFlag(boolean userNonBehavioralFlag) {
		Map<String, Object> dataWithNonBehavioralFlag = getOriginalDummyData();
		dataWithNonBehavioralFlag.put(USER_NON_BEHAVIORAL_KEY, userNonBehavioralFlag);
		return dataWithNonBehavioralFlag;
	}
}
