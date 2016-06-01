package com.unity3d.ads.device;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.ClientProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Device {

	public enum MemoryInfoType { TOTAL_MEMORY, FREE_MEMORY }

	public static int getApiLevel() {
		return Build.VERSION.SDK_INT;
	}

	public static String getOsVersion() {
		return Build.VERSION.RELEASE;
	}

	public static String getManufacturer () {
		return Build.MANUFACTURER;
	}

	public static String getModel() {
		return Build.MODEL;
	}

	public static int getScreenLayout () {
		if (ClientProperties.getApplicationContext() != null) {
			return ClientProperties.getApplicationContext().getResources().getConfiguration().screenLayout;
		}

		return -1;
	}

	@SuppressLint("DefaultLocale")
	public static String getAndroidId () {
		String androidID = null;

		try {
			androidID = Settings.Secure.getString(ClientProperties.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
		}
		catch (Exception e) {
			DeviceLog.exception("Problems fetching androidId", e);
		}

		return androidID;
	}

	public static String getAdvertisingTrackingId() {
		return AdvertisingId.getAdvertisingTrackingId();
	}

	public static boolean isLimitAdTrackingEnabled() {
		return AdvertisingId.getLimitedAdTracking();
	}

	public static boolean isUsingWifi () {
		ConnectivityManager mConnectivity;

		if (ClientProperties.getApplicationContext() != null) {
			mConnectivity = (ConnectivityManager)ClientProperties.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
			if (mConnectivity == null) {
				return false;
			}

			TelephonyManager mTelephony;
			mTelephony = (TelephonyManager)ClientProperties.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

			// Skip if no connection, or background data disabled
			NetworkInfo info = mConnectivity.getActiveNetworkInfo();
			if (info == null || !mConnectivity.getBackgroundDataSetting() || !mConnectivity.getActiveNetworkInfo().isConnected() || mTelephony == null) {
				return false;
			}

			int netType = info.getType();
			return netType == ConnectivityManager.TYPE_WIFI && info.isConnected();
		}

		return false;
	}

	public static int getNetworkType() {
		if (ClientProperties.getApplicationContext() != null) {
			TelephonyManager tm = (TelephonyManager)ClientProperties.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getNetworkType();
		}

		return -1;
	}

	public static String getNetworkOperator() {
		if(ClientProperties.getApplicationContext() != null) {
			TelephonyManager tm = (TelephonyManager)ClientProperties.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getNetworkOperator();
		}
		return "";
	}

	public static String getNetworkOperatorName() {
		if (ClientProperties.getApplicationContext() != null) {
			TelephonyManager tm = (TelephonyManager)ClientProperties.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			return tm.getNetworkOperatorName();
		}

		return "";
	}

	public static int getScreenDensity() {
		if (ClientProperties.getApplicationContext() != null) {
			return ClientProperties.getApplicationContext().getResources().getDisplayMetrics().densityDpi;
		}

		return -1;
	}

	public static int getScreenWidth() {
		if (ClientProperties.getApplicationContext() != null) {
			return ClientProperties.getApplicationContext().getResources().getDisplayMetrics().widthPixels;
		}

		return -1;
	}

	public static int getScreenHeight() {
		if (ClientProperties.getApplicationContext() != null) {
			return ClientProperties.getApplicationContext().getResources().getDisplayMetrics().heightPixels;
		}

		return -1;
	}

	public static boolean isActiveNetworkConnected () {
		if (ClientProperties.getApplicationContext() != null) {
			ConnectivityManager cm = (ConnectivityManager)ClientProperties.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

			if (cm != null) {
				NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
				return activeNetwork != null && activeNetwork.isConnected();
			}
		}

		return false;
	}

	public static boolean isAppInstalled(String pkgname) {
		if (ClientProperties.getApplicationContext() != null) {
			PackageManager pm = ClientProperties.getApplicationContext().getPackageManager();

			try {
				PackageInfo pkgInfo = pm.getPackageInfo(pkgname, 0);

				if(pkgInfo != null && pkgInfo.packageName != null && pkgname.equals(pkgInfo.packageName)) {
					return true;
				}
			} catch(PackageManager.NameNotFoundException e) {
				DeviceLog.exception("Couldn't find package: " + pkgname, e);
				return false;
			}
		}

		return false;
	}

	public static List<Map<String, Object>> getInstalledPackages(boolean hash) {
		List<Map<String,Object>> returnList = new ArrayList<>();

		if (ClientProperties.getApplicationContext() != null) {
			PackageManager pm = ClientProperties.getApplicationContext().getPackageManager();

			for(PackageInfo pkg : pm.getInstalledPackages(0)) {
				HashMap<String, Object> packageEntry = new HashMap<>();

				if (hash) {
					packageEntry.put("name", Utilities.Sha256(pkg.packageName));
				}
				else {
					packageEntry.put("name", pkg.packageName);
				}

				if (pkg.firstInstallTime > 0) {
					packageEntry.put("time", pkg.firstInstallTime);
				}

				String installer = pm.getInstallerPackageName(pkg.packageName);
				if (installer != null && !installer.isEmpty()) {
					packageEntry.put("installer", installer);
				}

				returnList.add(packageEntry);
			}
		}

		return returnList;
	}

	public static String getUniqueEventId() {
		return UUID.randomUUID().toString();
	}

	@SuppressWarnings("deprecation")
	public static boolean isWiredHeadsetOn() {
		if (ClientProperties.getApplicationContext() != null) {
			AudioManager am = (AudioManager)ClientProperties.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

			// Note: This method is officially deprecated but documentation gives more fine-grained approach. This method
			// returns whether headset is connected or not. Audio playback might be routed through other means. Since we are
			// not using this value for making audio routing decisions, using this is ok and supported by Android docs.
			return am.isWiredHeadsetOn();
		}

		return false;
	}

	public static String getSystemProperty (String propertyName, String defaultValue) {
		if (defaultValue != null) {
			return System.getProperty(propertyName, defaultValue);
		}

		return System.getProperty(propertyName);
	}

	public static int getRingerMode () {
		if (ClientProperties.getApplicationContext() != null) {
			AudioManager am = (AudioManager)ClientProperties.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

			if (am != null)
				return am.getRingerMode();
			else
				return -2;
		}

		return -1;
	}

	public static int getStreamVolume(int streamType) {
		if (ClientProperties.getApplicationContext() != null) {
			AudioManager am = (AudioManager)ClientProperties.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

			if (am != null)
				return am.getStreamVolume(streamType);
			else
				return -2;
		}

		return -1;
	}

	public static int getScreenBrightness () {
		if (ClientProperties.getApplicationContext() != null) {
			return Settings.System.getInt(ClientProperties.getApplicationContext().getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, -1);
		}

		return -1;
	}

	public static long getFreeSpace (File file) {
		if (file != null && file.exists()) {
			return Math.round(file.getFreeSpace() / 1024);
		}

		return -1;
	}

	public static long getTotalSpace (File file) {
		if (file != null && file.exists()) {
			return Math.round(file.getTotalSpace() / 1024);
		}

		return -1;
	}

	public static float getBatteryLevel () {
		if (ClientProperties.getApplicationContext() != null) {
			Intent i = ClientProperties.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

			if (i != null) {
				int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				return level / (float)scale;
			}
		}

		return -1;
	}


	public static int getBatteryStatus () {
		if (ClientProperties.getApplicationContext() != null) {
			Intent i = ClientProperties.getApplicationContext().registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

			if (i != null) {
				return i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			}
		}

		return -1;
	}

	public static long getTotalMemory () {
		return getMemoryInfo(MemoryInfoType.TOTAL_MEMORY);
	}

	public static long getFreeMemory () {
		return getMemoryInfo(MemoryInfoType.FREE_MEMORY);
	}

	private static long getMemoryInfo (MemoryInfoType infoType) {
		int lineNumber = -1;

		switch (infoType) {
			case TOTAL_MEMORY:
				lineNumber = 1;
				break;
			case FREE_MEMORY:
				lineNumber = 2;
				break;
			default:
				break;
		}

		RandomAccessFile reader = null;
		String line = null;

		try {
			reader = new RandomAccessFile("/proc/meminfo", "r");

			for (int i = 0; i < lineNumber; i++) {
				line = reader.readLine();
			}
			return getMemoryValueFromString(line);
		} catch (IOException e) {
			DeviceLog.exception("Error while reading memory info: " + infoType, e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				DeviceLog.exception("Error closing RandomAccessFile", e);
			}
		}

		return -1;
	}

	private static long getMemoryValueFromString (String memVal) {
		if (memVal != null) {
			Pattern p = Pattern.compile("(\\d+)");
			Matcher m = p.matcher(memVal);
			String value = "";

			while (m.find()) {
				value = m.group(1);
			}

			return Long.parseLong(value);
		}

		return -1;
	}

	public static boolean isRooted() {
		try {
			return searchPathForBinary("su");
		} catch(Exception e) {
			DeviceLog.exception("Rooted check failed", e);
			return false;
		}
	}

	private static boolean searchPathForBinary(String binary) {
		String[] paths = System.getenv("PATH").split(":");
		for(String path : paths) {
			File pathDir = new File(path);
			if(pathDir.exists() && pathDir.isDirectory()) {
				File[] pathDirFiles = pathDir.listFiles();
				if(pathDirFiles != null) {
					for (File fileInPath : pathDirFiles) {
						if (fileInPath.getName().equals(binary)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

}