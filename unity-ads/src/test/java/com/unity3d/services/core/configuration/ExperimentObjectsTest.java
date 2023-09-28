package com.unity3d.services.core.configuration;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentObjectsTest {

	@Test
	public void testExperimentObjectsNull() {
		IExperiments expo = new ExperimentObjects(null);
		Assert.assertFalse(expo.isWebMessageEnabled());
	}

	@Test
	public void testExperimentObjectsWithInvalid() throws JSONException {
		ExperimentObjects expo = new ExperimentObjects(new JSONObject("{\"something\": false}"));
		Assert.assertFalse(expo.isWebMessageEnabled());
	}

	@Test
	public void testExperimentObjectsWithBoolean() throws JSONException {
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"tsi_prw\": {\"value\": \"true\"}}"));
		Assert.assertTrue(expo.shouldNativeTokenAwaitPrivacy());
		Assert.assertFalse(expo.isNativeWebViewCacheEnabled());
	}

	@Test
	public void testExperimentObjectsCurrentSession() throws JSONException {
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"tsi_prw\": {\"value\": \"true\", \"applied\": \"next\"},"
																+ "\"scar_init\": {\"value\": true, \"applied\": \"next\"},"
																+ "\"nilt\": {\"value\": false, \"applied\": \"next\"},"
																+ "\"scar_bn\": {\"value\": true, \"applied\": \"next\"},"
																+ "\"wac\": {\"value\": \"true\", \"applied\": \"immediate\"}}"));
		Assert.assertFalse(expo.getCurrentSessionExperiments().has("tsi_prw"));
		Assert.assertTrue(expo.getCurrentSessionExperiments().has("wac"));

		JSONObject expectedOut = new JSONObject("{\"wac\":\"true\"}");
		Assert.assertEquals(expectedOut.toString(), expo.getCurrentSessionExperiments().toString());
	}

	@Test
	public void testExperimentObjectsNextSession() throws JSONException {
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"tsi_prw\": {\"value\": \"true\", \"applied\": \"next\"},"
																+ "\"scar_init\": {\"value\": true, \"applied\": \"next\"},"
																+ "\"nilt\": {\"value\": false, \"applied\": \"next\"},"
																+ "\"scar_bn\": {\"value\": true, \"applied\": \"next\"},"
																+ "\"wac\": {\"value\": \"true\", \"applied\": \"immediate\"}}"));
		Assert.assertTrue(expo.getNextSessionExperiments().has("tsi_prw"));
		Assert.assertFalse(expo.getNextSessionExperiments().has("wac"));

		JSONObject expectedOut = new JSONObject("{\"nilt\":\"false\",\"scar_bn\":\"true\",\"tsi_prw\":\"true\",\"scar_init\":\"true\"}");
		Assert.assertEquals(expectedOut.length(), expo.getNextSessionExperiments().length());
		Assert.assertEquals(expectedOut.get("nilt"), expo.getNextSessionExperiments().get("nilt"));
		Assert.assertEquals(expectedOut.get("scar_bn"), expo.getNextSessionExperiments().get("scar_bn"));
		Assert.assertEquals(expectedOut.get("tsi_prw"), expo.getNextSessionExperiments().get("tsi_prw"));
		Assert.assertEquals(expectedOut.get("scar_init"), expo.getNextSessionExperiments().get("scar_init"));
	}

	@Test
	public void testGetExperimentTagsWithMixedDataTypes() throws JSONException {
		// Testing a JSON object with both boolean and Strings to ensure opt interpretation is correct
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"tsi_prw\": {\"value\": \"true\", \"applied\": \"next\"},"
																+ "\"scar_init\": {\"value\": true, \"applied\": \"next\"},"
																+ "\"nilt\": {\"value\": false, \"applied\": \"next\"},"
																+ "\"scar_bn\": {\"value\": true, \"applied\": \"next\"},"
																+ "\"wac\": {\"value\": \"true\", \"applied\": \"immediate\"}}"));
		Map<String, String> expectedOut = new HashMap<>();
		expectedOut.put("tsi_prw", "true");
		expectedOut.put("wac", "true");
		expectedOut.put("scar_init", "true");
		expectedOut.put("scar_bn", "true");
		expectedOut.put("nilt", "false");
		Assert.assertEquals(expectedOut.entrySet(), expo.getExperimentTags().entrySet());
	}
}