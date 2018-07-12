package com.unity3d.ads;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import com.unity3d.ads.adunit.AdUnitOpen;
import com.unity3d.ads.placement.Placement;
import com.unity3d.ads.cache.CacheThread;
import com.unity3d.ads.configuration.Configuration;
import com.unity3d.ads.configuration.EnvironmentCheck;
import com.unity3d.ads.configuration.InitializeThread;
import com.unity3d.ads.connectivity.ConnectivityMonitor;
import com.unity3d.ads.log.DeviceLog;
import com.unity3d.ads.misc.Utilities;
import com.unity3d.ads.properties.ClientProperties;
import com.unity3d.ads.properties.SdkProperties;
import com.unity3d.ads.configuration.InitializeThread;

import org.json.JSONException;
import org.json.JSONObject;

public final class UnityAds {
	private static boolean _configurationInitialized = false;
	private static boolean _debugMode = false;

	/**
	 *  An enumeration for the completion state of an ad.
	 */
	public enum FinishState {
		/**
		 *  A state that indicates that the ad did not successfully display.
		 */
		ERROR,
		/**
		 *  A state that indicates that the user skipped the ad.
		 */
		SKIPPED,
		/**
		 *  A state that indicates that the ad was played entirely.
		 */
		COMPLETED
	}

	/**
	 * Describes state of Unity Ads placements. All placement states other than READY imply that placement is not currently ready to show ads.
	 */
	public enum PlacementState {
		/**
		 * Placement is ready to show ads. You can call show method and ad unit will open.
		 */
		READY,

		/**
		 * Current placement state is not available. SDK is not initialized or this placement has not been configured in Unity Ads admin tools.
		 */
		NOT_AVAILABLE,

		/**
		 * Placement is disabled. Placement can be enabled via Unity Ads admin tools.
		 */
		DISABLED,

		/**
		 * Placement is not yet ready but it will be ready in the future. Most likely reason is caching.
		 */
		WAITING,

		/**
		 * Placement is properly configured but there are currently no ads available for the placement.
		 */
		NO_FILL
	}

	public enum UnityAdsError {
		NOT_INITIALIZED,
		INITIALIZE_FAILED,
		INVALID_ARGUMENT,
		VIDEO_PLAYER_ERROR,
		INIT_SANITY_CHECK_FAIL,
		AD_BLOCKER_DETECTED,
		FILE_IO_ERROR,
		DEVICE_ID_ERROR,
		SHOW_ERROR,
		INTERNAL_ERROR
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 */
	public static void initialize(final Activity activity, final String gameId, final IUnityAdsListener listener) {
		initialize(activity, gameId, listener, false);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 * @param testMode If true, only test ads are shown
	 */
	public static void initialize(final Activity activity, final String gameId, final IUnityAdsListener listener, final boolean testMode) {
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
			DeviceLog.error("Error while initializing Unity Ads: device is not supported");
			return;
		}

		SdkProperties.setInitializationTime(System.currentTimeMillis());

		if(gameId == null || gameId.length() == 0) {
			DeviceLog.error("Error while initializing Unity Ads: empty game ID, halting Unity Ads init");
			if(listener != null) {
				listener.onUnityAdsError(UnityAdsError.INVALID_ARGUMENT, "Empty game ID");
			}
			return;
		}

		if(activity == null) {
			DeviceLog.error("Error while initializing Unity Ads: null activity, halting Unity Ads init");
			if(listener != null) {
				listener.onUnityAdsError(UnityAdsError.INVALID_ARGUMENT, "Null activity");
			}
			return;
		}

		if(testMode) {
			DeviceLog.info("Initializing Unity Ads " + SdkProperties.getVersionName() + " (" + SdkProperties.getVersionCode() + ") with game id " + gameId + " in test mode");
		} else {
			DeviceLog.info("Initializing Unity Ads " + SdkProperties.getVersionName() + " (" + SdkProperties.getVersionCode() + ") with game id " + gameId + " in production mode");
		}

		setDebugMode(_debugMode);

		ClientProperties.setGameId(gameId);
		ClientProperties.setListener(listener);
		ClientProperties.setApplicationContext(activity.getApplicationContext());
		ClientProperties.setApplication(activity.getApplication());
		SdkProperties.setTestMode(testMode);

		if(EnvironmentCheck.isEnvironmentOk()) {
			DeviceLog.info("Unity Ads environment check OK");
		} else {
			DeviceLog.error("Error during Unity Ads environment check, halting Unity Ads init");
			if(listener != null) {
				listener.onUnityAdsError(UnityAdsError.INIT_SANITY_CHECK_FAIL, "Unity Ads init environment check failed");
			}
			return;
		}

		Configuration configuration = new Configuration();
		Class[] apiClassList = {
			com.unity3d.ads.api.AdUnit.class,
			com.unity3d.ads.api.Broadcast.class,
			com.unity3d.ads.api.Cache.class,
			com.unity3d.ads.api.Connectivity.class,
			com.unity3d.ads.api.DeviceInfo.class,
			com.unity3d.ads.api.Listener.class,
			com.unity3d.ads.api.Storage.class,
			com.unity3d.ads.api.Sdk.class,
			com.unity3d.ads.api.Request.class,
			com.unity3d.ads.api.Resolve.class,
			com.unity3d.ads.api.VideoPlayer.class,
			com.unity3d.ads.api.Placement.class,
			com.unity3d.ads.api.Intent.class,
			com.unity3d.ads.api.Lifecycle.class,
			com.unity3d.ads.api.WebPlayer.class,
			com.unity3d.ads.api.Preferences.class,
			com.unity3d.ads.api.Purchasing.class,
			com.unity3d.ads.api.SensorInfo.class
		};

		configuration.setWebAppApiClassList(apiClassList);
		InitializeThread.initialize(configuration);
	}

