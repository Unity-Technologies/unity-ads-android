package com.unity3d.ads.properties;

import android.content.Context;
import android.os.Build;

import com.unity3d.ads.BuildConfig;
import com.unity3d.ads.cache.CacheDirectory;
import com.unity3d.ads.log.DeviceLog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class SdkProperties {
	private static String _configUrl = getDefaultConfigUrl("release");
	private static CacheDirectory _cacheDirectory = null;
	private static final String CACHE_DIR_NAME = "UnityAdsCache";
	private static final String LOCAL_CACHE_FILE_PREFIX = "UnityAdsCache-";
	private static final String LOCAL_STORAGE_FILE_PREFIX = "UnityAdsStorage-";
	private static int _showTimeout = 5000;
	private static long _initializationTime = 0;

	private static boolean _initialized = false;
	private static boolean _reinitialized = false;
	private static boolean _testMode = false;

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
		return _configUrl;
	}

	public static String getDefaultConfigUrl(String flavor) {
		return "https://config.unityads.unity3d.com/webview/" + getWebViewBranch() + "/" + flavor + "/config.json";
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

	public static void setShowTimeout(int timeout) {
		_showTimeout = timeout;
	}

	public static int getShowTimeout() {
		return _showTimeout;
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
}
