package com.unity3d.ads.test.instrumentation.services.core.configuration;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.core.configuration.Experiments;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ExperimentsTest {

	@Test
	public void testExperimentsWithData() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("fff", false);
		jsonObject.put("tsi", false);
		jsonObject.put("tsi_dc", true);
		jsonObject.put("tsi_epii", false);
		jsonObject.put("tsi_p", false);

		Experiments experiments = new Experiments(jsonObject);
		Assert.assertTrue(experiments.isHandleDeveloperConsent());
		Assert.assertFalse(experiments.isTwoStageInitializationEnabled());
	}

	@Test
	public void testExperimentsWithMissingData() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("fff", false);

		Experiments experiments = new Experiments(jsonObject);
		Assert.assertFalse(experiments.isHandleDeveloperConsent());
	}

	@Test
	public void testExperimentsWithEmptyData() {
		JSONObject jsonObject = new JSONObject();

		Experiments experiments = new Experiments(jsonObject);
		Assert.assertFalse(experiments.isHandleDeveloperConsent());
	}

	@Test
	public void testExperimentsWithNullData() {
		Experiments experiments = new Experiments(null);
		Assert.assertFalse(experiments.isHandleDeveloperConsent());
	}

	@Test
	public void testExperimentsDefault() {
		Experiments experiments = new Experiments();
		Assert.assertFalse(experiments.isHandleDeveloperConsent());
	}

}
