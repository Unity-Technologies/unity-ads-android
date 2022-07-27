package com.unity3d.ads.test.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.test.TestUtilities;
import com.unity3d.services.core.api.DownloadLatestWebViewStatus;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ErrorState;
import com.unity3d.services.core.configuration.Experiments;
import com.unity3d.services.core.configuration.ExperimentsReader;
import com.unity3d.services.core.configuration.InitializeThread;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.webview.WebView;
import com.unity3d.services.core.webview.WebViewApp;

import org.hamcrest.core.Is;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

@RunWith(AndroidJUnit4.class)
public class InitializeThreadTest {
	private static final String TEST_WEBVIEW_URL = "https://www.example.net/test/webview.html";
	private String _testConfigHash = "12345";
	private WebView webview;

	@Before
	public void setup() throws MalformedURLException, URISyntaxException {
		ClientProperties.setApplicationContext(InstrumentationRegistry.getInstrumentation().getTargetContext());
		SdkProperties.setConfigUrl(TestUtilities.getTestServerAddress() + "/testconfig.json");
		SdkProperties.setLatestConfiguration(null);
		DeleteAllTempFiles();
	}

	@After
	public void cleanup() {
		DeleteAllTempFiles();
	}

	private void DeleteAllTempFiles() {
		new File(SdkProperties.getLocalWebViewFile()).delete();
		new File(SdkProperties.getLocalWebViewFileUpdated()).delete();
		new File(SdkProperties.getLocalConfigurationFilepath()).delete();
	}

	private JSONObject getJSONObject(String webViewUrl, String hash, String version) {
		JSONObject json = new JSONObject();

		try {
			json.put("url", webViewUrl);
			json.put("hash", hash);
			json.put("version", version);
		} catch (Exception e) {
			return json;
		}

		return json;
	}

	@Test
	public void testInitializeStateLoadConfigFile() throws Exception {
		String filePath = SdkProperties.getLocalConfigurationFilepath();
		JSONObject json = getJSONObject("fake-url", "fake-hash", "fake-version");
		json.put("mr", 1);
		json.put("rcf", 2);
		json.put("rd", 3);
		json.put("sto", 4);
		json.put("lto", 5);
		json.put("wto", 6);
		Configuration initConfig = new Configuration(json);
		boolean fileWritten = Utilities.writeFile(new File(filePath), initConfig.getJSONString());
		assertTrue("File was not written properly", fileWritten);

		InitializeThread.InitializeStateLoadConfigFile state = new InitializeThread.InitializeStateLoadConfigFile(initConfig);
		Object nextState = state.execute();

		assertTrue("Next state is not InitializeStateReset", nextState instanceof InitializeThread.InitializeStateReset);

		Configuration configuration = ((InitializeThread.InitializeStateReset)nextState).getConfiguration();
		assertEquals("Max Retries not properly overridden", 1, configuration.getMaxRetries());
		assertEquals("Retry Scaling Factor not properly overridden", 2, configuration.getRetryScalingFactor(), 0);
		assertEquals("Retry Delay not properly overridden", 3, configuration.getRetryDelay());
		assertEquals("Show timeout not properly overridden", 4, configuration.getShowTimeout());
		assertEquals("Load timeout not properly overridden", 5, configuration.getLoadTimeout());
		assertEquals("No fill timeout not properly overridden", 6, configuration.getWebViewBridgeTimeout());
	}

	@Test
	public void testInitializeStateLoadConfigFileNoConfigExists() {
		String filePath = SdkProperties.getLocalConfigurationFilepath();
		boolean fileExists = new File(filePath).exists();
		assertFalse("File should not exist", fileExists);

		Configuration initConfig = new Configuration();
		InitializeThread.InitializeStateLoadConfigFile state = new InitializeThread.InitializeStateLoadConfigFile(initConfig);
		Object nextState = state.execute();

		assertTrue("Next state is not InitializeStateReset", nextState instanceof InitializeThread.InitializeStateReset);

		Configuration configuration = ((InitializeThread.InitializeStateReset)nextState).getConfiguration();
		assertEquals("Max Retries not return default value", 6, configuration.getMaxRetries());
		assertEquals("Retry Scaling Factor not return default value", 2, configuration.getRetryScalingFactor(), 0);
		assertEquals("Retry Delay not return default value", 5000L, configuration.getRetryDelay());
		assertEquals("Show timeout not return default value", 10000, configuration.getShowTimeout());
		assertEquals("Load timeout not return default value", 30000, configuration.getLoadTimeout());
		assertEquals("No fill timeout not return default value", 5000, configuration.getWebViewBridgeTimeout());

	}