	/**
	 * Checks if Unity Ads has been initialized. This might be useful for debugging initialization problems.
	 *
	 * @return If true, Unity Ads has been successfully initialized
	 */
	public static boolean isInitialized() {
		return SdkProperties.isInitialized();
	}

	/**
	 * Change listener for IUnityAdsListener callbacks
	 *
	 * @param listener New listener for IUnityAdsListener callbacks
	 */
	public static void setListener(IUnityAdsListener listener) {
		ClientProperties.setListener(listener);
	}

	/**
	 * Get current listener for IUnityAdsListener callbacks
	 *
	 * @return Current listener for IUnityAdsListener callbacks
	 */
	public static IUnityAdsListener getListener() {
		return ClientProperties.getListener();
	}

	/**
	 * Checks if current device supports running Unity Ads
	 *
	 * @return If true, device supports Unity Ads. If false, device can't initialize or show Unity Ads.
	 */
	public static boolean isSupported() {
		return Build.VERSION.SDK_INT >= 16;
	}

	/**
	 * Get current SDK version
	 *
	 * @return Current SDK version name
	 */
	public static String getVersion() {
		return SdkProperties.getVersionName();
	}

	/**
	 * Check if default placement is ready to show ads
	 *
	 * @return If true, default placement is ready to show ads
	 */
	public static boolean isReady() {
		return isSupported() && isInitialized() && Placement.isReady();
	}

	/**
	 * Check if placement is ready to show ads
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @return If true, placement is ready to show ads
	 */
	public static boolean isReady(String placementId) {
		return isSupported() && isInitialized() && placementId != null && Placement.isReady(placementId);
	}

	/**
	 * Get current state of default placement
	 *
	 * @return If PlacementState.READY, default placement is ready to show ads. Other states give detailed reasons why placement is not ready.
	 */
	public static PlacementState getPlacementState() {
		if(isSupported() && isInitialized()) {
			return Placement.getPlacementState();
		} else {
			return UnityAds.PlacementState.NOT_AVAILABLE;
		}
	}

	/**
	 * Get current state of a placement
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @return If PlacementState.READY, placement is ready to show ads. Other states give detailed reasons why placement is not ready.
	 */
	public static PlacementState getPlacementState(String placementId) {
		if(isSupported() && isInitialized() && placementId != null) {
			return Placement.getPlacementState(placementId);
		} else {
			return UnityAds.PlacementState.NOT_AVAILABLE;
		}
	}

