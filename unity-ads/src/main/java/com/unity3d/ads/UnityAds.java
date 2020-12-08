package com.unity3d.ads;

import android.app.Activity;
import android.content.Context;

import com.unity3d.services.UnityServices;
import com.unity3d.services.ads.UnityAdsImplementation;
import com.unity3d.services.core.log.DeviceLog;

public final class UnityAds {
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
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 * @deprecated this method is deprecated in favor of {@link #initialize(Context, String)} use {@link #addListener(IUnityAdsListener)} to add a listener and use Application Context
	 */
	@Deprecated
	public static void initialize(final Activity activity, final String gameId, final IUnityAdsListener listener) {
		boolean testMode = false;
		boolean usePerPlacementLoad = false;
		initialize(activity, gameId, listener, testMode, usePerPlacementLoad, null);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @deprecated this method is deprecated in favor of {@link #initialize(Context, String)} use Application Context
	 */
	@Deprecated
	public static void initialize(final Activity activity, final String gameId) {
		boolean testMode = false;
		boolean usePerPlacementLoad = false;
		initialize(activity, gameId, null, testMode, usePerPlacementLoad, null);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param initializationListener Listener for IUnityAdsInitializationListener callbacks
	 * @deprecated this method is deprecated in favor of {@link #initialize(Context, String, IUnityAdsInitializationListener)} use Application Context
	 */
	@Deprecated
	public static void initialize(final Activity activity, final String gameId, final IUnityAdsInitializationListener initializationListener) {
		boolean testMode = false;
		boolean usePerPlacementLoad = false;
		initialize(activity, gameId, null, testMode, usePerPlacementLoad, initializationListener);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 * @param testMode If true, only test ads are shown
	 * @deprecated this method is deprecated in favor of {@link #initialize(Context, String, boolean)} use {@link #addListener(IUnityAdsListener)} to add a listener and use Application Context
	 */
	@Deprecated
	public static void initialize(final Activity activity, final String gameId, final IUnityAdsListener listener, final boolean testMode) {
		boolean usePerPlacementLoad = false;
		initialize(activity, gameId, listener, testMode, usePerPlacementLoad, null);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param testMode If true, only test ads are shown
	 * @deprecated this method is deprecated in favor of {@link #initialize(Context, String, boolean)} use Application Context
	 */
	@Deprecated
	public static void initialize(final Activity activity, final String gameId, final boolean testMode) {
		boolean usePerPlacementLoad = false;
		initialize(activity, gameId, null, testMode, usePerPlacementLoad, null);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param testMode If true, only test ads are shown
	 * @param initializationListener Listener for IUnityAdsInitializationListener callbacks
	 * @deprecated this method is deprecated in favor of {@link #initialize(Context, String, boolean, IUnityAdsInitializationListener)} use Application context
	 */
	@Deprecated
	public static void initialize(final Activity activity, final String gameId, final boolean testMode, final IUnityAdsInitializationListener initializationListener) {
		boolean usePerPlacementLoad = false;
		initialize(activity, gameId, null, testMode, usePerPlacementLoad, initializationListener);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 * Note: The `load` API is in closed beta and available upon invite only. If you would like to be considered for the beta, please contact Unity Ads Support.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 * @param testMode If true, only test ads are shown
	 * @param enablePerPlacementLoad Set this flag to `YES` to disable automatic placement caching. When this is enabled, developer must call `load` on placements before calling show
	 * @deprecated this method is deprecated in favor of {@link #initialize(Context, String, boolean, boolean)} use {@link #addListener(IUnityAdsListener)} to add a listener and use Application Context
	 */
	@Deprecated
	public static void initialize(final Activity activity, final String gameId, final IUnityAdsListener listener, final boolean testMode, final boolean enablePerPlacementLoad) {
		initialize(activity, gameId, listener, testMode, enablePerPlacementLoad, null);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 * Note: The `load` API is in closed beta and available upon invite only. If you would like to be considered for the beta, please contact Unity Ads Support.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param testMode If true, only test ads are shown
	 * @param enablePerPlacementLoad Set this flag to `YES` to disable automatic placement caching. When this is enabled, developer must call `load` on placements before calling show
	 *  @deprecated this method is deprecated in favor of {@link #initialize(Context, String, boolean, boolean)} use Application Context
	 */
	@Deprecated
	public static void initialize(final Activity activity, final String gameId, final boolean testMode, final boolean enablePerPlacementLoad) {
		initialize(activity, gameId, null, testMode, enablePerPlacementLoad, null);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 * Note: The `load` API is in closed beta and available upon invite only. If you would like to be considered for the beta, please contact Unity Ads Support.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param testMode If true, only test ads are shown
	 * @param enablePerPlacementLoad Set this flag to `YES` to disable automatic placement caching. When this is enabled, developer must call `load` on placements before calling show
	 * @param initializationListener Listener for IUnityAdsInitializationListener callbacks
	 * @deprecated this method is deprecated in favor of {@link #initialize(Context, String, boolean, boolean, IUnityAdsInitializationListener)} use Application Context
	 */
	@Deprecated
	public static void initialize(final Activity activity, final String gameId, final boolean testMode, final boolean enablePerPlacementLoad, final IUnityAdsInitializationListener initializationListener) {
		initialize(activity, gameId, null, testMode, enablePerPlacementLoad, initializationListener);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param context Current Android context of calling app in favor of Application Context
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 */
	public static void initialize(final Context context, final String gameId) {
		boolean testMode = false;
		boolean usePerPlacementLoad = false;
		UnityAdsImplementation.initialize(context, gameId, null, testMode, usePerPlacementLoad, null);
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
		boolean usePerPlacementLoad = false;
		UnityAdsImplementation.initialize(context, gameId, null, testMode, usePerPlacementLoad, initializationListener);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param context Current Android context of calling app in favor of Application Context
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param testMode If true, only test ads are shown
	 */
	public static void initialize(final Context context, final String gameId, final boolean testMode) {
		boolean usePerPlacementLoad = false;
		UnityAdsImplementation.initialize(context, gameId, null, testMode, usePerPlacementLoad, null);
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
		boolean usePerPlacementLoad = false;
		UnityAdsImplementation.initialize(context, gameId, null, testMode, usePerPlacementLoad, initializationListener);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 * Note: The `load` API is in closed beta and available upon invite only. If you would like to be considered for the beta, please contact Unity Ads Support.
	 *
	 * @param context Current Android context of calling app in favor of Application Context
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param testMode If true, only test ads are shown
	 * @param enablePerPlacementLoad Set this flag to `YES` to disable automatic placement caching. When this is enabled, developer must call `load` on placements before calling show
	 */
	public static void initialize(final Context context, final String gameId, final boolean testMode, final boolean enablePerPlacementLoad) {
		UnityAdsImplementation.initialize(context, gameId, null, testMode, enablePerPlacementLoad, null);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 * Note: The `load` API is in closed beta and available upon invite only. If you would like to be considered for the beta, please contact Unity Ads Support.
	 *
	 * @param context Current Android context of calling app in favor of Application Context
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param testMode If true, only test ads are shown
	 * @param enablePerPlacementLoad Set this flag to `YES` to disable automatic placement caching. When this is enabled, developer must call `load` on placements before calling show
	 * @param initializationListener Listener for IUnityAdsInitializationListener callbacks
	 */
	public static void initialize(final Context context, final String gameId, final boolean testMode, final boolean enablePerPlacementLoad, final IUnityAdsInitializationListener initializationListener) {
		UnityAdsImplementation.initialize(context, gameId, null, testMode, enablePerPlacementLoad, initializationListener);
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
	 * Set listener for IUnityAdsListener callbacks. Use this if only ever using one listener.
	 * This overwrites the previous set listener.
	 *
	 * @param listener New listener for IUnityAdsListener callbacks
	 *
	 * @deprecated this method is deprecated in favor of {@link #addListener(IUnityAdsListener)}
	 */
	@Deprecated
	public static void setListener(IUnityAdsListener listener) {
		UnityAdsImplementation.setListener(listener);
	}

	/**
	 * Get current listener for IUnityAdsListener callbacks. Returns the most recent listener set through setListener
	 * or the listener from the first initialize
	 *
	 * @return Return IUnityAdsListener that was set from setListener
	 *
	 * @deprecated this method is deprecated in favor of {@link #addListener(IUnityAdsListener)} and {@link #removeListener(IUnityAdsListener)}
	 */
	@Deprecated
	public static IUnityAdsListener getListener() {
		return UnityAdsImplementation.getListener();
	}

	/**
	 * Add listener for IUnityAdsListener callbacks. Use this if subscribing multiple listeners.
	 * It is not recommended to mix setListener with addListener
	 *
	 * @param listener New listener for IUnityAdsListener callbacks
	 */
	public static void addListener(IUnityAdsListener listener) {
		UnityAdsImplementation.addListener(listener);
	}

	/**
	 * Remove listener for IUnityAdsListener callbacks
	 *
	 * @param listener New listener for IUnityAdsListener callbacks
	 */
	public static void removeListener(IUnityAdsListener listener) {
		UnityAdsImplementation.removeListener(listener);
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
	 * Check if default placement is ready to show ads
	 *
	 * @return If true, default placement is ready to show ads
	 */
	public static boolean isReady() {
		return UnityAdsImplementation.isReady();
	}

	/**
	 * Check if placement is ready to show ads
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @return If true, placement is ready to show ads
	 */
	public static boolean isReady(String placementId) {
		return UnityAdsImplementation.isReady(placementId);
	}

	/**
	 * Get current state of default placement
	 *
	 * @return If PlacementState.READY, default placement is ready to show ads. Other states give detailed reasons why placement is not ready.
	 */
	public static PlacementState getPlacementState() {
		return UnityAdsImplementation.getPlacementState();
	}

	/**
	 * Get current state of a placement
	 *
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @return If PlacementState.READY, placement is ready to show ads. Other states give detailed reasons why placement is not ready.
	 */
	public static PlacementState getPlacementState(String placementId) {
		return UnityAdsImplementation.getPlacementState(placementId);
	}

	/**
	 * Show one advertisement using default placement.
	 *
	 * @param activity Current Android activity of calling app
	 */
	public static void show(final Activity activity) {
		UnityAdsImplementation.show(activity);
	}

	/**
	 * Show one advertisement with custom placement.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 */
	public static void show(final Activity activity, final String placementId) {
		UnityAdsImplementation.show(activity, placementId);
	}

	/**
	 * Show one advertisement with custom placement and custom options.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @param options Custom options
	 */
	public static void show(final Activity activity, final String placementId, final UnityAdsShowOptions options) {
		UnityAdsImplementation.show(activity, placementId, options);
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
	 * Request fill for a specific placement ID. This functionality is enabled through the `enablePerPlacementLoad` in initialize.
	 * Note: The `load` API is in closed beta and available upon invite only. If you would like to be considered for the beta, please contact Unity Ads Support.
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
			public void onUnityAdsFailedToLoad(String placementId) {

			}
		});
	}

	/**
	 * Request fill for a specific placement ID. This functionality is enabled through the `enablePerPlacementLoad` in initialize.
	 * Note: The `load` API is in closed beta and available upon invite only. If you would like to be considered for the beta, please contact Unity Ads Support.
	 *
	 * @param placementId The placement ID to be loaded.
	 * @param listener The listener which is going to be notified about load request result.
	 */
	public static void load(final String placementId, final IUnityAdsLoadListener listener) {
		UnityAdsImplementation.load(placementId, new UnityAdsLoadOptions(), listener);
	}

	/**
	 * Request fill for a specific placement ID with custom options. This functionality is enabled through the `enablePerPlacementLoad` in initialize.
	 * Note: The `load` API is in closed beta and available upon invite only. If you would like to be considered for the beta, please contact Unity Ads Support
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

  /**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 * Note: The `load` API is in closed beta and available upon invite only. If you would like to be considered for the beta, please contact Unity Ads Support.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 * @param testMode If true, only test ads are shown
	 * @param enablePerPlacementLoad Set this flag to `YES` to disable automatic placement caching. When this is enabled, developer must call `load` on placements before calling show
	 * @param initializationListener Listener for IUnityAdsInitializationListener callbacks
	 */
	private static void initialize(final Activity activity, final String gameId, final IUnityAdsListener listener, final boolean testMode, final boolean enablePerPlacementLoad, final IUnityAdsInitializationListener initializationListener) {
		DeviceLog.entered();

		if (activity == null) {
			DeviceLog.error("Error while initializing Unity Ads: null activity, halting Unity Ads init");

			if (listener != null) {
				listener.onUnityAdsError(UnityAdsError.INITIALIZE_FAILED, "Error while initializing Unity Ads: null activity");
			}

			if (initializationListener != null) {
				initializationListener.onInitializationFailed(UnityAdsInitializationError.INVALID_ARGUMENT, "Error while initializing Unity Ads: null activity");
			}

			return;
		}

		UnityAdsImplementation.initialize(activity.getApplicationContext(), gameId, listener, testMode, enablePerPlacementLoad, initializationListener);
	}
}
