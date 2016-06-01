package com.unity3d.ads.test.unit;

import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.UnityAds;
import com.unity3d.ads.api.Placement;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.bridge.CallbackStatus;
import com.unity3d.ads.webview.bridge.Invocation;
import com.unity3d.ads.webview.bridge.WebViewCallback;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class PlacementTest {
	@Test
	public void testReset() {
		SdkProperties.setInitialized(false);
		Placement.reset();

		assertFalse("Placement reset test: default placement isReady not false", Placement.isReady());
		assertFalse("Placement reset test: random placement isReady not false", Placement.isReady("1234"));
		assertEquals("Placement reset test: default placement getPlacementState is not NOT_AVAILABLE", Placement.getPlacementState(), UnityAds.PlacementState.NOT_AVAILABLE);
		assertEquals("Placement reset test: random placement getPlacementState is not NOT_AVAILABLE", Placement.getPlacementState("1234"), UnityAds.PlacementState.NOT_AVAILABLE);
		assertNull("Placement reset test: default placement defined after reset", Placement.getDefaultPlacement());
	}

	@Test
	public void testDefaultPlacement() {
		String testPlacement = "testPlacement";

		Placement.reset();
		SdkProperties.setInitialized(true);
		assertFalse("Default placement test: default placement ready when not configured", UnityAds.isReady());
		assertEquals("Default placement test: default placement state is not NOT_AVAILABLE when not configured", UnityAds.getPlacementState(), UnityAds.PlacementState.NOT_AVAILABLE);

		MockWebViewApp webapp1 = new MockWebViewApp();
		WebViewApp.setCurrentApp(webapp1);

		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("1234", invocation.getId());
		Placement.setDefaultPlacement("testPlacement", callback);
		invocation.sendInvocationCallback();

		assertTrue("Default placement test: default placement not set successfully", webapp1.getCallbackInvoked());
		assertEquals("Default placement test: placement names do not match", testPlacement, Placement.getDefaultPlacement());

		MockWebViewApp webapp2 = new MockWebViewApp();
		WebViewApp.setCurrentApp(webapp2);

		Invocation invocation2 = new Invocation();
		WebViewCallback callback2 = new WebViewCallback("1234", invocation2.getId());
		Placement.setPlacementState(testPlacement, UnityAds.PlacementState.WAITING.name(), callback2);
		invocation2.sendInvocationCallback();

		assertTrue("Default placement test: placement not successfully set to waiting state", webapp2.getCallbackInvoked());
		assertFalse("Default placement test: placement is ready when it should not be ready due to waiting state", UnityAds.isReady());
		assertEquals("Default placement test: placement is not waiting", UnityAds.getPlacementState(), UnityAds.PlacementState.WAITING);

		MockWebViewApp webapp3 = new MockWebViewApp();
		WebViewApp.setCurrentApp(webapp3);
		Invocation invocation3 = new Invocation();
		WebViewCallback callback3 = new WebViewCallback("1234", invocation3.getId());
		Placement.setPlacementState(testPlacement, UnityAds.PlacementState.READY.name(), callback3);
		invocation3.sendInvocationCallback();

		assertTrue("Default placement test: placement not successfully set to ready state", webapp3.getCallbackInvoked());
		assertTrue("Default placement test: placement is not ready", UnityAds.isReady());
		assertEquals("Default placement test: placement status is not ready", UnityAds.getPlacementState(), UnityAds.PlacementState.READY);
	}

	public void testCustomPlacement() {
		String customPlacement = "customPlacement";

		Placement.reset();
		SdkProperties.setInitialized(true);
		assertFalse("Custom placement test: placement is ready before placement configuration", UnityAds.isReady(customPlacement));
		assertEquals("Custom placement test: placement state is not NOT_AVAILABLE before placement configuration", UnityAds.getPlacementState(customPlacement), UnityAds.PlacementState.NOT_AVAILABLE);

		MockWebViewApp webapp1 = new MockWebViewApp();
		WebViewApp.setCurrentApp(webapp1);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("1234", invocation.getId());
		Placement.setPlacementState(customPlacement, UnityAds.PlacementState.DISABLED.name(), callback);
		invocation.sendInvocationCallback();
		assertTrue("Custom placement test: placement state disabled callback not invoked", webapp1.getCallbackInvoked());
		assertFalse("Custom placement test: placement is ready but placement state disabled means placement should not be ready", UnityAds.isReady(customPlacement));
		assertEquals("Custom placement test: placement state is not DISABLED after disabling", UnityAds.getPlacementState(customPlacement), UnityAds.PlacementState.DISABLED);

		MockWebViewApp webapp2 = new MockWebViewApp();
		WebViewApp.setCurrentApp(webapp2);
		Invocation invocation2 = new Invocation();
		WebViewCallback callback2 = new WebViewCallback("1234", invocation2.getId());
		Placement.setPlacementState(customPlacement, UnityAds.PlacementState.NO_FILL.name(), callback2);
		invocation2.sendInvocationCallback();

		assertTrue("Custom placement test: placement state no fill callback not invoked", webapp2.getCallbackInvoked());
		assertFalse("Custom placement test: placement is ready but placement state no fill means placement should not be ready", UnityAds.isReady(customPlacement));
		assertEquals("Custom placement test: placement state is not NO_FILL after set to no fill", UnityAds.getPlacementState(customPlacement), UnityAds.PlacementState.NO_FILL);

		MockWebViewApp webapp3 = new MockWebViewApp();
		WebViewApp.setCurrentApp(webapp3);
		Invocation invocation3 = new Invocation();
		WebViewCallback callback3 = new WebViewCallback("1234", invocation3.getId());
		Placement.setPlacementState(customPlacement, UnityAds.PlacementState.WAITING.name(), callback3);
		invocation3.sendInvocationCallback();
		assertTrue("Custom placement test: placement state waiting callback not invoked", webapp3.getCallbackInvoked());
		assertFalse("Custom placement test: placement is ready but placement state waiting means placement should not be ready", UnityAds.isReady(customPlacement));
		assertEquals("Custom placement test: placement state is not WAITING after set to waiting", UnityAds.getPlacementState(customPlacement), UnityAds.PlacementState.WAITING);

		MockWebViewApp webapp4 = new MockWebViewApp();
		WebViewApp.setCurrentApp(webapp4);
		Invocation invocation4 = new Invocation();
		WebViewCallback callback4 = new WebViewCallback("1234", invocation4.getId());
		Placement.setPlacementState(customPlacement, UnityAds.PlacementState.READY.name(), callback4);
		invocation4.sendInvocationCallback();
		assertTrue("Custom placement test: placement state ready callback not invoked", webapp4.getCallbackInvoked());
		assertTrue("Custom placement test: placement is not ready but placement state ready means placement should be ready", UnityAds.isReady(customPlacement));
		assertEquals("Custom placement test: placement state is not READY after set to ready", UnityAds.getPlacementState(customPlacement), UnityAds.PlacementState.READY);
	}

	@Test
	public void testPlacementAnalytics() {
		String analyticsPlacement = "analyticsPlacement";

		Placement.reset();
		SdkProperties.setInitialized(true);

		MockWebViewApp webapp1 = new MockWebViewApp();
		WebViewApp.setCurrentApp(webapp1);
		Invocation invocation = new Invocation();
		WebViewCallback callback = new WebViewCallback("1234", invocation.getId());
		Placement.setPlacementAnalytics(true, callback);
		invocation.sendInvocationCallback();
		assertTrue("Placement analytics: setPlacementAnalytics success callback not invoked", webapp1.getCallbackInvoked());

		AnalyticsWebViewApp webapp2 = new AnalyticsWebViewApp();
		WebViewApp.setCurrentApp(webapp2);
		Invocation invocation2 = new Invocation();
		WebViewCallback callback2 = new WebViewCallback("1234", invocation2.getId());
		Placement.setPlacementState(analyticsPlacement, UnityAds.PlacementState.WAITING.name(), callback2);
		invocation2.sendInvocationCallback();

		assertTrue("Placement analytics test: placement set to waiting state, callback not invoked", webapp2.getCallbackInvoked());

		UnityAds.PlacementState waitingState = UnityAds.getPlacementState(analyticsPlacement);

		assertEquals("Placement analytics test: placement is not in waiting state", waitingState, UnityAds.PlacementState.WAITING);

		Object[] analyticsParams = webapp2.getAnalyticsParams();
		assertNotNull("Placement analytics test: placement analytics is not sent", analyticsParams);
		assertEquals("Placement analytics test: analytics parameters do not have two arguments", analyticsParams.length, 2);
		assertEquals("Placement analytics test: first argument is not placement name", analyticsPlacement, (String) analyticsParams[0]);
		assertEquals("Placement analytics test: second argument is not placement state", UnityAds.PlacementState.WAITING.name(), (String) analyticsParams[1]);

		AnalyticsWebViewApp webapp3 = new AnalyticsWebViewApp();
		WebViewApp.setCurrentApp(webapp3);
		UnityAds.PlacementState waitingState2 = UnityAds.getPlacementState(analyticsPlacement);
		assertEquals("Placement analytics test: placement is still not in waiting state", waitingState2, UnityAds.PlacementState.WAITING);
		assertNull("Placement analytics test: analytics event sent twice", webapp3.getAnalyticsParams());

		AnalyticsWebViewApp webapp4 = new AnalyticsWebViewApp();
		WebViewApp.setCurrentApp(webapp4);
		Invocation invocation3 = new Invocation();
		WebViewCallback callback3 = new WebViewCallback("1234", invocation3.getId());
		Placement.setPlacementState(analyticsPlacement, UnityAds.PlacementState.READY.name(), callback3);
		invocation3.sendInvocationCallback();

		assertTrue("Placement analytics test: placement set to ready state, callback not invoked", webapp4.getCallbackInvoked());

		UnityAds.PlacementState readyState = UnityAds.getPlacementState(analyticsPlacement);

		assertEquals("Placement analytics test: placement is not in ready state", readyState, UnityAds.PlacementState.READY);

		Object[] analyticsParams2 = webapp4.getAnalyticsParams();
		assertNotNull("Placement analytics test: when state has changed, placement analytics was not sent", analyticsParams2);
		assertEquals("Placement analytics test: when state has changed, analytics parameters do not have two arguments", analyticsParams2.length, 2);
		assertEquals("Placement analytics test: when state has changed, first argument is not placement name", analyticsPlacement, (String) analyticsParams2[0]);
		assertEquals("Placement analytics test: when state has changed, second argument is not placement state", UnityAds.PlacementState.READY.name(), (String) analyticsParams2[1]);
	}

	public class MockWebViewApp extends WebViewApp {
		boolean _callbackInvoked = false;

		@Override
		public boolean invokeCallback(Invocation invocation) {
			Object[] params = (Object[])invocation.getResponses().get(0).get(2);
			CallbackStatus status = (CallbackStatus)invocation.getResponses().get(0).get(0);
			if (params.length == 1 && status == CallbackStatus.OK) {
				_callbackInvoked = true;
			}

			return super.invokeCallback(invocation);
		}

		public boolean getCallbackInvoked() {
			return _callbackInvoked;
		}
	}

	public class AnalyticsWebViewApp extends MockWebViewApp {
		Object analyticsParams[] = null;

		@Override
		public boolean invokeMethod(String className, String methodName, Method callback, Object... params) {
			if(className.equals("webview") && methodName.equals("placementAnalytics") && callback == null) {
				analyticsParams = params;
			}

			return true;
		}

		public Object[] getAnalyticsParams() {
			return analyticsParams;
		}
	}
}