	@Test
	public void testInitializeStateReset() throws InterruptedException {
		final ConditionVariable cv = new ConditionVariable();

		Handler handler = new Handler(Looper.getMainLooper());
		handler.post(new Runnable() {
			@Override
			public void run() {
				webview = new WebView(InstrumentationRegistry.getInstrumentation().getTargetContext());
				cv.open();
			}
		});
		cv.block(30000);

		WebViewApp.setCurrentApp(new WebViewApp());
		WebViewApp.getCurrentApp().setWebView(webview);
		WebViewApp.getCurrentApp().setWebAppLoaded(true);
		SdkProperties.setInitialized(true);

		Configuration initConfig = new Configuration();
		InitializeThread.InitializeStateReset state = new InitializeThread.InitializeStateReset(initConfig);
		Object nextState = state.execute();

		assertFalse("Init state reset test: SDK is initialized after SDK was reset", SdkProperties.isInitialized());
		assertFalse("Init state reset test: webapp is loaded after SDK was reset", WebViewApp.getCurrentApp().isWebAppLoaded());
		assertTrue("Init state reset test: next state is not config", nextState instanceof InitializeThread.InitializeStateInitModules);

		Configuration config = ((InitializeThread.InitializeStateInitModules)nextState).getConfiguration();

		assertEquals("Init state reset test: next state config url is not set", config.getConfigUrl(), null);
	}

	@Test
	public void testInitializeStateAdBlockerCheck() {
		Configuration goodConfig = new Configuration("http://www.unity3d.com/test");
		InitializeThread.InitializeStateInitModules state = new InitializeThread.InitializeStateInitModules(goodConfig);
		Object nextState = state.execute();

		assertTrue("Init state ad blocker check test: next state is not load config", nextState instanceof InitializeThread.InitializeStateConfig);

		Configuration badConfig = new Configuration("http://localhost/test");
		InitializeThread.InitializeStateInitModules state2 = new InitializeThread.InitializeStateInitModules(badConfig);
		Object nextState2 = state2.execute();

		assertTrue("Init state ad blocker check test: next state is not error state", nextState2  instanceof InitializeThread.InitializeStateError);
	}

	@Test
	public void testInitializeStateConfig() {
		Configuration initConfig = new Configuration(SdkProperties.getConfigUrl());
		InitializeThread.InitializeStateConfig state = new InitializeThread.InitializeStateConfig(initConfig);
		Object nextState = state.execute();

		assertTrue("Init state config test: next state is not load cache", nextState instanceof InitializeThread.InitializeStateLoadCache);

		Configuration config = ((InitializeThread.InitializeStateLoadCache)nextState).getConfiguration();

		assertEquals("Init state config test: config webview url does not match url in testconfig.json", config.getWebViewUrl(), TEST_WEBVIEW_URL);
		assertEquals("Init state config test: config webview hash does not match hash in testconfig.json", config.getWebViewHash(), _testConfigHash);
	}

	@Test
	public void testInitializeStateConfigWithNwc() throws JSONException {
		ExperimentsReader mockExperimentReader = Mockito.mock(ExperimentsReader.class);
		Mockito.when(mockExperimentReader.getCurrentlyActiveExperiments()).thenReturn(new Experiments(new JSONObject("{\"nwc\": true}")));
		Configuration initConfig = new Configuration(SdkProperties.getConfigUrl(), mockExperimentReader);
		InitializeThread.InitializeStateConfig state = new InitializeThread.InitializeStateConfig(initConfig);
		Object nextState = state.execute();

		assertTrue("Init state config test: next state is not create with remote", nextState instanceof InitializeThread.InitializeStateCreateWithRemote);

		Configuration config = ((InitializeThread.InitializeStateCreateWithRemote)nextState).getConfiguration();

		assertEquals("Init state config test: config webview url does not match url in testconfig.json", config.getWebViewUrl(), TEST_WEBVIEW_URL);
		assertEquals("Init state config test: config webview hash does not match hash in testconfig.json", config.getWebViewHash(), _testConfigHash);
	}

