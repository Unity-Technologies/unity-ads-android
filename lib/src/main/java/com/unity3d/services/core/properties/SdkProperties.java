package com.unity3d.services.core.properties;

import android.content.Context;
import com.unity3d.ads.BuildConfig;
import com.unity3d.services.IUnityServicesListener;
import com.unity3d.services.core.cache.CacheDirectory;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.log.DeviceLog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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

	private static boolean _initialized = false;
	private static boolean _reinitialized = false;
	private static boolean _testMode = false;
	private static boolean _debugMode = false;

	public static boolean isInitialized () {
		return _initialized;
	}

	public static void setInitialized (boolean initialized) {
		_initialized = initialized;
	}

	public static boolean isTestMode () {
		return _testMode;
	}

	public static void setTestMode (boolean testMode) {
		_testMode = testMode;
	}

	public static int getVersionCode () {
		return BuildConfig.VERSION_CODE;
	}

	public static String getVersionName () { return BuildConfig.VERSION_NAME; }

	public static String getCacheDirectoryName () {
		return CACHE_DIR_NAME;
	}

	public static String getCacheFilePrefix() {
		return LOCAL_CACHE_FILE_PREFIX;
	}

	public static String getLocalStorageFilePrefix() {
		return LOCAL_STORAGE_FILE_PREFIX;
	}

	public static void setConfigUrl (String url) throws URISyntaxException, MalformedURLException {
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
		if(BuildConfig.DEBUG) {
			return BuildConfig.WEBVIEW_BRANCH;
		}
		return getVersionName();
	}

	public static String getLocalWebViewFile () {
		return SdkProperties.getCacheDirectory().getAbsolutePath() + "/" + "UnityAdsWebApp.html";
	}

	public static File getCacheDirectory () {
		return getCacheDirectory(ClientProperties.getApplicationContext());
	}

	public static File getCacheDirectory (Context context) {
		if(_cacheDirectory == null) {
			setCacheDirectory(new CacheDirectory(CACHE_DIR_NAME));
		}

		return _cacheDirectory.getCacheDirectory(context);
	}

	public static void setCacheDirectory (CacheDirectory cacheDirectory) {
		_cacheDirectory = cacheDirectory;
	}

	public static CacheDirectory getCacheDirectoryObject () {
		return _cacheDirectory;
	}

	public static void setInitializationTime (long milliseconds) {
		_initializationTime = milliseconds;
	}

	public static long getInitializationTime () {
		return _initializationTime;
	}

	public static void setReinitialized (boolean status) {
		_reinitialized = status;
	}

	public static boolean isReinitialized () {
		return _reinitialized;
	}

	public static void setDebugMode(boolean debugMode) {
		_debugMode = debugMode;

		if(debugMode) {
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

	public static boolean isChinaLocale(String networkISOCode) {
		if (networkISOCode.equalsIgnoreCase(CHINA_ISO_ALPHA_2_CODE) || networkISOCode.equalsIgnoreCase(CHINA_ISO_ALPHA_3_CODE)) {
			return true;
		} else {
			return false;
		}
  }
}
