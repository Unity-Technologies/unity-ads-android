package com.unity3d.services.core.properties;

import android.content.Context;

import com.unity3d.ads.BuildConfig;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.IUnityServicesListener;
import com.unity3d.services.core.cache.CacheDirectory;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.log.DeviceLog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicReference;

public class SdkProperties {
	private static String _configUrl = null;
	private static CacheDirectory _cacheDirectory = null;
	private static final String CACHE_DIR_NAME = "UnityAdsCache";
	private static final String LOCAL_CACHE_FILE_PREFIX = "UnityAdsCache-";
	private static final String LOCAL_STORAGE_FILE_PREFIX = "UnityAdsStorage-";
	private static final String CHINA_ISO_ALPHA_2_CODE = "CN";
	private static final String CHINA_ISO_ALPHA_3_CODE = "CHN";
	private static long _initializationTime = 0;
	private static IUnityServicesListener _listener;
	private static Configuration _latestConfiguration;

	private static LinkedHashSet<IUnityAdsInitializationListener> _initializationListeners = new LinkedHashSet<IUnityAdsInitializationListener>();

	private static boolean _initialized = false;
	private static boolean _reinitialized = false;
	private static boolean _testMode = false;
	private static boolean _perPlacementLoadEnabled = false;
	private static boolean _debugMode = false;
	private static AtomicReference<InitializationState> _currentInitializationState = new AtomicReference<>(InitializationState.NOT_INITIALIZED);

	public enum InitializationState {
		NOT_INITIALIZED,
		INITIALIZING,
		INITIALIZED_SUCCESSFULLY,
		INITIALIZED_FAILED
	}

	public static void notifyInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
		setInitializeState(InitializationState.INITIALIZED_FAILED);

		for(IUnityAdsInitializationListener initializationListener: getInitializationListeners()) {
			initializationListener.onInitializationFailed(error, message);
		}
		resetInitializationListeners();
	}

	public static void notifyInitializationComplete() {
		setInitializeState(InitializationState.INITIALIZED_SUCCESSFULLY);

		for(IUnityAdsInitializationListener initializationListener: getInitializationListeners()) {
			initializationListener.onInitializationComplete();
		}
		resetInitializationListeners();
	}

	public static void setInitializeState(InitializationState initializeState) {
		_currentInitializationState.set(initializeState);
	}

	public static InitializationState getCurrentInitializationState() {
		return _currentInitializationState.get();
	}

	public static boolean isInitialized() {
		return _initialized;
	}

	public static void setInitialized(boolean initialized) {
		_initialized = initialized;
	}

	public static boolean isTestMode() {
		return _testMode;
	}

	public static void setTestMode(boolean testMode) {
		_testMode = testMode;
	}

	public static boolean isPerPlacementLoadEnabled () { return _perPlacementLoadEnabled; }

	public static void setPerPlacementLoadEnabled (boolean perPlacementLoad) {
		_perPlacementLoadEnabled = perPlacementLoad;
	}

	public static int getVersionCode () {
		return BuildConfig.VERSION_CODE;
	}

	public static String getVersionName() {
		return BuildConfig.VERSION_NAME;
	}

	public static String getCacheDirectoryName() {
		return CACHE_DIR_NAME;
	}

	public static String getCacheFilePrefix() {
		return LOCAL_CACHE_FILE_PREFIX;
	}

	public static String getLocalStorageFilePrefix() {
		return LOCAL_STORAGE_FILE_PREFIX;
	}

	public static void setConfigUrl(String url) throws URISyntaxException, MalformedURLException {
		if (url == null) {
			throw new MalformedURLException();
		}
		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			throw new MalformedURLException();
		}

		URL u = new URL(url);
		u.toURI();

		_configUrl = url;
	}

	public static String getConfigUrl() {
		if (_configUrl == null) {
			_configUrl = getDefaultConfigUrl("release");
		}
		return _configUrl;
	}

	public static String getDefaultConfigUrl(String flavor) {
		boolean isChinaLocale = isChinaLocale(Device.getNetworkCountryISO());
		String baseURI = "https://config.unityads.unity3d.com/webview/";
		if (isChinaLocale) {
			baseURI = "https://config.unityads.unitychina.cn/webview/";
		}
		return baseURI + getWebViewBranch() + "/" + flavor + "/config.json";
	}

	private static String getWebViewBranch() {
		if (BuildConfig.DEBUG) {
			return BuildConfig.WEBVIEW_BRANCH;
		}
		return getVersionName();
	}

	public static String getLocalWebViewFile() {
		return SdkProperties.getCacheDirectory().getAbsolutePath() + "/" + "UnityAdsWebApp.html";
	}

	public static String getLocalConfigurationFilepath() {
		return SdkProperties.getCacheDirectory().getAbsolutePath() + "/" + "UnityAdsWebViewConfiguration.json";
	}

	public static void setLatestConfiguration(Configuration configuration) {
		_latestConfiguration = configuration;
	}

	public static Configuration getLatestConfiguration() {
		return _latestConfiguration;
	}

	public static String getLocalWebViewFileUpdated() {
		return getLocalWebViewFile() + ".new";
	}

	public static File getCacheDirectory() {
		return getCacheDirectory(ClientProperties.getApplicationContext());
	}

	public static File getCacheDirectory(Context context) {
		if (_cacheDirectory == null) {
			setCacheDirectory(new CacheDirectory(CACHE_DIR_NAME));
		}

		return _cacheDirectory.getCacheDirectory(context);
	}

	public static void setCacheDirectory(CacheDirectory cacheDirectory) {
		_cacheDirectory = cacheDirectory;
	}

	public static CacheDirectory getCacheDirectoryObject() {
		return _cacheDirectory;
	}

	public static void setInitializationTime(long milliseconds) {
		_initializationTime = milliseconds;
	}

	public static long getInitializationTime() {
		return _initializationTime;
	}

	public static void setReinitialized(boolean status) {
		_reinitialized = status;
	}

	public static boolean isReinitialized() {
		return _reinitialized;
	}

	public static void setDebugMode(boolean debugMode) {
		_debugMode = debugMode;

		if (debugMode) {
			DeviceLog.setLogLevel(DeviceLog.LOGLEVEL_DEBUG);
		} else {
			DeviceLog.setLogLevel(DeviceLog.LOGLEVEL_INFO);
		}
	}

	public static boolean getDebugMode() {
		return _debugMode;
	}

	public static void setListener(IUnityServicesListener listener) {
		_listener = listener;
	}

	public static IUnityServicesListener getListener() {
		return _listener;
	}

	public static void addInitializationListener(IUnityAdsInitializationListener listener) {
		if(listener == null) {
			return;
		}

		synchronized (_initializationListeners) {
			_initializationListeners.add(listener);
		}
	}

	public static IUnityAdsInitializationListener[] getInitializationListeners() {
		synchronized (_initializationListeners) {
			IUnityAdsInitializationListener[] listeners = new IUnityAdsInitializationListener[_initializationListeners.size()];
			_initializationListeners.toArray(listeners);
			return listeners;
		}
	}

	public static void resetInitializationListeners () {
		synchronized (_initializationListeners) {
			_initializationListeners.clear();
		}
	}

	public static boolean isChinaLocale(String networkISOCode) {
		if (networkISOCode.equalsIgnoreCase(CHINA_ISO_ALPHA_2_CODE)
			|| networkISOCode.equalsIgnoreCase(CHINA_ISO_ALPHA_3_CODE)) {
			return true;
		} else {
			return false;
		}
	}
}
