package com.unity3d.services.core.configuration;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class ExperimentObjectTest {

	@Test
	public void testExperimentObjectWithNull() {
		ExperimentObject exp = new ExperimentObject(null);
		Assert.assertFalse(exp.getBooleanValue());
		Assert.assertEquals("", exp.getStringValue());
		Assert.assertEquals(ExperimentAppliedRule.NEXT, exp.getAppliedRule());
	}

	@Test
	public void testExperimentObjectWithInvalid() throws JSONException {
		ExperimentObject exp = new ExperimentObject(new JSONObject("{\"something\": false}"));
		Assert.assertFalse(exp.getBooleanValue());
		Assert.assertEquals("", exp.getStringValue());
		Assert.assertEquals(ExperimentAppliedRule.NEXT, exp.getAppliedRule());
	}

	@Test
	public void testExperimentObjectWithBooleanValueOnly() throws JSONException {
		ExperimentObject exp = new ExperimentObject(new JSONObject("{\"value\": \"true\"}"));
		Assert.assertTrue(exp.getBooleanValue());
		Assert.assertEquals("true", exp.getStringValue());
		Assert.assertEquals(ExperimentAppliedRule.NEXT, exp.getAppliedRule());
	}

	@Test
	public void testExperimentObjectWithStringValueOnly() throws JSONException {
		ExperimentObject exp = new ExperimentObject(new JSONObject("{\"value\": \"dis\"}"));
		Assert.assertFalse(exp.getBooleanValue());
		Assert.assertEquals("dis", exp.getStringValue());
		Assert.assertEquals(ExperimentAppliedRule.NEXT, exp.getAppliedRule());
	}

	@Test
	public void testExperimentObjectWithStringValueFromBoolean() throws JSONException {
		ExperimentObject exp = new ExperimentObject(new JSONObject("{\"value\": false }"));
		Assert.assertFalse(exp.getBooleanValue());
		Assert.assertEquals("false", exp.getStringValue());
		Assert.assertEquals(ExperimentAppliedRule.NEXT, exp.getAppliedRule());
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
