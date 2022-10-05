package com.unity3d.ads.test.instrumentation.services.core.configuration;

import com.unity3d.services.core.configuration.ExperimentObjects;
import com.unity3d.services.core.configuration.IExperiments;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExperimentObjectsTest {

	@Test
	public void testExperimentObjectsNull() {
		IExperiments expo = new ExperimentObjects(null);
		Assert.assertTrue(expo.isTwoStageInitializationEnabled());
		Assert.assertTrue(expo.isNativeTokenEnabled());
	}

	@Test
	public void testExperimentObjectsWithInvalid() throws JSONException {
		ExperimentObjects expo = new ExperimentObjects(new JSONObject("{\"something\": false}"));
		Assert.assertTrue(expo.isTwoStageInitializationEnabled());
		Assert.assertTrue(expo.isNativeTokenEnabled());
	}

	@Test
	public void testExperimentObjects() throws JSONException {
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"tsi\": {\"value\": \"true\"}}"));
		Assert.assertTrue(expo.isTwoStageInitializationEnabled());
		Assert.assertTrue(expo.isNativeTokenEnabled());
		Assert.assertFalse(expo.isForwardExperimentsToWebViewEnabled());
	}

	@Test
	public void testExperimentObjectsCurrentSession() throws JSONException {
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"tsi\": {\"value\": \"true\", \"applied\": \"next\"},"
																+ "\"fff\": {\"value\": \"true\", \"applied\": \"immediate\"}}"));
		Assert.assertFalse(expo.getCurrentSessionExperiments().has("tsi"));
		Assert.assertTrue(expo.getCurrentSessionExperiments().has("fff"));
	}

	@Test
	public void testExperimentObjectsNextSession() throws JSONException {
		IExperiments expo = new ExperimentObjects(new JSONObject("{\"tsi\": {\"value\": \"true\", \"applied\": \"next\"},"
																+ "\"fff\": {\"value\": \"true\", \"applied\": \"immediate\"}}"));
		Assert.assertTrue(expo.getNextSessionExperiments().has("tsi"));
		Assert.assertFalse(expo.getNextSessionExperiments().has("fff"));
	}
}
