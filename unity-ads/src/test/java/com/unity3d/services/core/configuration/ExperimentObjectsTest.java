package com.unity3d.services.core.configuration;

import com.unity3d.services.core.configuration.ExperimentObjects;
import com.unity3d.services.core.configuration.IExperiments;

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
		Assert.assertFalse(expo.isNewInitFlowEnabled());
	}

	@Test
	public void testExperimentObjectsWithInvalid() throws JSONException {
		ExperimentObjects expo = new ExperimentObjects(new JSONObject("{\"something\": false}"));
		Assert.assertFalse(expo.isNewInitFlowEnabled());
	}

	@Test
	public void testExperimentObjectsWithBoolean() throws JSONException {
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"s_init\": {\"value\": \"true\"}}"));
		Assert.assertTrue(expo.isNewInitFlowEnabled());
		Assert.assertFalse(expo.isNativeWebViewCacheEnabled());
	}

	@Test
	public void testExperimentObjectsWithString() throws JSONException {
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"s_init\": {\"value\": \"true\"},"
			+ "\"scar_bm\": {\"value\": \"eag\"}}"));
		Assert.assertEquals("eag", expo.getScarBiddingManager());
		Assert.assertTrue(expo.isNewInitFlowEnabled());
		Assert.assertFalse(expo.isNativeWebViewCacheEnabled());
	}

	@Test
	public void testExperimentObjectsCurrentSession() throws JSONException {
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"s_init\": {\"value\": \"true\", \"applied\": \"next\"},"
																+ "\"scar_init\": {\"value\": true, \"applied\": \"next\"},"
																+ "\"nilt\": {\"value\": false, \"applied\": \"next\"},"
																+ "\"scar_bm\": {\"value\": \"eag\", \"applied\": \"next\"},"
																+ "\"wac\": {\"value\": \"true\", \"applied\": \"immediate\"}}"));
		Assert.assertFalse(expo.getCurrentSessionExperiments().has("s_init"));
		Assert.assertTrue(expo.getCurrentSessionExperiments().has("wac"));

		JSONObject expectedOut = new JSONObject("{\"wac\":\"true\"}");
		Assert.assertEquals(expectedOut.toString(), expo.getCurrentSessionExperiments().toString());
	}

	@Test
	public void testExperimentObjectsNextSession() throws JSONException {
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"s_init\": {\"value\": \"true\", \"applied\": \"next\"},"
																+ "\"scar_init\": {\"value\": true, \"applied\": \"next\"},"
																+ "\"nilt\": {\"value\": false, \"applied\": \"next\"},"
																+ "\"scar_bm\": {\"value\": \"eag\", \"applied\": \"next\"},"
																+ "\"wac\": {\"value\": \"true\", \"applied\": \"immediate\"}}"));
		Assert.assertTrue(expo.getNextSessionExperiments().has("s_init"));
		Assert.assertFalse(expo.getNextSessionExperiments().has("wac"));

		JSONObject expectedOut = new JSONObject("{\"nilt\":\"false\",\"scar_bm\":\"eag\",\"s_init\":\"true\",\"scar_init\":\"true\"}");
		Assert.assertEquals(expectedOut.length(), expo.getNextSessionExperiments().length());
		Assert.assertEquals(expectedOut.get("nilt"), expo.getNextSessionExperiments().get("nilt"));
		Assert.assertEquals(expectedOut.get("scar_bm"), expo.getNextSessionExperiments().get("scar_bm"));
		Assert.assertEquals(expectedOut.get("s_init"), expo.getNextSessionExperiments().get("s_init"));
		Assert.assertEquals(expectedOut.get("scar_init"), expo.getNextSessionExperiments().get("scar_init"));
	}

	@Test
	public void testGetExperimentTagsWithMixedDataTypes() throws JSONException {
		// Testing a JSON object with both boolean and Strings to ensure opt interpretation is correct
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"s_init\": {\"value\": \"true\", \"applied\": \"next\"},"
																+ "\"scar_init\": {\"value\": true, \"applied\": \"next\"},"
																+ "\"nilt\": {\"value\": false, \"applied\": \"next\"},"
																+ "\"scar_bm\": {\"value\": \"eag\", \"applied\": \"next\"},"
																+ "\"wac\": {\"value\": \"true\", \"applied\": \"immediate\"}}"));
		Map<String, String> expectedOut = new HashMap<>();
		expectedOut.put("s_init", "true");
		expectedOut.put("wac", "true");
		expectedOut.put("scar_init", "true");
		expectedOut.put("scar_bm", "eag");
		expectedOut.put("nilt", "false");
		Assert.assertTrue(expectedOut.entrySet().equals(expo.getExperimentTags().entrySet()));
	}
}