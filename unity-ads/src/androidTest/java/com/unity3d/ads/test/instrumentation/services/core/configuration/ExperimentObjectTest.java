package com.unity3d.ads.test.instrumentation.services.core.configuration;

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;

import com.unity3d.services.core.configuration.ExperimentAppliedRule;
import com.unity3d.services.core.configuration.ExperimentObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4ClassRunner.class)
public class ExperimentObjectTest {

	@Test
	public void testExperimentObjectWithNull() {
		ExperimentObject exp = new ExperimentObject(null);
		Assert.assertFalse(exp.getBooleanValue());
		Assert.assertEquals(ExperimentAppliedRule.NEXT, exp.getAppliedRule());
	}

	@Test
	public void testExperimentObjectWithInvalid() throws JSONException {
		ExperimentObject exp = new ExperimentObject(new JSONObject("{\"something\": false}"));
		Assert.assertFalse(exp.getBooleanValue());
		Assert.assertEquals(ExperimentAppliedRule.NEXT, exp.getAppliedRule());
	}

	@Test
	public void testExperimentObjectWithValueOnly() throws JSONException {
		ExperimentObject exp = new ExperimentObject(new JSONObject("{\"value\": \"true\"}"));
		Assert.assertTrue(exp.getBooleanValue());
	}

	@Test
	public void testExperimentObjectWithValueAndAppliedRuleNext() throws JSONException {
		ExperimentObject exp = new ExperimentObject(new JSONObject("{\"value\": \"true\", \"applied\": \"next\"}"));
		Assert.assertTrue(exp.getBooleanValue());
		Assert.assertEquals(ExperimentAppliedRule.NEXT, exp.getAppliedRule());
	}

	@Test
	public void testExperimentObjectWithValueAndAppliedRuleImmediate() throws JSONException {
		ExperimentObject exp = new ExperimentObject(new JSONObject("{\"value\": \"true\", \"applied\": \"immediate\"}"));
		Assert.assertTrue(exp.getBooleanValue());
		Assert.assertEquals(ExperimentAppliedRule.IMMEDIATE, exp.getAppliedRule());
	}
}
