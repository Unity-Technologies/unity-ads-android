package com.unity3d.ads.test.unit;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.view.ViewGroup;

import com.unity3d.ads.api.Connectivity;
import com.unity3d.ads.api.DeviceInfo;
import com.unity3d.ads.connectivity.ConnectivityEvent;
import com.unity3d.ads.connectivity.ConnectivityMonitor;
import com.unity3d.ads.connectivity.IConnectivityListener;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.video.VideoPlayerView;
import com.unity3d.ads.webview.WebViewApp;
import com.unity3d.ads.webview.WebViewEventCategory;
import com.unity3d.ads.webview.bridge.CallbackStatus;
import com.unity3d.ads.webview.bridge.Invocation;
import com.unity3d.ads.webview.bridge.WebViewCallback;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ConnectivityTest {
	@Before
	public void setup() {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
	}

	@Test
	public void testListener() {
		// Make sure connectivity monitor thinks it's connected when test starts
		ConnectivityMonitor.connected();

		Listener listener = new Listener();
		ConnectivityMonitor.addListener(listener);

		ConnectivityMonitor.disconnected();
		assertEquals("ConnectivityMonitor disconnected callbacks not equal to one", 1, listener.getOnDisconnectedCalls());

		ConnectivityMonitor.connected();
		assertEquals("ConnectivityMonitor connected callbacks not equal to one", 1, listener.getOnConnectedCalls());
	}

	private class Listener implements IConnectivityListener {
		private int _onConnectedCalls = 0;
		private int _onDisconnectedCalls = 0;

		@Override
		public void onConnected() {
			_onConnectedCalls++;
		}

		@Override
		public void onDisconnected() {
			_onDisconnectedCalls++;
		}

		public int getOnConnectedCalls() {
			return _onConnectedCalls;
		}

		public int getOnDisconnectedCalls() {
			return _onDisconnectedCalls;
		}
	}

	@Test
	public void testWebappEvents() {
		// Make sure connectivity monitor thinks it's connected when test starts
		ConnectivityMonitor.connected();

		final MockWebViewApp webapp = new MockWebViewApp();
		WebViewApp.setCurrentApp(webapp);
		WebViewApp.getCurrentApp().setWebAppLoaded(true);

		Handler handler = new Handler(Looper.getMainLooper());
		ConditionVariable cv = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				Connectivity.setConnectionMonitoring(true, webapp.getCallback());
				webapp.getInvocation().sendInvocationCallback();

			}
		});
		boolean success = cv.block(1000);

		assertTrue("Connectivity MockWebViewApp did not respond with success callback", webapp.getCallbackInvoked());

		cv = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				ConnectivityMonitor.disconnected();
			}
		});
		success = cv.block(1000);

		assertEquals("Connectivity MockWebViewApp did not get one disconnect event", 1, webapp.getDisconnectedEvents());

		cv = new ConditionVariable();
		handler.post(new Runnable() {
			@Override
			public void run() {
				ConnectivityMonitor.connected();
			}
		});
		success = cv.block(1000);

		assertTrue("Connectivity MockWebViewApp did not get connect event", webapp.getConnectedEvents() > 0);
	}

	private class MockWebViewApp extends WebViewApp {
		private int _disconnectedEvents = 0;
		private int _connectedEvents = 0;
		boolean _callbackInvoked = false;
		Invocation _invocation = null;
		ConditionVariable _cv = null;

		public WebViewCallback getCallback() {
			_invocation = new Invocation();
			return new WebViewCallback("1234", _invocation.getId());
		}

		public Invocation getInvocation() {
			return _invocation;
		}

		public void setCV(ConditionVariable cv) {
			_cv = cv;
		}

		@Override
		public boolean invokeCallback(Invocation invocation) {
			Object[] params = (Object[])invocation.getResponses().get(0).get(2);
			CallbackStatus status = (CallbackStatus)invocation.getResponses().get(0).get(0);
			if (params.length == 1 && status == CallbackStatus.OK) {
				_callbackInvoked = true;
			}

			openCVAndReset();

			return true;
		}

		public boolean getCallbackInvoked() {
			return _callbackInvoked;
		}

		public void openCVAndReset () {
			if (_cv != null) {
				DeviceLog.debug("Opening CV");
				_cv.open();
				_cv = null;
			}
			if (_invocation != null) {
				_invocation = null;
			}
		}

		@Override
		public boolean sendEvent(Enum eventCategory, Enum eventId, Object... params) {
			if(eventCategory != WebViewEventCategory.CONNECTIVITY) {
				throw new IllegalArgumentException("Event category not CONNECTIVITY");
			}

			DeviceLog.debug("EVENT: " + eventId.name());

			switch((ConnectivityEvent)eventId) {
				case CONNECTED:
					_connectedEvents++;
					openCVAndReset();
					break;

				case DISCONNECTED:
					_disconnectedEvents++;
					openCVAndReset();
					break;

				case NETWORK_CHANGE:
					// These events might come in random times so just ignore them so make tests stable
					break;

				default:
					throw new IllegalArgumentException("Connectivity test: Event ID not CONNECTED, DISCONNECTED or NETWORK_CHANGE");
			}

			return true;
		}

		public int getConnectedEvents() {
			return _connectedEvents;
		}

		public int getDisconnectedEvents() {
			return _disconnectedEvents;
		}
	}
}