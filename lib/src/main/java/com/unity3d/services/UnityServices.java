package com.unity3d.services;

import android.app.Activity;
import android.os.Build;

import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.EnvironmentCheck;
import com.unity3d.services.core.configuration.InitializeThread;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;

public class UnityServices {
	private static boolean _configurationInitialized = false;

	public enum UnityServicesError {
		INVALID_ARGUMENT,
		INIT_SANITY_CHECK_FAIL
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *  @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 * @param testMode If true, only test ads are shown
	 * @param enablePerPlacementLoad If true, disables automatic requests, and allows the load() function to request placements instead
	 */
	public static void initialize(final Activity activity, final String gameId, final IUnityServicesListener listener, final boolean testMode, final boolean enablePerPlacementLoad) {
		DeviceLog.entered();

		// Allow init call only once. Configuration thread will take care of retries in case of network failures.
		if (_configurationInitialized) {
			if (ClientProperties.getGameId() != null && !ClientProperties.getGameId().equals(gameId)) {
				DeviceLog.warning("You are trying to re-initialize with a different gameId");
			}

			return;
		}
		_configurationInitialized = true;

		if(!isSupported()) {
			DeviceLog.error("Error while initializing Unity Services: device is not supported");
			return;
		}

		SdkProperties.setInitializationTime(System.currentTimeMillis());

		if(gameId == null || gameId.length() == 0) {
			DeviceLog.error("Error while initializing Unity Services: empty game ID, halting Unity Ads init");
			if(listener != null) {
				listener.onUnityServicesError(UnityServicesError.INVALID_ARGUMENT, "Empty game ID");
			}
			return;
		}

		if(activity == null) {
			DeviceLog.error("Error while initializing Unity Services: null activity, halting Unity Ads init");
			if(listener != null) {
				listener.onUnityServicesError(UnityServicesError.INVALID_ARGUMENT, "Null activity");
			}
			return;
		}

		if(testMode) {
			DeviceLog.info("Initializing Unity Services " + SdkProperties.getVersionName() + " (" + SdkProperties.getVersionCode() + ") with game id " + gameId + " in test mode");
		} else {
			DeviceLog.info("Initializing Unity Services " + SdkProperties.getVersionName() + " (" + SdkProperties.getVersionCode() + ") with game id " + gameId + " in production mode");
		}

		SdkProperties.setDebugMode(SdkProperties.getDebugMode());
		SdkProperties.setListener(listener);
		ClientProperties.setGameId(gameId);
		ClientProperties.setApplicationContext(activity.getApplicationContext());
		ClientProperties.setApplication(activity.getApplication());
		SdkProperties.setPerPlacementLoadEnabled(enablePerPlacementLoad);
		SdkProperties.setTestMode(testMode);

		if(EnvironmentCheck.isEnvironmentOk()) {
			DeviceLog.info("Unity Services environment check OK");
		} else {
			DeviceLog.error("Error during Unity Services environment check, halting Unity Services init");
			if(listener != null) {
				listener.onUnityServicesError(UnityServicesError.INIT_SANITY_CHECK_FAIL, "Unity Services init environment check failed");
			}
			return;
		}

		Configuration configuration = new Configuration();
		InitializeThread.initialize(configuration);
	}

	public static boolean isSupported() {
		return Build.VERSION.SDK_INT >= 16;
	}

	public static boolean isInitialized() {
		return SdkProperties.isInitialized();
	}

	public static String getVersion() {
		return SdkProperties.getVersionName();
	}

	/**
	 * Toggles debug mode on/off
	 *
	 * @param debugMode If true, debug mode is on and there will be lots of debug output from Unity Services. If false, there will be only some short log messages from Unity Services.
	 */
	public static void setDebugMode(boolean debugMode) {
		SdkProperties.setDebugMode(debugMode);
	}

	/**
	 * Get current debug mode status
	 *
	 * @return If true, debug mode is on. If false, debug mode is off.
	 */
	public static boolean getDebugMode() {
		return SdkProperties.getDebugMode();
	}
}