	/**
	 * Show one advertisement using default placement.
	 *
	 * @param activity Current Android activity of calling app
	 */
	public static void show(final Activity activity) {
		if(Placement.getDefaultPlacement() != null) {
			show(activity, Placement.getDefaultPlacement());
		} else {
			handleShowError("", UnityAdsError.NOT_INITIALIZED, "Unity Ads default placement is not initialized");
		}
	}

	/**
	 * Show one advertisement with custom placement.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 */
	public static void show(final Activity activity, final String placementId) {
		if(activity == null) {
			handleShowError(placementId, UnityAdsError.INVALID_ARGUMENT, "Activity must not be null");
			return;
		}

		if(isReady(placementId)) {
			DeviceLog.info("Unity Ads opening new ad unit for placement " + placementId);
			ClientProperties.setActivity(activity);
			new Thread(new Runnable() {
				@Override
				public void run() {
					Display defaultDisplay = ((WindowManager)activity.getSystemService(activity.WINDOW_SERVICE)).getDefaultDisplay();
					JSONObject options = new JSONObject();
					try {
						options.put("requestedOrientation", activity.getRequestedOrientation());

						JSONObject display = new JSONObject();
						display.put("rotation", defaultDisplay.getRotation());
						if (Build.VERSION.SDK_INT >= 13) {
							Point displaySize = new Point();
							defaultDisplay.getSize(displaySize);
							display.put("width", displaySize.x);
							display.put("height", displaySize.y);
						} else {
							display.put("width", defaultDisplay.getWidth());
							display.put("height", defaultDisplay.getHeight());
						}
						options.put("display", display);
					} catch(JSONException e) {
						DeviceLog.exception("JSON error while constructing show options", e);
					}

					try {
						if(!AdUnitOpen.open(placementId, options)) {
							handleShowError(placementId, UnityAdsError.INTERNAL_ERROR, "Webapp timeout, shutting down Unity Ads");
							
							InitializeThread.reset();
						}
					}
					catch (NoSuchMethodException exception) {
						DeviceLog.exception("Could not get callback method", exception);
						handleShowError(placementId, UnityAdsError.SHOW_ERROR, "Could not get com.unity3d.ads.properties.showCallback method");
					}
				}
			}).start();
		} else {
			if (!isSupported()) {
				handleShowError(placementId, UnityAdsError.NOT_INITIALIZED, "Unity Ads is not supported for this device");
			} else if(!isInitialized()) {
				handleShowError(placementId, UnityAdsError.NOT_INITIALIZED, "Unity Ads is not initialized");
			} else {
				handleShowError(placementId, UnityAdsError.SHOW_ERROR, "Placement \"" + placementId + "\" is not ready");
			}
		}
	}

	private static void handleShowError(final String placementId, final UnityAdsError error, final String message) {
		final String errorMessage = "Unity Ads show failed: " + message;
		DeviceLog.error(errorMessage);

		final IUnityAdsListener listener = ClientProperties.getListener();
		if(listener != null) {
			Utilities.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listener.onUnityAdsError(error, errorMessage);

					if(placementId != null) {
						listener.onUnityAdsFinish(placementId, FinishState.ERROR);
					} else {
						listener.onUnityAdsFinish("", FinishState.ERROR);
					}
				}
			});
		}
	}

	/**
	 * Toggles debug mode on/off
	 *
	 * @param debugMode If true, debug mode is on and there will be lots of debug output from Unity Ads. If false, there will be only some short log messages from Unity Ads.
	 */
	public static void setDebugMode(boolean debugMode) {
		_debugMode = debugMode;

		if(debugMode) {
			DeviceLog.setLogLevel(DeviceLog.LOGLEVEL_DEBUG);
		} else {
			DeviceLog.setLogLevel(DeviceLog.LOGLEVEL_INFO);
		}
	}

	/**
	 * Get current debug mode status
	 *
	 * @return If true, debug mode is on. If false, debug mode is off.
	 */
	public static boolean getDebugMode() {
		return _debugMode;
	}
}
