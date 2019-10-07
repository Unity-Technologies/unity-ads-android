package com.unity3d.ads.test.instrumentation.services.ads.webplayer;

import com.unity3d.services.ads.webplayer.WebPlayerSettingsCache;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class WebPlayerViewSettingsCacheTest {

	private WebPlayerSettingsCache webPlayerSettingsCache;

	@Before
	public void before() {
		webPlayerSettingsCache = new WebPlayerSettingsCache();
	}

	//================================================================================
	// WebSettings
	//================================================================================

	@Test
	public void testAddWebSettings() throws Exception {
		String viewId = "view1";
		JSONObject expectedSettings = new JSONObject();
		expectedSettings.put("myValue", 1);

		webPlayerSettingsCache.addWebSettings(viewId, expectedSettings);
		JSONObject actualSettings = webPlayerSettingsCache.getWebSettings(viewId);
		assertEquals(expectedSettings, actualSettings);
		assertEquals(expectedSettings.toString(), actualSettings.toString());
	}

	@Test
	public void testRemoveWebSettings() throws Exception {
		String viewId = "view1";
		JSONObject expectedSettings = new JSONObject();
		expectedSettings.put("myValue", 1);

		webPlayerSettingsCache.addWebSettings(viewId, expectedSettings);
		webPlayerSettingsCache.removeWebSettings(viewId);
		JSONObject actualSettings = webPlayerSettingsCache.getWebSettings(viewId);
		assertNotEquals(expectedSettings, actualSettings);
		assertNotEquals(expectedSettings.toString(), actualSettings.toString());
	}

	@Test
	public void testGetWebSettingsWhenViewIdNotFound() {
		JSONObject emptySettings = webPlayerSettingsCache.getWebSettings("noView");
		assertEquals(new JSONObject().toString(), emptySettings.toString());
	}

	//================================================================================
	// WebPlayerSettings
	//================================================================================

	@Test
	public void testAddWebPlayerSettings() throws Exception {
		String viewId = "view1";
		JSONObject expectedSettings = new JSONObject();
		expectedSettings.put("myValue", 1);

		webPlayerSettingsCache.addWebPlayerSettings(viewId, expectedSettings);
		JSONObject actualSettings = webPlayerSettingsCache.getWebPlayerSettings(viewId);
		assertEquals(expectedSettings, actualSettings);
		assertEquals(expectedSettings.toString(), actualSettings.toString());
	}

	@Test
	public void testRemoveWebPlayerSettings() throws Exception {
		String viewId = "view1";
		JSONObject expectedSettings = new JSONObject();
		expectedSettings.put("myValue", 1);

		webPlayerSettingsCache.addWebPlayerSettings(viewId, expectedSettings);
		webPlayerSettingsCache.removeWebPlayerSettings(viewId);
		JSONObject actualSettings = webPlayerSettingsCache.getWebPlayerSettings(viewId);
		assertNotEquals(expectedSettings, actualSettings);
		assertNotEquals(expectedSettings.toString(), actualSettings.toString());
	}

	@Test
	public void testGetWebPlayerSettingsWhenViewIdNotFound() {
		JSONObject emptySettings = webPlayerSettingsCache.getWebPlayerSettings("noView");
		assertEquals(new JSONObject().toString(), emptySettings.toString());
	}

	//================================================================================
	// WebPlayerEventSettings
	//================================================================================

	@Test
	public void testAddWebPlayerEventSettings() throws Exception {
		String viewId = "view1";
		JSONObject expectedSettings = new JSONObject();
		expectedSettings.put("myValue", 1);

		webPlayerSettingsCache.addWebPlayerEventSettings(viewId, expectedSettings);
		JSONObject actualSettings = webPlayerSettingsCache.getWebPlayerEventSettings(viewId);
		assertEquals(expectedSettings, actualSettings);
		assertEquals(expectedSettings.toString(), actualSettings.toString());
	}

	@Test
	public void testRemoveWebPlayerEventSettings() throws Exception {
		String viewId = "view1";
		JSONObject expectedSettings = new JSONObject();
		expectedSettings.put("myValue", 1);

		webPlayerSettingsCache.addWebPlayerEventSettings(viewId, expectedSettings);
		webPlayerSettingsCache.removeWebPlayerEventSettings(viewId);
		JSONObject actualSettings = webPlayerSettingsCache.getWebPlayerEventSettings(viewId);
		assertNotEquals(expectedSettings, actualSettings);
		assertNotEquals(expectedSettings.toString(), actualSettings.toString());
	}

	@Test
	public void testGetWebPlayerEventSettingsWhenViewIdNotFound() {
		JSONObject emptySettings = webPlayerSettingsCache.getWebPlayerEventSettings("noView");
		assertEquals(new JSONObject().toString(), emptySettings.toString());
	}

}
