package com.unity3d.ads.test.instrumentation.services.ads.load;

import android.support.test.runner.AndroidJUnit4;

import com.unity3d.services.ads.load.LoadBridge;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.WebViewEventCategory;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LoadBridgeTest {
	private WebViewApp webViewApp;
	private LoadBridge loadBridge;

	@Before
	public void before() {
		// TODO mock webview correctly
		webViewApp = mock(WebViewApp.class);
		WebViewApp.setCurrentApp(webViewApp);

		loadBridge = new LoadBridge();
	}

	@After
	public void after() {
		WebViewApp.setCurrentApp(null);
	}

	@Test
	public void testLoadPlacements() throws Exception {
		LinkedHashMap<String, Integer> loadMap = new LinkedHashMap<>();
		loadMap.put("test1", new Integer(1));
		loadMap.put("test2", new Integer(2));

		JSONObject expected = new JSONObject();
		expected.put("test1", 1);
		expected.put("test2", 2);

		loadBridge.loadPlacements(loadMap);
		ArgumentCaptor<WebViewEventCategory> argument1Captor = ArgumentCaptor.forClass(WebViewEventCategory.class);
		ArgumentCaptor<LoadBridge.LoadEvent> argument2Captor = ArgumentCaptor.forClass(LoadBridge.LoadEvent.class);
		ArgumentCaptor<JSONObject> argument3Captor = ArgumentCaptor.forClass(JSONObject.class);

		verify(webViewApp, times(1)).sendEvent(argument1Captor.capture(), argument2Captor.capture(), argument3Captor.capture());
		assertEquals(WebViewEventCategory.LOAD_API, argument1Captor.getValue());
		assertEquals(LoadBridge.LoadEvent.LOAD_PLACEMENTS, argument2Captor.getValue());
		assertEquals(expected.toString(), argument3Captor.getValue().toString());
	}

	@Test
	public void testLoadPlacementsWithoutWebView() {
		WebViewApp.setCurrentApp(null);

		LinkedHashMap<String, Integer> loadMap = new LinkedHashMap<>();
		loadMap.put("test1", new Integer(1));
		loadMap.put("test2", new Integer(2));

		verify(webViewApp, times(0)).sendEvent(any(WebViewEventCategory.class), any(LoadBridge.LoadEvent.class), any(LinkedHashMap.class));
	}
}
