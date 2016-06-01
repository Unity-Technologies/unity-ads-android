package com.unity3d.ads.api;

import com.unity3d.ads.connectivity.ConnectivityMonitor;
import com.unity3d.ads.device.Device;
import com.unity3d.ads.device.DeviceError;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.webview.bridge.WebViewCallback;
import com.unity3d.ads.webview.bridge.WebViewExposed;

import org.json.JSONArray;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DeviceInfo {
	public enum StorageType { EXTERNAL, INTERNAL }

	@WebViewExposed
	public static void getAndroidId (WebViewCallback callback) {
		callback.invoke(Device.getAndroidId());
	}

	@WebViewExposed
	public static void getAdvertisingTrackingId(WebViewCallback callback) {
		callback.invoke(Device.getAdvertisingTrackingId());
	}

	@WebViewExposed
	public static void getLimitAdTrackingFlag(WebViewCallback callback) {
		callback.invoke(Device.isLimitAdTrackingEnabled());
	}

	@WebViewExposed
	public static void getApiLevel (WebViewCallback callback) {
		callback.invoke(Device.getApiLevel());
	}

	@WebViewExposed
	public static void getOsVersion (WebViewCallback callback) {
		callback.invoke(Device.getOsVersion());
	}

	@WebViewExposed
	public static void getManufacturer (WebViewCallback callback) {
		callback.invoke(Device.getManufacturer());
	}

	@WebViewExposed
	public static void getModel (WebViewCallback callback) {
		callback.invoke(Device.getModel());
	}

	@WebViewExposed
	public static void getScreenLayout (WebViewCallback callback) {
		callback.invoke(Device.getScreenLayout());
	}

	@WebViewExposed
	public static void getScreenDensity (WebViewCallback callback) {
		callback.invoke(Device.getScreenDensity());
	}

	@WebViewExposed
	public static void getScreenWidth (WebViewCallback callback) {
		callback.invoke(Device.getScreenWidth());
	}

	@WebViewExposed
	public static void getScreenHeight (WebViewCallback callback) {
		callback.invoke(Device.getScreenHeight());
	}

	@WebViewExposed
	public static void getTimeZone(Boolean dst, WebViewCallback callback) {
		callback.invoke(TimeZone.getDefault().getDisplayName(dst, TimeZone.SHORT, Locale.US));
	}

	@WebViewExposed
	public static void getConnectionType(WebViewCallback callback) {
		String connectionType;
		if(Device.isUsingWifi()) {
			connectionType = "wifi";
		} else if(Device.isActiveNetworkConnected()) {
			connectionType = "cellular";
		} else {
			connectionType = "none";
		}
		callback.invoke(connectionType);
	}

	@WebViewExposed
	public static void getNetworkType(WebViewCallback callback) {
		callback.invoke(Device.getNetworkType());
	}

	@WebViewExposed
	public static void getNetworkOperator(WebViewCallback callback) {
		callback.invoke(Device.getNetworkOperator());
	}

	@WebViewExposed
	public static void getNetworkOperatorName(WebViewCallback callback) {
		callback.invoke(Device.getNetworkOperatorName());
	}

	@WebViewExposed
	public static void isAppInstalled(String pkgname, WebViewCallback callback) {
		callback.invoke(Device.isAppInstalled(pkgname));
	}

	@WebViewExposed
	public static void isRooted(WebViewCallback callback) {
		callback.invoke(Device.isRooted());
	}

	@WebViewExposed
	public static void getInstalledPackages(boolean md5, WebViewCallback callback) {
		List<Map<String, Object>> installedPackages = Device.getInstalledPackages(md5);
		JSONArray packageJson = new JSONArray(installedPackages);
		callback.invoke(packageJson);
	}

	@WebViewExposed
	public static void getUniqueEventId(WebViewCallback callback) {
		callback.invoke(Device.getUniqueEventId());
	}

	@WebViewExposed
	public static void getHeadset(WebViewCallback callback) {
		callback.invoke(Device.isWiredHeadsetOn());
	}

	@WebViewExposed
	public static void getSystemProperty(String propertyName, String defaultValue, WebViewCallback callback) {
		callback.invoke(Device.getSystemProperty(propertyName, defaultValue));
	}

	@WebViewExposed
	public static void getRingerMode (WebViewCallback callback) {
		int ringerMode = Device.getRingerMode();
		if (ringerMode > -1)
			callback.invoke(ringerMode);
		else {
			switch (ringerMode) {
				case -1:
					callback.error(DeviceError.APPLICATION_CONTEXT_NULL, ringerMode);
					break;
				case -2:
					callback.error(DeviceError.AUDIOMANAGER_NULL, ringerMode);
					break;
				default:
					DeviceLog.error("Unhandled ringerMode error: " + ringerMode);
					break;

			}
		}
	}

	@WebViewExposed
	public static void getSystemLanguage(WebViewCallback callback) {
		callback.invoke(Locale.getDefault().toString());
	}

	@WebViewExposed
	public static void getDeviceVolume(Integer streamType, WebViewCallback callback) {
		callback.invoke(Device.getStreamVolume(streamType));
	}

	@WebViewExposed
	public static void getScreenBrightness (WebViewCallback callback) {
		int screenBrightness = Device.getScreenBrightness();
		if (screenBrightness > -1)
			callback.invoke(screenBrightness);
		else {
			switch (screenBrightness) {
				case -1:
					callback.error(DeviceError.APPLICATION_CONTEXT_NULL, screenBrightness);
					break;
				default:
					DeviceLog.error("Unhandled screenBrightness error: " + screenBrightness);
					break;
			}
		}
	}

	private static StorageType getStorageTypeFromString (String storageType) {
		StorageType storage;

		try {
			storage = StorageType.valueOf(storageType);
		}
		catch (IllegalArgumentException e) {
			DeviceLog.exception("Illegal argument: " + storageType, e);
			return null;
		}

		return storage;
	}

	private static File getFileForStorageType (StorageType storageType) {
		switch (storageType) {
			case INTERNAL:
				return ClientProperties.getApplicationContext().getCacheDir();
			case EXTERNAL:
				return ClientProperties.getApplicationContext().getExternalCacheDir();
			default:
				DeviceLog.error("Unhandled storagetype: " + storageType);
				return null;
		}
	}

	@WebViewExposed
	public static void getFreeSpace (String storageType, WebViewCallback callback) {
		StorageType storage = getStorageTypeFromString(storageType);

		if (storage == null) {
			callback.error(DeviceError.INVALID_STORAGETYPE, storageType);
			return;
		}

		long space = Device.getFreeSpace(getFileForStorageType(storage));
		if (space > -1)
			callback.invoke(space);
		else
			callback.error(DeviceError.COULDNT_GET_STORAGE_LOCATION, space);
	}

	@WebViewExposed
	public static void getTotalSpace (String storageType, WebViewCallback callback) {
		StorageType storage = getStorageTypeFromString(storageType);

		if (storage == null) {
			callback.error(DeviceError.INVALID_STORAGETYPE, storageType);
			return;
		}

		long space = Device.getTotalSpace(getFileForStorageType(storage));
		if (space > -1)
			callback.invoke(space);
		else
			callback.error(DeviceError.COULDNT_GET_STORAGE_LOCATION, space);
	}

	@WebViewExposed
	public static void getBatteryLevel(WebViewCallback callback) {
		callback.invoke(Device.getBatteryLevel());
	}

	@WebViewExposed
	public static void getBatteryStatus(WebViewCallback callback) {
		callback.invoke(Device.getBatteryStatus());
	}

	@WebViewExposed
	public static void getFreeMemory(WebViewCallback callback) {
		callback.invoke(Device.getFreeMemory());
	}

	@WebViewExposed
	public static void getTotalMemory(WebViewCallback callback) {
		callback.invoke(Device.getTotalMemory());
	}

}