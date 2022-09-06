package com.unity3d.ads.test.instrumentation.services.core.configuration;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.services.core.configuration.Experiments;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class ExperimentsTest {
	private static final String TSI_TAG_INIT_ENABLED = "tsi";
	private static final String TSI_TAG_FORWARD_FEATURE_FLAGS = "fff";
	private static final String TSI_TAG_UPDATE_PII_FIELDS  = "tsi_upii";
	private static final String TSI_TAG_NATIVE_TOKEN = "tsi_nt";
	private static final String TSI_TAG_PRIVACY_REQUEST = "tsi_prr";
	private static final String TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY = "tsi_prw";
	private static final String TSI_TAG_NATIVE_WEBVIEW_CACHE = "nwc";
	private static final String TSI_TAG_WEB_AD_ASSET_CACHING = "wac";
	private static final String EXP_TAG_NEW_LIFECYCLE_TIMER = "nlt";

	@Test
	public void testExperimentsWithData() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(TSI_TAG_FORWARD_FEATURE_FLAGS, false);
		jsonObject.put(TSI_TAG_INIT_ENABLED, false);
		jsonObject.put(TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY, true);
		jsonObject.put(TSI_TAG_UPDATE_PII_FIELDS, true);
		jsonObject.put(TSI_TAG_NATIVE_TOKEN, true);
		jsonObject.put(TSI_TAG_PRIVACY_REQUEST, true);
		jsonObject.put(TSI_TAG_NATIVE_WEBVIEW_CACHE, true);
		jsonObject.put(TSI_TAG_WEB_AD_ASSET_CACHING, true);
		jsonObject.put(EXP_TAG_NEW_LIFECYCLE_TIMER, true);
		Experiments experiments = new Experiments(jsonObject);
		Assert.assertTrue(experiments.shouldNativeTokenAwaitPrivacy());
		Assert.assertFalse(experiments.isTwoStageInitializationEnabled());
		Assert.assertTrue(experiments.isPrivacyRequestEnabled());
		Assert.assertTrue(experiments.isUpdatePiiFields());
		Assert.assertTrue(experiments.isNativeTokenEnabled());
		Assert.assertTrue(experiments.isNativeWebViewCacheEnabled());
		Assert.assertTrue(experiments.isWebAssetAdCaching());
		Assert.assertTrue(experiments.isNewLifecycleTimer());
	}

	@Test
	public void testExperimentsTags() throws JSONException {
		JSONObject experimentJson = new JSONObject("{\"tsi\": true, \"fff\":false}");
		Experiments experiments = new Experiments(experimentJson);
		Map<String, String> experimentTags = experiments.getExperimentTags();
		Assert.assertNotNull(experimentTags.get(TSI_TAG_INIT_ENABLED));
		Assert.assertNotNull(experimentTags.get(TSI_TAG_FORWARD_FEATURE_FLAGS));
		Assert.assertEquals("true", experimentTags.get(TSI_TAG_INIT_ENABLED));
		Assert.assertEquals("false", experimentTags.get(TSI_TAG_FORWARD_FEATURE_FLAGS));
	}

	@Test
	public void testExperimentsTagsNoExperiments() {
		Experiments experiments = new Experiments();
		Map<String, String> experimentTags = experiments.getExperimentTags();
		Assert.assertEquals(0, experimentTags.size());
		validateDefaultExperiments(experiments);
	}

	@Test
	public void testExperimentsWithMissingData() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(TSI_TAG_FORWARD_FEATURE_FLAGS, false);

		Experiments experiments = new Experiments(jsonObject);
		Assert.assertFalse(experiments.isNativeWebViewCacheEnabled());
		Assert.assertFalse(experiments.isWebAssetAdCaching());
		Assert.assertFalse(experiments.isNewLifecycleTimer());
	}

	@Test
	public void testExperimentsWithEmptyData() {
		Experiments experiments = new Experiments(new JSONObject());
		validateDefaultExperiments(experiments);	}

	@Test
	public void testExperimentsWithNullData() {
		Experiments experiments = new Experiments(null);
		validateDefaultExperiments(experiments);	}

	@Test
	public void testExperimentsDefault() {
		Experiments experiments = new Experiments();
		validateDefaultExperiments(experiments);
	}

	@Test
	public void testExperimentsGetNextSessionExperiments() throws JSONException {
		JSONObject experimentJson = new JSONObject("{\"tsi\": true, \"fff\":false}");
		Experiments experiments = new Experiments(experimentJson);
		Assert.assertTrue(experiments.getNextSessionExperiments().getBoolean("tsi"));
	}

	@Test
	public void testExperimentsGetCurrentSessionExperiments() throws JSONException {
		JSONObject experimentJson = new JSONObject("{\"tsi\": true, \"fff\":true}");
		Experiments experiments = new Experiments(experimentJson);
		Assert.assertTrue(experiments.getCurrentSessionExperiments().getBoolean("fff"));
	}

	private void validateDefaultExperiments(Experiments experiments) {
		Assert.assertFalse(experiments.shouldNativeTokenAwaitPrivacy());
		Assert.assertFalse(experiments.isTwoStageInitializationEnabled());
		Assert.assertFalse(experiments.isNativeWebViewCacheEnabled());
		Assert.assertFalse(experiments.isWebAssetAdCaching());
		Assert.assertFalse(experiments.isNewLifecycleTimer());
	}

}