	@Test
	public void testInitializeStateConfigWithWac() throws JSONException {
		Configuration initConfig = new Configuration(SdkProperties.getConfigUrl(), new Experiments(new JSONObject("{\"wac\": true}")));
		InitializeThread.InitializeStateConfig state = new InitializeThread.InitializeStateConfig(initConfig);
		Object nextState = state.execute();

		assertTrue("Init state config test: next state is not load cache", nextState instanceof InitializeThread.InitializeStateLoadCache);

		Configuration config = ((InitializeThread.InitializeStateLoadCache)nextState).getConfiguration();

		assertEquals("Init state config test: config webview url does not match url in testconfig.json", config.getWebViewUrl(), TEST_WEBVIEW_URL);
		assertEquals("Init state config test: config webview hash does not match hash in testconfig.json", config.getWebViewHash(), _testConfigHash);
	}

	@Test
	public void testInitializeStateConfigWithNullHash() throws MalformedURLException, URISyntaxException {
		String url = TestUtilities.getTestServerAddress() + "/testconfig_with_null_hash.json";

		Configuration localConfig = new Configuration(url);
		localConfig.setWebViewUrl(TEST_WEBVIEW_URL);
		SdkProperties.setConfigUrl(url);

		InitializeThread.InitializeStateConfig state = new InitializeThread.InitializeStateConfig(localConfig);

		Object nextState = state.execute();

		assertTrue("Init state config test: next state is not load cache", nextState instanceof InitializeThread.InitializeStateLoadCache);

		Configuration config = ((InitializeThread.InitializeStateLoadCache)nextState).getConfiguration();

		assertEquals("Init state config test: config webview url does not match url in test_with_null_hash.json", config.getWebViewUrl(), TEST_WEBVIEW_URL);
		assertEquals("Init state config test: config webview hash does not match hash in test_with_null_hash.json", null, config.getWebViewHash());
	}

	// Test for cache load fail case, success case is handled in load web test
	@Test
	public void testInitializeStateLoadCache() {
		Configuration bogusConfig;

		try {
			bogusConfig = new Configuration(getJSONObject(TEST_WEBVIEW_URL, _testConfigHash, null));
		} catch (Exception e) {
			Assert.fail();
			return;
		}

		InitializeThread.InitializeStateLoadCache state = new InitializeThread.InitializeStateLoadCache(bogusConfig);
		Object nextState = state.execute();

		assertTrue("Init state load cache test: init is not loading from web with bogus config ", nextState instanceof InitializeThread.InitializeStateLoadWeb);

		Configuration webConfig = ((InitializeThread.InitializeStateLoadWeb)nextState).getConfiguration();

		assertEquals("Init state load cache test: load web config url is not correct", webConfig.getWebViewUrl(), TEST_WEBVIEW_URL);
		assertEquals("Init state load cache test: load web config hash is not correct", webConfig.getWebViewHash(), _testConfigHash);
	}

	@Test
	public void testInitializeStateLoadWeb() {
		String webUrl = TestUtilities.getTestServerAddress() + "/testwebapp.html";
		String webData = "<p>Test web app</p>\n"; // Contents of testwebapp.html on test server
		String webHash = Utilities.Sha256(webData);

		Configuration webConfig;

		try {
			webConfig = new Configuration(getJSONObject(webUrl, webHash, null));
		} catch (Exception e) {
			Assert.fail();
			return;
		}

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
				"webviewbridge.handleInvocation(\"[['com.unity3d.services.core.api.Sdk','initComplete', [], 'CALLBACK_01']]\");" +
				"}</script>";
		String hash = Utilities.Sha256(data);

		Configuration config;
		try {
			 config = new Configuration(getJSONObject(url, hash, ""));
		} catch (Exception e) {
			Assert.fail();
			return;
		}



		InitializeThread.InitializeStateCreate state = new InitializeThread.InitializeStateCreate(config, data);
		Object nextState = state.execute();

		assertTrue("Init state create test: next state is not complete", nextState instanceof InitializeThread.InitializeStateComplete);
	}

