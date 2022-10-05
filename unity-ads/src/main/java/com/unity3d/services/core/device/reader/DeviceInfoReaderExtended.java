package com.unity3d.services.core.device.reader;

import android.webkit.WebSettings;

import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DeviceInfoReaderExtended implements IDeviceInfoReader {
	private final IDeviceInfoReader _deviceInfoReader;

	public DeviceInfoReaderExtended(IDeviceInfoReader deviceInfoReader) {
		_deviceInfoReader = deviceInfoReader;
	}

	@Override
	public Map<String, Object> getDeviceInfoData() {
		Map<String, Object> deviceInfoData = _deviceInfoReader.getDeviceInfoData();
		deviceInfoData.put("bundleId", ClientProperties.getAppName());
		deviceInfoData.put("encrypted", ClientProperties.isAppDebuggable());
		deviceInfoData.put("rooted", Device.isRooted());
		deviceInfoData.put("osVersion", Device.getOsVersion());
		deviceInfoData.put("deviceModel", Device.getModel());
		deviceInfoData.put("language", Locale.getDefault().toString());
		deviceInfoData.put("connectionType", Device.getConnectionType());
		deviceInfoData.put("screenHeight", Device.getScreenHeight());
		deviceInfoData.put("screenWidth", Device.getScreenWidth());
		deviceInfoData.put("deviceMake", Device.getManufacturer());
		deviceInfoData.put("screenDensity", Device.getScreenDensity());
		deviceInfoData.put("screenSize",  Device.getScreenLayout());
		deviceInfoData.put("limitAdTracking",  Device.isLimitAdTrackingEnabled());
		deviceInfoData.put("networkOperator", Device.getNetworkOperator());
		deviceInfoData.put("volume", Device.getStreamVolume(1));
		deviceInfoData.put("deviceFreeSpace", Device.getFreeSpace(ClientProperties.getApplicationContext().getCacheDir()));

		// PRELOAD required data.
		deviceInfoData.put("apiLevel", String.valueOf(Device.getApiLevel()));
		deviceInfoData.put("networkType",  Device.getNetworkType());
		deviceInfoData.put("bundleVersion", ClientProperties.getAppVersion());
		try {
			deviceInfoData.put("timeZone", TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT, Locale.US));
		} catch (AssertionError assertionError) {
			// This can occur on some flavours of Android 8.1 and is a workaround for an OS bug
			DeviceLog.error("Could not read timeZone information: %s", assertionError.getMessage());
		}
		deviceInfoData.put("timeZoneOffset", TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000);
		deviceInfoData.put("webviewUa", WebSettings.getDefaultUserAgent(ClientProperties.getApplicationContext()));
		deviceInfoData.put("networkOperatorName", Device.getNetworkOperatorName());
		deviceInfoData.put("wiredHeadset", Device.isWiredHeadsetOn());
		deviceInfoData.put("versionCode", SdkProperties.getVersionCode());
		deviceInfoData.put("stores", "google");
		deviceInfoData.put("appStartTime", SdkProperties.getInitializationTimeEpoch() / 1000);

		// Native Config
		deviceInfoData.put("sdkVersionName", SdkProperties.getVersionName());

		// Glyph signals related
		deviceInfoData.put("eventTimeStamp", System.currentTimeMillis() / 1000);
		deviceInfoData.put("cpuCount", Device.getCPUCount());
		deviceInfoData.put("usbConnected", Device.isUSBConnected());
		try {
			deviceInfoData.put("apkHash", Device.getApkDigest());
		} catch (Exception e) {
			DeviceLog.error("Could not get APK Digest");
		}
		deviceInfoData.put("apkDeveloperSigningCertificateHash", Device.getCertificateFingerprint());
		deviceInfoData.put("deviceUpTime", Device.getUptime());
		deviceInfoData.put("deviceElapsedRealtime", Device.getElapsedRealtime());
		deviceInfoData.put("adbEnabled", Device.isAdbEnabled());
		deviceInfoData.put("androidFingerprint", Device.getFingerprint());
		deviceInfoData.put("batteryStatus", Device.getBatteryStatus());
		deviceInfoData.put("batteryLevel", Device.getBatteryLevel());
		deviceInfoData.put("networkMetered", Device.getNetworkMetered());

		// Misc
		deviceInfoData.put("test", SdkProperties.isTestMode());
		deviceInfoData.put("callType", "token");
		return deviceInfoData;
	}


}
