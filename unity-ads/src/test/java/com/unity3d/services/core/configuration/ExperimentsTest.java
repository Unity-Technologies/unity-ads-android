package com.unity3d.services.core.configuration;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class ExperimentsTest {
	private static final String TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY = "tsi_prw";
	private static final String TSI_TAG_NATIVE_WEBVIEW_CACHE = "nwc";
	private static final String TSI_TAG_WEB_AD_ASSET_CACHING = "wac";
	private static final String EXP_TAG_SCAR_INIT = "scar_init";
	private static final String EXP_TAG_NEW_INIT_FLOW = "s_init";

	private static final String EXP_TAG_SCAR_BIDDING_MANAGER = "scar_bm";

	private static final String EXP_TAG_JETPACK_LIFECYCLE = "gjl";

	@Test
	public void testExperimentsWithData() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(TSI_TAG_NATIVE_TOKEN_AWAIT_PRIVACY, true);
		jsonObject.put(TSI_TAG_NATIVE_WEBVIEW_CACHE, true);
		jsonObject.put(TSI_TAG_WEB_AD_ASSET_CACHING, true);
		jsonObject.put(EXP_TAG_SCAR_INIT, true);
		jsonObject.put(EXP_TAG_NEW_INIT_FLOW, true);
		jsonObject.put(EXP_TAG_SCAR_BIDDING_MANAGER, "laz");
		jsonObject.put(EXP_TAG_JETPACK_LIFECYCLE, true);
		Experiments experiments = new Experiments(jsonObject);
		Assert.assertTrue(experiments.shouldNativeTokenAwaitPrivacy());
		Assert.assertTrue(experiments.isNativeWebViewCacheEnabled());
		Assert.assertTrue(experiments.isWebAssetAdCaching());
		Assert.assertTrue(experiments.isScarInitEnabled());
		Assert.assertTrue(experiments.isNewInitFlowEnabled());
		Assert.assertEquals("laz", experiments.getScarBiddingManager());
	}

	@Test
	public void testExperimentsTags() throws JSONException {
		JSONObject experimentJson = new JSONObject("{\"s_init\": true, \"wac\":false}");
		Experiments experiments = new Experiments(experimentJson);
		Map<String, String> experimentTags = experiments.getExperimentTags();
		Assert.assertNotNull(experimentTags.get(EXP_TAG_NEW_INIT_FLOW));
		Assert.assertNotNull(experimentTags.get(TSI_TAG_WEB_AD_ASSET_CACHING));
		Assert.assertEquals("true", experimentTags.get(EXP_TAG_NEW_INIT_FLOW));
		Assert.assertEquals("false", experimentTags.get(TSI_TAG_WEB_AD_ASSET_CACHING));
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
		jsonObject.put(TSI_TAG_WEB_AD_ASSET_CACHING, false);

		Experiments experiments = new Experiments(jsonObject);
		Assert.assertFalse(experiments.isNativeWebViewCacheEnabled());
		Assert.assertFalse(experiments.isWebAssetAdCaching());
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
		JSONObject experimentJson = new JSONObject("{\"wac\":false, \"s_init\":true}");
		Experiments experiments = new Experiments(experimentJson);
		Assert.assertFalse(experiments.getNextSessionExperiments().optBoolean("wac"));
		Assert.assertTrue(experiments.getNextSessionExperiments().optBoolean("s_init"));
	}

	@Test
	public void testExperimentsGetCurrentSessionExperiments() throws JSONException {
		JSONObject experimentJson = new JSONObject("{\"wac\":true, \"s_init\":true}");
		Experiments experiments = new Experiments(experimentJson);
		Assert.assertTrue(experiments.getCurrentSessionExperiments().optBoolean("wac"));
		Assert.assertFalse(experiments.getCurrentSessionExperiments().optBoolean("s_init"));
	}

	private void validateDefaultExperiments(Experiments experiments) {
		Assert.assertFalse(experiments.shouldNativeTokenAwaitPrivacy());
		Assert.assertFalse(experiments.isNativeWebViewCacheEnabled());
		Assert.assertFalse(experiments.isWebAssetAdCaching());
		Assert.assertFalse(experiments.isScarInitEnabled());
		Assert.assertFalse(experiments.isNewInitFlowEnabled());
		Assert.assertEquals("dis", experiments.getScarBiddingManager());
		Assert.assertFalse(experiments.isJetpackLifecycle());
	}

}
