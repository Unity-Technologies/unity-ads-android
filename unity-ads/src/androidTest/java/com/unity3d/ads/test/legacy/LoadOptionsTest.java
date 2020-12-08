package com.unity3d.ads.test.legacy;

import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.UnityAdsLoadOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class LoadOptionsTest  {
	@Test
	public void testAllKeys() throws JSONException {
		String objectId = "1234";
		String adm = "<ad>Test</ad>";

		UnityAdsLoadOptions options = new UnityAdsLoadOptions();

		options.setAdMarkup(adm);
		options.setObjectId(objectId);

		JSONObject result = options.getData();

		assertNotNull("UnityAdsLoadOptions with adm and object ID should not be null", result);
		assertEquals("UnityAdsLoadOptions number of fields is incorrect", 2, result.length());
		assertTrue("UnityAdsLoadOptions should have ad_markup field", result.has("adMarkup"));
		assertTrue("UnityAdsLoadOptions should have object_id field", result.has("objectId"));
		assertEquals("UnityAdsLoadOptions ad_markup field is incorrect", adm, result.getString("adMarkup"));
		assertEquals("UnityAdsLoadOptions object_id field is incorrect", objectId, result.getString("objectId"));
	}

	@Test
	public void testEmptyObject() {
		UnityAdsLoadOptions options = new UnityAdsLoadOptions();

		JSONObject result = options.getData();
		assertNotNull("UnityAdsLoadOptions with empty fields should not be null", result);
		assertEquals("UnityAdsLoadOptions number of fields for empty object is not zero", 0, result.length());
	}
}
