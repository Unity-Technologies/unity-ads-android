package com.unity3d.services;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.EnvironmentCheck;
import com.unity3d.services.core.configuration.InitializeThread;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;

public class UnityServices {

	public enum UnityServicesError {
		INVALID_ARGUMENT,
		INIT_SANITY_CHECK_FAIL
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *  @param context Current Android application context of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 * @param testMode If true, only test ads are shown
	 * @param enablePerPlacementLoad If true, disables automatic requests, and allows the load() function to request placements instead
	 * @param initializationListener Listener for IUnityAdsInitializationListener callbacks
	 */
	public static void initialize(final Context context, final String gameId, final IUnityServicesListener listener, final boolean testMode, final boolean enablePerPlacementLoad, final IUnityAdsInitializationListener initializationListener) {
		DeviceLog.entered();

		if (SdkProperties.getCurrentInitializationState() != SdkProperties.InitializationState.NOT_INITIALIZED) {
			String differingParameters = "";

			String previousGameId = ClientProperties.getGameId();
			if (previousGameId != null && !previousGameId.equals(gameId)) {
				differingParameters += createExpectedParametersString("Game ID", ClientProperties.getGameId(), gameId);
			}

			boolean previousTestMode = SdkProperties.isTestMode();
			if (previousTestMode != testMode) {
				differingParameters += createExpectedParametersString("Test Mode", previousTestMode, testMode);
			}

			boolean previousLoadEnabled = SdkProperties.isPerPlacementLoadEnabled();
			if (previousLoadEnabled != enablePerPlacementLoad) {
				differingParameters += createExpectedParametersString("Enable Per Placement Load", previousLoadEnabled, enablePerPlacementLoad);
			}

			if (!TextUtils.isEmpty(differingParameters)) {
				String message = "Unity Ads SDK failed to initialize due to already being initialized with different parameters" + differingParameters;
				DeviceLog.warning(message);
				if (initializationListener != null) {
					initializationListener.onInitializationFailed(UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT, message);
				}
				if (listener != null) {
					listener.onUnityServicesError(UnityServicesError.INVALID_ARGUMENT, message);
				}
				return;
			}
		}

		SdkProperties.addInitializationListener(initializationListener);

		if(SdkProperties.getCurrentInitializationState() == SdkProperties.InitializationState.INITIALIZED_SUCCESSFULLY) {
			SdkProperties.notifyInitializationComplete();
			return;
		}

		if(SdkProperties.getCurrentInitializationState() == SdkProperties.InitializationState.INITIALIZED_FAILED) {
			SdkProperties.notifyInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, "Unity Ads SDK failed to initialize due to previous failed reason");
			return;
		}

		if(SdkProperties.getCurrentInitializationState() == SdkProperties.InitializationState.INITIALIZING) {
			return;
		}
		SdkProperties.setInitializeState(SdkProperties.InitializationState.INITIALIZING);
		ClientProperties.setGameId(gameId);
		SdkProperties.setTestMode(testMode);
		SdkProperties.setPerPlacementLoadEnabled(enablePerPlacementLoad);

		if(!isSupported()) {
			DeviceLog.error("Error while initializing Unity Services: device is not supported");
			SdkProperties.notifyInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, "Unity Ads SDK failed to initialize due to device is not supported");
			return;
		}

		SdkProperties.setInitializationTime(Device.getElapsedRealtime());

		if(gameId == null || gameId.length() == 0) {
			DeviceLog.error("Error while initializing Unity Services: empty game ID, halting Unity Ads init");
			SdkProperties.notifyInitializationFailed(UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT, "Unity Ads SDK failed to initialize due to empty game ID");
			if(listener != null) {
				listener.onUnityServicesError(UnityServicesError.INVALID_ARGUMENT, "Empty game ID");
			}
			return;
		}

		if(context == null) {
			DeviceLog.error("Error while initializing Unity Services: null context, halting Unity Ads init");
			SdkProperties.notifyInitializationFailed(UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT, "Unity Ads SDK failed to initialize due to null context");
			if(listener != null) {
				listener.onUnityServicesError(UnityServicesError.INVALID_ARGUMENT, "Null context");
			}
			return;
		}

		if (context instanceof Application) {
			ClientProperties.setApplication((Application) context);
		} else if (context instanceof Activity) {
			ClientProperties.setApplication(((Activity) context).getApplication());
		} else {
			DeviceLog.error("Error while initializing Unity Services: invalid context, halting Unity Ads init");
			SdkProperties.notifyInitializationFailed(UnityAds.UnityAdsInitializationError.INVALID_ARGUMENT, "Unity Ads SDK failed to initialize due to invalid context");
			if(listener != null) {
				listener.onUnityServicesError(UnityServicesError.INVALID_ARGUMENT, "Invalid context");
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
		ClientProperties.setApplicationContext(context.getApplicationContext());

		if(!EnvironmentCheck.isEnvironmentOk()) {
			DeviceLog.error("Error during Unity Services environment check, halting Unity Services init");
			SdkProperties.notifyInitializationFailed(UnityAds.UnityAdsInitializationError.INTERNAL_ERROR, "Unity Ads SDK failed to initialize due to environment check failed");
			if(listener != null) {
				listener.onUnityServicesError(UnityServicesError.INIT_SANITY_CHECK_FAIL, "Unity Services init environment check failed");
			}
			return;
		}
		DeviceLog.info("Unity Services environment check OK");

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

	private static String createExpectedParametersString(String fieldName, Object current, Object received) {
		return "\n - " + fieldName + " Current: " + current.toString() + " | Received: " + received.toString();
	}
}
