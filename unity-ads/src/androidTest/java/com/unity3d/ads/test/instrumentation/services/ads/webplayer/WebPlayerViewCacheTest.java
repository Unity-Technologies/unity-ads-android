package com.unity3d.ads.test.instrumentation.services.ads.webplayer;

import android.app.Activity;
import androidx.test.annotation.UiThreadTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.unity3d.ads.test.legacy.TestActivity;
import com.unity3d.services.ads.webplayer.WebPlayerView;
import com.unity3d.services.ads.webplayer.WebPlayerViewCache;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class WebPlayerViewCacheTest {

	@Rule
	public ActivityTestRule<TestActivity> activityTestRule = new ActivityTestRule<>(TestActivity.class);

	private WebPlayerViewCache webPlayerViewCache;

	@Before
	public void before() {
		webPlayerViewCache = new WebPlayerViewCache();
	}

	@Test
	@UiThreadTest
	public void testAddWebPlayer() {
		final Activity activity = activityTestRule.getActivity();
		String viewId = "webplayer";
		WebPlayerView expectedWebPlayerView = new WebPlayerView(activity, viewId, new JSONObject(), new JSONObject());
		webPlayerViewCache.addWebPlayer(viewId, expectedWebPlayerView);
		WebPlayerView actualWebPlayerView = webPlayerViewCache.getWebPlayer(viewId);
		assertEquals(expectedWebPlayerView, actualWebPlayerView);
	}

	@Test
	@UiThreadTest
	public void testRemoveWebPlayer() {
		final Activity activity = activityTestRule.getActivity();
		String viewId = "webplayer";
		WebPlayerView expectedWebPlayerView = new WebPlayerView(activity, viewId, new JSONObject(), new JSONObject());
		webPlayerViewCache.addWebPlayer(viewId, expectedWebPlayerView);
		webPlayerViewCache.removeWebPlayer(viewId);
		WebPlayerView actualWebPlayerView = webPlayerViewCache.getWebPlayer(viewId);
		assertNull(actualWebPlayerView);
	}

	@Test
	public void testGetWebPlayerWhenViewIdNotFound() {
		WebPlayerView webPlayerView = webPlayerViewCache.getWebPlayer("webplayer");
		assertNull(webPlayerView);
	}

}