	@Test
	public void testInitializeStateComplete() {
		Configuration config = new Configuration();

		InitializeThread.InitializeStateComplete state = new InitializeThread.InitializeStateComplete(config);
		Object nextState = state.execute();

		assertNull("Init state complete test: next step is not null", nextState);
	}

	@Test
	public void testInitializeStateFailed() {
		String url = "http://www.example.net/handlecallback.html"; // This url is never used

		// This is very hackish but also reproducing the full init procedure would be very fragile
		String data = "<script>var nativebridge = new Object(); nativebridge.handleCallback = new function() { " +
			"webviewbridge.handleInvocation(\"[['com.unity3d.services.core.api.Sdk','initError', ['message', 1], 'CALLBACK_01']]\");" +
			"}</script>";
		String hash = Utilities.Sha256(data);

		Configuration config;
		try {
			config = new Configuration(getJSONObject(url, hash, ""));
		} catch (Exception e) {
			Assert.fail();
			return;
		}

		InitializeThread.InitializeStateCreate state = new InitializeThread.InitializeStateCreate(config, data);
		Object nextState = state.execute();

		assertTrue("Init state create webview test: next state is not an instance of InitializeStateError", nextState instanceof InitializeThread.InitializeStateError);
	}

	@Test
	public void testInitializeStateRetry() {
		Configuration config = new Configuration();

		InitializeThread.InitializeStateRetry state = new InitializeThread.InitializeStateRetry(new InitializeThread.InitializeStateComplete(config), 5000);
		long startTime = SystemClock.elapsedRealtime();
		Object nextState = state.execute();
		long endTime = SystemClock.elapsedRealtime();

		assertTrue("Init state retry test: retry state did not return proper next state", nextState instanceof InitializeThread.InitializeStateComplete);
		assertFalse("Init state retry test: retry delay is less than four seconds (should be five seconds)", (endTime - startTime) < 4000);
		assertFalse("Init state retry test: retry delay is greater than six seconds (should be five seconds)", (endTime - startTime) > 6000);
	}

	@Test()
	public void testInitializeThreadRunThrowOOMError() throws Exception {
		Constructor<InitializeThread> constructor = InitializeThread.class.getDeclaredConstructor(Class.forName("com.unity3d.services.core.configuration.InitializeThread$InitializeState"));
		constructor.setAccessible(true);
		InitializeThread initializeThread = constructor.newInstance(new InitializeThread.InitializeStateCreate(null, null) {
			@Override
			public InitializeThread.InitializeStateCreate execute() {
				throw new OutOfMemoryError("This should get caught automatically");
			}
		});
		initializeThread.run();
	}

	@Test
	public void testInitializeStateCompleteTriggerMultipleOnInitializeComplete() {
		IUnityAdsInitializationListener initializationListener1 = mock(IUnityAdsInitializationListener.class);
		IUnityAdsInitializationListener initializationListener2 = mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener1);
		SdkProperties.addInitializationListener(initializationListener2);

		Configuration config = new Configuration();

