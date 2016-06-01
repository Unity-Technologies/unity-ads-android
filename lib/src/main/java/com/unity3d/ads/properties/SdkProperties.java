package com.unity3d.ads.properties;

import android.content.Context;
import android.os.Build;

import com.unity3d.ads.BuildConfig;
import com.unity3d.ads.log.DeviceLog;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class SdkProperties {
	private static String _configUrl = getDefaultConfigUrl("release");
	private static File _cacheDirectory = null;
	private static final String CACHE_DIR_NAME = "UnityAdsCache";
	private static final String LOCAL_CACHE_FILE_PREFIX = "UnityAdsCache-";
	private static final String LOCAL_STORAGE_FILE_PREFIX = "UnityAdsStorage-";
	private static int _showTimeout = 5000;

	private static boolean _initialized = false;
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
		return "https://cdn.unityads.unity3d.com/webview/" + getWebViewBranch() + "/" + flavor + "/config.json";
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
		if (_cacheDirectory == null) {
			File filesDir = context.getFilesDir();
			// If device storage is full, filesDir might be null
			if (filesDir != null) {
				_cacheDirectory = new File(filesDir.getPath());
			}

			if (Build.VERSION.SDK_INT > 18) {
				File externalCacheFile = context.getExternalCacheDir();
				// If device storage is full, external cachedir might be null
				if (externalCacheFile != null) {
					String absoluteCachePath = externalCacheFile.getAbsolutePath();
					_cacheDirectory = new File(absoluteCachePath, CACHE_DIR_NAME);
					if (_cacheDirectory.mkdirs()) {
						DeviceLog.debug("Successfully created cache");
					}
				}
			}

			if (!_cacheDirectory.isDirectory()) {
				DeviceLog.error("Unity Ads cache: Creating cache dir failed");
				return null;
			}

			DeviceLog.debug("Unity Ads cache: using " + _cacheDirectory.getAbsolutePath() + " as cache");
		}

		return _cacheDirectory;
	}

	public static void setShowTimeout(int timeout) {
		_showTimeout = timeout;
	}

	public static int getShowTimeout() {
		return _showTimeout;
	}
}
