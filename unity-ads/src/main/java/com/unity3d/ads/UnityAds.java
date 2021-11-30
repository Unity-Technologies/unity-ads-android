package com.unity3d.ads;

import android.app.Activity;
import android.content.Context;

import com.unity3d.services.UnityServices;
import com.unity3d.services.ads.UnityAdsImplementation;
import com.unity3d.services.core.log.DeviceLog;

public final class UnityAds {
	
	public enum UnityAdsShowCompletionState {
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
	 * Enumeration of UnityAds initialization errors.
	 */
	public enum UnityAdsInitializationError {
		/**
		 * Error related to environment or internal services
		 */
		INTERNAL_ERROR,

		/**
		 * Error related to invalid arguments
		 */
		INVALID_ARGUMENT,

		/**
		 * Error related to url being blocked
		 */
		AD_BLOCKER_DETECTED
	}

	/**
	 * Enumeration of UnityAds load errors.
	 */
	public enum UnityAdsLoadError {
		/**
		 * Error related to SDK not initialized
		 */
		INITIALIZE_FAILED,

		/**
		 * Error related to environment or internal services
		 */
		INTERNAL_ERROR,

		/**
		 * Error related to invalid arguments
		 */
		INVALID_ARGUMENT,

		/**
		 * Error related to there being no ads available
		 */
		NO_FILL,

		/**
		 * Error related to an Ad being unable to load within a specified time frame
		 */
		TIMEOUT
	}

	/**
	 * Enumeration of UnityAds show errors.
	 */
	public enum UnityAdsShowError {
		/**
		 * Error related to SDK not initialized
		 */
		NOT_INITIALIZED,

		/**
		 * Error related to placement not being ready
		 */
		NOT_READY,

		/**
		 * Error related to the video player
		 */
		VIDEO_PLAYER_ERROR,

		/**
		 * Error related to invalid arguments
		 */
		INVALID_ARGUMENT,

		/**
		 * Error related to internet connection
		 */
		NO_CONNECTION,

		/**
		 * Error related to ad is already being showed
		 */
		ALREADY_SHOWING,

		/**
		 * Error related to environment or internal services
		 */
		INTERNAL_ERROR,
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param context Current Android context of calling app in favor of Application Context
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 */
	public static void initialize(final Context context, final String gameId) {
		boolean testMode = false;
		UnityAdsImplementation.initialize(context, gameId, testMode, null);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param context Current Android context of calling app in favor of Application Context
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param initializationListener Listener for IUnityAdsInitializationListener callbacks
	 */
	public static void initialize(final Context context, final String gameId, final IUnityAdsInitializationListener initializationListener) {
		boolean testMode = false;
		UnityAdsImplementation.initialize(context, gameId, testMode, initializationListener);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param context Current Android context of calling app in favor of Application Context
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param testMode If true, only test ads are shown
	 */
	public static void initialize(final Context context, final String gameId, final boolean testMode) {
		UnityAdsImplementation.initialize(context, gameId, testMode, null);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param context Current Android context of calling app in favor of Application Context
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param testMode If true, only test ads are shown
	 * @param initializationListener Listener for IUnityAdsInitializationListener callbacks
	 */
	public static void initialize(final Context context, final String gameId, final boolean testMode, final IUnityAdsInitializationListener initializationListener) {
		UnityAdsImplementation.initialize(context, gameId, testMode, initializationListener);
	}

	/**
	 * Checks if Unity Ads has been initialized. This might be useful for debugging initialization problems.
	 *
	 * @return If true, Unity Ads has been successfully initialized
	 */
	public static boolean isInitialized() {
		return UnityServices.isInitialized();
	}

	/**
	 * Checks if current device supports running Unity Ads
	 *
	 * @return If true, device supports Unity Ads. If false, device can't initialize or show Unity Ads.
	 */
	public static boolean isSupported() {
		return UnityAdsImplementation.isSupported();
	}

	/**
	 * Get current SDK version
	 *
	 * @return Current SDK version name
	 */
	public static String getVersion() {
		return UnityAdsImplementation.getVersion();
	}

	/**
	 * Show one advertisement with custom placement.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 */
	@Deprecated
	public static void show(final Activity activity, final String placementId) {
		UnityAdsImplementation.show(activity, placementId, null);
	}

	/**
	 * Show one advertisement with custom placement and custom options.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @param showListener Listener for IUnityAdsShowListener callbacks
	 */
	public static void show(final Activity activity, final String placementId, final IUnityAdsShowListener showListener) {
		UnityAdsImplementation.show(activity, placementId, showListener);
	}

	/**
	 * Show one advertisement with custom placement and custom options.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @param options Custom options
	 */
	@Deprecated
	public static void show(final Activity activity, final String placementId, final UnityAdsShowOptions options) {
		UnityAdsImplementation.show(activity, placementId, options, null);
	}

	/**
	 * Show one advertisement with custom placement and custom options.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @param options Custom options
	 * @param showListener Listener for IUnityAdsShowListener callbacks
	 */
	public static void show(final Activity activity, final String placementId, final UnityAdsShowOptions options, final IUnityAdsShowListener showListener) {
		UnityAdsImplementation.show(activity, placementId, options, showListener);
	}

	/**
	 * Toggles debug mode on/off
	 *
	 * @param debugMode If true, debug mode is on and there will be lots of debug output from Unity Ads. If false, there will be only some short log messages from Unity Ads.
	 */
	public static void setDebugMode(boolean debugMode) {
		UnityAdsImplementation.setDebugMode(debugMode);
	}

	/**
	 * Get current debug mode status
	 *
	 * @return If true, debug mode is on. If false, debug mode is off.
	 */
	public static boolean getDebugMode() {
		return UnityAdsImplementation.getDebugMode();
	}

	/**
	 * Request fill for a specific placement ID.
	 *
	 * @param placementId The placement ID to be loaded.
	 */
	@Deprecated
	public static void load(final String placementId) {
		load(placementId, new IUnityAdsLoadListener() {
			@Override
			public void onUnityAdsAdLoaded(String placementId) {

			}

			@Override
			public void onUnityAdsFailedToLoad(String placementId, UnityAdsLoadError error, String message) {

			}
		});
	}

	/**
	 * Request fill for a specific placement ID.
	 *
	 * @param placementId The placement ID to be loaded.
	 * @param listener The listener which is going to be notified about load request result.
	 */
	public static void load(final String placementId, final IUnityAdsLoadListener listener) {
		UnityAdsImplementation.load(placementId, new UnityAdsLoadOptions(), listener);
	}

	/**
	 * Request fill for a specific placement ID with custom options.
	 *
	 * @param placementId The placement ID to be loaded.
	 * @param loadOptions Custom options.
	 * @param listener The listener which is going to be notified about load request result.
	 */
	public static void load(final String placementId, final UnityAdsLoadOptions loadOptions, final IUnityAdsLoadListener listener) {
		UnityAdsImplementation.load(placementId, loadOptions, listener);
	}

	/**
	 * Get request token.
	 */
	public static String getToken() {
		return UnityAdsImplementation.getToken();
	}

}
