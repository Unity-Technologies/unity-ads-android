package com.unity3d.ads.test.unit;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.unity3d.ads.configuration.Configuration;
import com.unity3d.ads.configuration.InitializeThread;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.ads.webview.WebView;
import com.unity3d.ads.webview.WebViewApp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class InitializeThreadTest {
	private static final String _testConfigUrl = "https://www.example.net/test/webview.html";
	private String _testConfigHash = "12345";
	private Class[] _apiClassList = {com.unity3d.ads.api.Sdk.class};

	private WebView webview;

	@Before
	public void setup() throws MalformedURLException, URISyntaxException {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getTargetContext());
		SdkProperties.setConfigUrl(TestUtilities.getTestServerAddress() + "/testconfig.json");
	}

	@Test
	public void testInitializeStateReset() throws InterruptedException {
		final ConditionVariable cv = new ConditionVariable();

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				webview = new WebView(InstrumentationRegistry.getTargetContext());
				cv.open();
			}
		});
		cv.block(30000);

		WebViewApp.setCurrentApp(new WebViewApp());
		WebViewApp.getCurrentApp().setWebView(webview);
		WebViewApp.getCurrentApp().setWebAppLoaded(true);
		SdkProperties.setInitialized(true);

		Configuration initConfig = new Configuration();
		initConfig.setWebAppApiClassList(_apiClassList);
		InitializeThread.InitializeStateReset state = new InitializeThread.InitializeStateReset(initConfig);
		Object nextState = state.execute();

		assertFalse("Init state reset test: SDK is initialized after SDK was reset", SdkProperties.isInitialized());
		assertFalse("Init state reset test: webapp is loaded after SDK was reset", WebViewApp.getCurrentApp().isWebAppLoaded());
		assertTrue("Init state reset test: next state is not config", nextState instanceof InitializeThread.InitializeStateAdBlockerCheck);

		Configuration config = ((InitializeThread.InitializeStateAdBlockerCheck)nextState).getConfiguration();

		assertEquals("Init state reset test: next state config url is not set", config.getConfigUrl(), SdkProperties.getConfigUrl());
	}

	@Test
	public void testInitializeStateAdBlockerCheck() {
		Configuration goodConfig = new Configuration();
		goodConfig.setConfigUrl("http://www.unity3d.com/test");
		InitializeThread.InitializeStateAdBlockerCheck state = new InitializeThread.InitializeStateAdBlockerCheck(goodConfig);
		Object nextState = state.execute();

		assertTrue("Init state ad blocker check test: next state is not load config", nextState instanceof InitializeThread.InitializeStateConfig);

		Configuration badConfig = new Configuration();
		badConfig.setConfigUrl("http://localhost/test");
		InitializeThread.InitializeStateAdBlockerCheck state2 = new InitializeThread.InitializeStateAdBlockerCheck(badConfig);
		Object nextState2 = state2.execute();

		assertNull("Init state ad blocker check test: next state is not null", nextState2);
	}

	@Test
	public void testInitializeStateConfig() {
		Configuration initConfig = new Configuration();
		initConfig.setConfigUrl(SdkProperties.getConfigUrl());
		InitializeThread.InitializeStateConfig state = new InitializeThread.InitializeStateConfig(initConfig);
		Object nextState = state.execute();

		assertTrue("Init state config test: next state is not load cache", nextState instanceof InitializeThread.InitializeStateLoadCache);

		Configuration config = ((InitializeThread.InitializeStateLoadCache)nextState).getConfiguration();

		assertEquals("Init state config test: config webview url does not match url in testconfig.json", config.getWebViewUrl(), _testConfigUrl);
		assertEquals("Init state config test: config webview hash does not match hash in testconfig.json", config.getWebViewHash(), _testConfigHash);
	}

	// Test for cache load fail case, success case is handled in load web test
	@Test
	public void testInitializeStateLoadCache() {
		Configuration bogusConfig = new Configuration();
		bogusConfig.setWebAppApiClassList(_apiClassList);
		bogusConfig.setWebViewUrl(_testConfigUrl);
		bogusConfig.setWebViewHash(_testConfigHash);

		InitializeThread.InitializeStateLoadCache state = new InitializeThread.InitializeStateLoadCache(bogusConfig);
		Object nextState = state.execute();

		assertTrue("Init state load cache test: init is not loading from web with bogus config ", nextState instanceof InitializeThread.InitializeStateLoadWeb);

		Configuration webConfig = ((InitializeThread.InitializeStateLoadWeb)nextState).getConfiguration();

		assertEquals("Init state load cache test: load web config url is not correct", webConfig.getWebViewUrl(), _testConfigUrl);
		assertEquals("Init state load cache test: load web config hash is not correct", webConfig.getWebViewHash(), _testConfigHash);
	}

	@Test
	public void testInitializeStateLoadWeb() {
		String webUrl = TestUtilities.getTestServerAddress() + "/testwebapp.html";
		String webData = "<p>Test web app</p>\n"; // Contents of testwebapp.html on test server
		String webHash = Utilities.Sha256(webData);
		Configuration webConfig = new Configuration();
		webConfig.setWebAppApiClassList(_apiClassList);
		webConfig.setWebViewUrl(webUrl);
		webConfig.setWebViewHash(webHash);

		InitializeThread.InitializeStateLoadWeb state = new InitializeThread.InitializeStateLoadWeb(webConfig);
		Object nextState = state.execute();

		assertTrue("Init state load web test: next state is not create", nextState instanceof InitializeThread.InitializeStateCreate);

		Configuration createConfig = ((InitializeThread.InitializeStateCreate)nextState).getConfiguration();

		assertEquals("Init state load web test: original webview url does not match created url", webUrl, createConfig.getWebViewUrl());
		assertEquals("Init state load web test: original webview hash does not match created hash", webHash, createConfig.getWebViewHash());

		String createWebData = ((InitializeThread.InitializeStateCreate)nextState).getWebData();

		assertEquals("Init state load web test: original webview content does not match created webview content", webData, createWebData);

		InitializeThread.InitializeStateLoadCache state2 = new InitializeThread.InitializeStateLoadCache(webConfig);
		Object nextState2 = state2.execute();

		assertTrue("Init state load web test: webapp was not successfully cached", nextState2 instanceof InitializeThread.InitializeStateCreate);

		Configuration createConfig2 = ((InitializeThread.InitializeStateCreate)nextState2).getConfiguration();

		assertEquals("Init state load web test: cached webview url does not match created url", webUrl, createConfig2.getWebViewUrl());
		assertEquals("Init state load web test: cached webview hash does not match created hash", webHash, createConfig2.getWebViewHash());

		String createWebData2 = ((InitializeThread.InitializeStateCreate)nextState2).getWebData();

		assertEquals("Init state load web test: cached webview content does not match created webview content", webData, createWebData2);
	}

	@Test
	public void testInitializeStateCreate() {
		String url = "http://www.example.net/handlecallback.html"; // This url is never used

		// This is very hackish but also reproducing the full init procedure would be very fragile
		String data = "<script>var nativebridge = new Object(); nativebridge.handleCallback = new function() { " +
				"webviewbridge.handleInvocation(\"[['com.unity3d.ads.api.Sdk','initComplete', [], 'CALLBACK_01']]\");" +
				"}</script>";
		String hash = Utilities.Sha256(data);

		Configuration config = new Configuration();
		config.setWebAppApiClassList(_apiClassList);
		config.setWebViewUrl(url);
		config.setWebViewHash(hash);
		config.setWebViewData(hash);

		InitializeThread.InitializeStateCreate state = new InitializeThread.InitializeStateCreate(config, data);
		Object nextState = state.execute();

		assertTrue("Init state create test: next state is not complete", nextState instanceof InitializeThread.InitializeStateComplete);
	}

	@Test
	public void testInitializeStateComplete() {
		InitializeThread.InitializeStateComplete state = new InitializeThread.InitializeStateComplete();
		Object nextState = state.execute();

		assertNull("Init state complete test: next step is not null", nextState);
	}

	@Test
	public void testInitializeStateRetry() {
		InitializeThread.InitializeStateRetry state = new InitializeThread.InitializeStateRetry(new InitializeThread.InitializeStateComplete(), 5);
		long startTime = SystemClock.elapsedRealtime();
		Object nextState = state.execute();
		long endTime = SystemClock.elapsedRealtime();

		assertTrue("Init state retry test: retry state did not return proper next state", nextState instanceof InitializeThread.InitializeStateComplete);
		assertFalse("Init state retry test: retry delay is less than four seconds (should be five seconds)", (endTime - startTime) < 4000);
		assertFalse("Init state retry test: retry delay is greater than six seconds (should be five seconds)", (endTime - startTime) > 6000);
	}
}