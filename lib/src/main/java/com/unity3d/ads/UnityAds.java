package com.unity3d.ads;

import android.app.Activity;

import com.unity3d.services.UnityServices;
import com.unity3d.services.ads.UnityAdsImplementation;

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
		UnityAdsImplementation.initialize(activity, gameId, listener, testMode);
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
	 * Set listener for IUnityAdsListener callbacks. Use this if only ever using one listener
	 *
	 * @param listener New listener for IUnityAdsListener callbacks
	 *
	 * @deprecated this method is deprecated in favor of {@link #addListener(IUnityAdsListener)}
	 */
	public static void setListener(IUnityAdsListener listener) {
		UnityAdsImplementation.setListener(listener);
	}

	/**
	 * Add listener for IUnityAdsListener callbacks. Use this if subscribing multiple listeners
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
	 * Get current listener for IUnityAdsListener callbacks. Returns the first listener added
	 *
	 * @return First listener set for IUnityAdsListener callbacks
	 *
	 * @deprecated this method is deprecated in favor of {@link #addListener(IUnityAdsListener)} and {@link #removeListener(IUnityAdsListener)}
	 */
	public static IUnityAdsListener getListener() {
		return UnityAdsImplementation.getListener();
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
}