		InitializeThread.InitializeStateComplete state = new InitializeThread.InitializeStateComplete(config);
		state.execute();
		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		Mockito.<IUnityAdsInitializationListener>verify(initializationListener1, times(1)).onInitializationComplete();
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener2, times(1)).onInitializationComplete();
	}

	@Test
	public void testInitializeStateErrorTriggerMultipleOnInitializationFailed() {
		IUnityAdsInitializationListener initializationListener1 = mock(IUnityAdsInitializationListener.class);
		IUnityAdsInitializationListener initializationListener2 = mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener1);
		SdkProperties.addInitializationListener(initializationListener2);

		Exception exception = new Exception();
		Configuration config = new Configuration();

		InitializeThread.InitializeStateError state = new InitializeThread.InitializeStateError(ErrorState.ResetWebApp, exception, config);
		Object nextState = state.execute();
		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		Mockito.<IUnityAdsInitializationListener>verify(initializationListener1, times(1)).onInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR,"Unity Ads failed to initialize due to internal error");
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener2, times(1)).onInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR,"Unity Ads failed to initialize due to internal error");
	}

	@Test
	public void testInitializeStateErrorWebAppFailedTriggerMultipleOnInitializationFailed() {
		IUnityAdsInitializationListener initializationListener1 = mock(IUnityAdsInitializationListener.class);
		IUnityAdsInitializationListener initializationListener2 = mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener1);
		SdkProperties.addInitializationListener(initializationListener2);

		Exception exception = new Exception("Web view failed to initialize");
		Configuration config = new Configuration();

		InitializeThread.InitializeStateError state = new InitializeThread.InitializeStateError(ErrorState.CreateWebApp, exception, config);
		Object nextState = state.execute();
		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		Mockito.<IUnityAdsInitializationListener>verify(initializationListener1, times(1)).onInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, "Web view failed to initialize");
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener2, times(1)).onInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, "Web view failed to initialize");
	}

	@Test
	public void testInitializeStateErrorTriggerMultipleOnInitializationFailedForAdBlocker() {
		IUnityAdsInitializationListener initializationListener1 = mock(IUnityAdsInitializationListener.class);
		IUnityAdsInitializationListener initializationListener2 = mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener1);
		SdkProperties.addInitializationListener(initializationListener2);

		Exception exception = new Exception("Unity Ads config server resolves to loopback address (due to ad blocker?)");
		Configuration config = new Configuration();

		InitializeThread.InitializeStateError state = new InitializeThread.InitializeStateError(ErrorState.InitModules, exception, config);
		Object nextState = state.execute();
		InstrumentationRegistry.getInstrumentation().waitForIdleSync();

		Mockito.<IUnityAdsInitializationListener>verify(initializationListener1, times(1)).onInitializationFailed(UnityAds.UnityAdsInitializationError.AD_BLOCKER_DETECTED, "Unity Ads config server resolves to loopback address (due to ad blocker?)");
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener2, times(1)).onInitializationFailed(UnityAds.UnityAdsInitializationError.AD_BLOCKER_DETECTED, "Unity Ads config server resolves to loopback address (due to ad blocker?)");
	}

	@Test()
	public void testInitializeThreadRunThrowOOMErrorAndTriggerOnInitializationFailed() throws Exception {
		IUnityAdsInitializationListener initializationListener = Mockito.mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener);

		Constructor<InitializeThread> constructor = InitializeThread.class.getDeclaredConstructor(Class.forName("com.unity3d.services.core.configuration.InitializeThread$InitializeState"));
		constructor.setAccessible(true);
		InitializeThread initializeThread = constructor.newInstance(new InitializeThread.InitializeStateCreate(null, null) {
			@Override
			public InitializeThread.InitializeStateCreate execute() {
				throw new OutOfMemoryError("This should get caught automatically");
			}
		});
		initializeThread.run();
		InstrumentationRegistry.getInstrumentation().waitForIdleSync();
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener, times(1)).onInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, "Unity Ads SDK failed to initialize due to application doesn't have enough memory to initialize Unity Ads SDK");
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener, times(0)).onInitializationComplete();
	}

	@Test()
	public void testInitializeThreadRunThrowExceptionAndTriggerOnInitializationFailed() throws Exception {
		IUnityAdsInitializationListener initializationListener = Mockito.mock(IUnityAdsInitializationListener.class);
		SdkProperties.addInitializationListener(initializationListener);

		Constructor<InitializeThread> constructor = InitializeThread.class.getDeclaredConstructor(Class.forName("com.unity3d.services.core.configuration.InitializeThread$InitializeState"));
		constructor.setAccessible(true);
		InitializeThread initializeThread = constructor.newInstance(new InitializeThread.InitializeStateComplete(null) {
			@Override
			public InitializeThread.InitializeStateComplete execute() {
				try {
					throw new Exception("This should get caught automatically");
				} catch (Exception e) {
					return new InitializeThread.InitializeStateComplete(null);
				}
			}
		});
		initializeThread.run();
		InstrumentationRegistry.getInstrumentation().waitForIdleSync();
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener, times(1)).onInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, "Unity Ads SDK encountered an error during initialization, cancel initialization");
		Mockito.<IUnityAdsInitializationListener>verify(initializationListener, times(0)).onInitializationComplete();
	}

	@Test
	public void testInitializeStateLoadCacheConfigAndWebViewNoCachedConfig() {
		InitializeThread.InitializeStateLoadCacheConfigAndWebView state = new InitializeThread.InitializeStateLoadCacheConfigAndWebView(new Configuration(), null);
		File configFile = new File(SdkProperties.getLocalConfigurationFilepath());
		File webViewFile = new File(SdkProperties.getLocalWebViewFile());

		try {
			webViewFile.createNewFile();
		} catch (IOException e) {
			Assert.fail("Unable to create test files");
		}

		assertFalse("Temp file already exists when it should be clean", configFile.exists());
		assertTrue("Temp file does not exist as expected", webViewFile.exists());

		Object nextState = state.execute();
		assertTrue("InitializeStateLoadCacheConfigAndWebView: next state is not InitializeStateCheckForUpdatedWebView", nextState instanceof InitializeThread.InitializeStateCheckForUpdatedWebView);
	}

	@Test
	public void testInitializeStateLoadCacheConfigAndWebViewNoCachedWebViewData() {
		InitializeThread.InitializeStateLoadCacheConfigAndWebView state = new InitializeThread.InitializeStateLoadCacheConfigAndWebView(new Configuration(), null);
		File configFile = new File(SdkProperties.getLocalConfigurationFilepath());
		File webViewFile = new File(SdkProperties.getLocalWebViewFile());

		try {
			configFile.createNewFile();
		} catch (IOException e) {
			Assert.fail("Unable to create test files");
		}

		assertTrue("Temp file does not exist as expected", configFile.exists());
		assertFalse("Temp file already exists when it should be clean", webViewFile.exists());

		Object nextState = state.execute();
		assertTrue("InitializeStateLoadCacheConfigAndWebView: next state is not InitializeStateCleanCache", nextState instanceof InitializeThread.InitializeStateCleanCache);
	}

	@Test
	public void testInitializeStateLoadCacheConfigAndWebView() {
		InitializeThread.InitializeStateLoadCacheConfigAndWebView state = new InitializeThread.InitializeStateLoadCacheConfigAndWebView(new Configuration(), null);
		File webViewFile = new File(SdkProperties.getLocalWebViewFile());
		File configFile = new File(SdkProperties.getLocalConfigurationFilepath());
		String configJson = "{hash:\"12345\",version:\"1.0.0\",url:\"http://www.google.com\"}";

		try {
			configFile.createNewFile();
			webViewFile.createNewFile();

			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(configFile, false));
			outputStreamWriter.write(configJson);
			outputStreamWriter.close();
		} catch (IOException e) {
			Assert.fail("Unable to create test files");
		}

		assertTrue("Temp file does not exist as expected", configFile.exists());
		assertTrue("Temp file does not exist as expected", webViewFile.exists());

		Object nextState = state.execute();
		assertTrue("InitializeStateLoadCacheConfigAndWebView: next state is not InitializeStateCheckForUpdatedWebView", nextState instanceof InitializeThread.InitializeStateCheckForUpdatedWebView);
	}

	@Test
	public void testInitializeStateCleanCacheNothingInCache() {
		InitializeThread.InitializeStateComplete targetState = new InitializeThread.InitializeStateComplete(new Configuration());
		InitializeThread.InitializeStateCleanCache state = new InitializeThread.InitializeStateCleanCache(new Configuration(), targetState);
		File webViewFile = new File(SdkProperties.getLocalWebViewFile());
		File configFile = new File(SdkProperties.getLocalConfigurationFilepath());
		assertFalse("Temp file already exists when it should be clean", configFile.exists());
		assertFalse("Temp file already exists when it should be clean", webViewFile.exists());

		Object nextState = state.execute();
		assertTrue("InitializeStateCleanCache: next state is not InitializeStateComplete", nextState instanceof InitializeThread.InitializeStateComplete);
		assertFalse("Temp file exists when it should be clean", configFile.exists());
		assertFalse("Temp file exists when it should be clean", webViewFile.exists());
	}

	@Test
	public void testInitializeStateCleanCache() {
		InitializeThread.InitializeStateComplete targetState = new InitializeThread.InitializeStateComplete(new Configuration());
		InitializeThread.InitializeStateCleanCache state = new InitializeThread.InitializeStateCleanCache(new Configuration(), targetState);
		File webViewFile = new File(SdkProperties.getLocalWebViewFile());
		File configFile = new File(SdkProperties.getLocalConfigurationFilepath());

		try {
			configFile.createNewFile();
			webViewFile.createNewFile();
		} catch (IOException e) {
			Assert.fail("Unable to create test files");
		}

		assertTrue("Temp file does not exist as expected", configFile.exists());
		assertTrue("Temp file does not exist as expected", webViewFile.exists());

		Object nextState = state.execute();
		assertTrue("InitializeStateCleanCache: next state is not InitializeStateComplete", nextState instanceof InitializeThread.InitializeStateComplete);
		assertFalse("Temp file exists when it should be clean", configFile.exists());
		assertFalse("Temp file exists when it should be clean", webViewFile.exists());
	}

	@Test
	public void testInitializeStateCheckForUpdatedWebViewUpdatedConfig() throws MalformedURLException, JSONException {
		byte[] data = {1,2,3,4,5};
		String webViewHash = Utilities.Sha256(data);
		String webViewHashNew = Utilities.Sha256("123");
		String sdkVersion = "99.99.99";

		Configuration cacheConfig = new Configuration(getJSONObject(TEST_WEBVIEW_URL, webViewHash, SdkProperties.getVersionName()).put("sdkv", SdkProperties.getVersionName()));
		Configuration updatedConfig = new Configuration(getJSONObject(TEST_WEBVIEW_URL, webViewHashNew, sdkVersion).put("sdkv", sdkVersion));

		InitializeThread.InitializeStateCheckForUpdatedWebView state = new InitializeThread.InitializeStateCheckForUpdatedWebView(updatedConfig, data, cacheConfig);
		Object nextState = state.execute();
		assertTrue("InitializeStateCheckForUpdatedWebView: next state is not InitializeStateCreate", nextState instanceof InitializeThread.InitializeStateCreate);
		assertTrue("SdkProperties should have the updated configuration", SdkProperties.getLatestConfiguration() != null);
		assertEquals("SdkProperties should have the updated configuration", SdkProperties.getLatestConfiguration().getWebViewHash(), webViewHashNew);
		assertEquals("SdkProperties should have the updated configuration", SdkProperties.getLatestConfiguration().getSdkVersion(), sdkVersion);
	}

	@Test
	public void testInitializeStateCheckForUpdatedWebViewNoUpdate() {
		byte[] data = {1,2,3,4,5};
		String webViewHash = Utilities.Sha256(data);

		Configuration cacheConfig = new Configuration();
		cacheConfig.setWebViewHash(webViewHash);
		Configuration updatedConfig = new Configuration();
		updatedConfig.setWebViewHash(webViewHash);

		InitializeThread.InitializeStateCheckForUpdatedWebView state = new InitializeThread.InitializeStateCheckForUpdatedWebView(updatedConfig, data, cacheConfig);
		Object nextState = state.execute();
		assertTrue("InitializeStateCheckForUpdatedWebView: next state is not InitializeStateCreate", nextState instanceof InitializeThread.InitializeStateCreate);
		assertTrue("SdkProperties should not have the updated configuration", SdkProperties.getLatestConfiguration() == null);
	}

	@Test
	public void testInitializeStateCheckForUpdatedWebViewNullData() {
		byte[] data = {1,2,3,4,5};
		String webViewHash = Utilities.Sha256(data);

		Configuration cacheConfig = new Configuration();
		cacheConfig.setWebViewHash("123");
		Configuration updatedConfig = new Configuration();
		updatedConfig.setWebViewHash(webViewHash);

		InitializeThread.InitializeStateCheckForUpdatedWebView state = new InitializeThread.InitializeStateCheckForUpdatedWebView(updatedConfig, null, cacheConfig);
		Object nextState = state.execute();
		assertTrue("InitializeStateCheckForUpdatedWebView: next state is not InitializeStateCleanCache", nextState instanceof InitializeThread.InitializeStateCleanCache);
		assertNull("SdkProperties should not have the updated configuration due to exception with processing null data", SdkProperties.getLatestConfiguration());
	}

	@Test
	public void testInitializeStateCheckForUpdatedWebViewNullConfig() {
		byte[] data = {1,2,3,4,5};
		String stringData = new String(data);
		String webViewHash = Utilities.Sha256(data);

		Configuration cacheConfig = new Configuration();
		cacheConfig.setWebViewHash(webViewHash);
		cacheConfig.setWebViewData(stringData);
		Configuration updatedConfig = new Configuration();
		updatedConfig.setWebViewHash(webViewHash);
		updatedConfig.setWebViewData(stringData);

		InitializeThread.InitializeStateCheckForUpdatedWebView state = new InitializeThread.InitializeStateCheckForUpdatedWebView(updatedConfig, data, null);
		Object nextState = state.execute();
		assertTrue("InitializeStateCheckForUpdatedWebView: next state is not InitializeStateCreate", nextState instanceof InitializeThread.InitializeStateCreate);
		assertNull("SdkProperties should not have the updated configuration due to matching hashes", SdkProperties.getLatestConfiguration());

		Configuration createStateConfig = ((InitializeThread.InitializeStateCreate)nextState).getConfiguration();
		assertEquals("WebView Hash does not match as expected", webViewHash, createStateConfig.getWebViewHash());
		assertEquals("WebView Data does not match as expected", stringData, createStateConfig.getWebViewData());
	}

	@Test
	public void testInitializeStateDownloadWebViewUpdate() {
		String webUrl = TestUtilities.getTestServerAddress() + "/testwebapp.html";
		String webData = "<p>Test web app</p>\n"; // Contents of testwebapp.html on test server
		String webHash = Utilities.Sha256(webData);
		Configuration webConfig = new Configuration();
		webConfig.setWebViewUrl(webUrl);
		webConfig.setWebViewHash(webHash);

		InitializeThread.InitializeStateDownloadWebView state = new InitializeThread.InitializeStateDownloadWebView(webConfig);
		Object nextState = state.execute();

		assertTrue("Init state load web test: next state is not create", nextState instanceof InitializeThread.InitializeStateUpdateCache);

		Configuration createConfig = ((InitializeThread.InitializeStateUpdateCache)nextState).getConfiguration();

		assertEquals("Init state load web test: original webview url does not match created url", webUrl, createConfig.getWebViewUrl());
		assertEquals("Init state load web test: original webview hash does not match created hash", webHash, createConfig.getWebViewHash());
	}

	@Test
	public void testInitializeStateUpdateCache() {
		String webData = "<p>Test web app</p>\n"; // Contents of testwebapp.html on test server
		Configuration cacheConfig = new Configuration();

		InitializeThread.InitializeStateUpdateCache state = new InitializeThread.InitializeStateUpdateCache(cacheConfig, webData);
		Object nextState = state.execute();

		assertNull("InitializeStateUpdateCache: state was not null after execution", nextState);

		File webViewFile = new File(SdkProperties.getLocalWebViewFile());
		File configFile = new File(SdkProperties.getLocalConfigurationFilepath());
		assertTrue("Cached config file does not exist as expected", configFile.exists());
		assertTrue("Cached webView file does not exist as expected", webViewFile.exists());
	}

	@Test
	public void testDownloadLatestWebView() {
		SdkProperties.setLatestConfiguration(new Configuration());
		DownloadLatestWebViewStatus status = InitializeThread.downloadLatestWebView();
		Assert.assertThat("Status should be DownloadLatestWebViewStatus.BACKGROUND_DOWNLOAD_STARTED", status, Is.is(DownloadLatestWebViewStatus.BACKGROUND_DOWNLOAD_STARTED));
	}

	@Test
	public void testDownloadLatestWebViewMissingConfiguration() {
		DownloadLatestWebViewStatus status = InitializeThread.downloadLatestWebView();
		Assert.assertThat("Status should be DownloadLatestWebViewStatus.MISSING_LATEST_CONFIG", status, Is.is(DownloadLatestWebViewStatus.MISSING_LATEST_CONFIG));
	}
}