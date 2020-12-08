package com.unity3d.ads.test.legacy;

import com.unity3d.ads.UnityAdsShowOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ShowOptionsTest {
	@Test
	public void testAllKeys() throws JSONException {
		String objectId = "1234";

		UnityAdsShowOptions options = new UnityAdsShowOptions();

		options.setObjectId(objectId);

		JSONObject result = options.getData();

		assertNotNull("UnityAdsShowOptions with object ID should not be null", result);
		assertEquals("UnityAdsShowOptions number of fields is incorrect", 1, result.length());
		assertTrue("UnityAdsShowOptions should have object_id field", result.has("objectId"));
		assertEquals("UnityAdsShowOptions object_id field is incorrect", objectId, result.getString("objectId"));
	}

	@Test
	public void testEmptyObject() {
		UnityAdsShowOptions options = new UnityAdsShowOptions();

		JSONObject result = options.getData();
		assertNotNull("UnityAdsShowOptions with empty fields should not be null", result);
		assertEquals("UnityAdsShowOptions number of fields for empty object is not zero", 0, result.length());
	}
}
