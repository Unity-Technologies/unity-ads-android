package com.unity3d.services.ads;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.ads.properties.AdsProperties;
import com.unity3d.services.IUnityServicesListener;
import com.unity3d.services.UnityServices;
import com.unity3d.services.ads.adunit.AdUnitOpen;
import com.unity3d.services.ads.load.LoadModule;
import com.unity3d.services.ads.placement.Placement;
import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class UnityAdsImplementation {

	static ExecutorService _showExecutorService = Executors.newSingleThreadExecutor();

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param context Current Android application context of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 */
	public static void initialize(final Context context, final String gameId, final IUnityAdsListener listener) {
		boolean testMode = false;
		initialize(context, gameId, listener, testMode);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param context Current Android application context of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 * @param testMode If true, only test ads are shown
	 */
	public static void initialize(final Context context, final String gameId, final IUnityAdsListener listener, final boolean testMode) {
		boolean usePerPlacementLoad = false;
		initialize(context, gameId, listener, testMode, usePerPlacementLoad, null);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *  @param context Current Android application context of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 * @param testMode If true, only test ads are shown
	 * @param enablePerPlacementLoad Set this flag to `YES` to disable automatic placement caching. When this is enabled, developer must call `load` on placements before calling show
	 * @param initializationListener Listener for IUnityAdsInitializationListener callbacks
	 */
	public static void initialize(final Context context, final String gameId, final IUnityAdsListener listener, final boolean testMode, final boolean enablePerPlacementLoad, final IUnityAdsInitializationListener initializationListener) {
		DeviceLog.entered();

		UnityAdsImplementation.addListener(listener);

		UnityServices.initialize(context, gameId, new IUnityServicesListener() {
			@Override
			public void onUnityServicesError(UnityServices.UnityServicesError error, String message) {
				if (listener == null) {
					return;
				}
				if (error == UnityServices.UnityServicesError.INIT_SANITY_CHECK_FAIL) {
					listener.onUnityAdsError(UnityAds.UnityAdsError.INIT_SANITY_CHECK_FAIL, message);
				}
				else if (error == UnityServices.UnityServicesError.INVALID_ARGUMENT) {
					listener.onUnityAdsError(UnityAds.UnityAdsError.INVALID_ARGUMENT, message);
				}
			}
		}, testMode, enablePerPlacementLoad, initializationListener);
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
	 * Set listener for IUnityAdsListener callbacks
	 *
	 * @param listener New listener for IUnityAdsListener callbacks
	 */
	@Deprecated
	public static void setListener(IUnityAdsListener listener) {
		AdsProperties.setListener(listener);
	}

	/**
	 * Get current listener for IUnityAdsListener callbacks. Returns the most recent listener set through setListener
	 * or the listener from the first initialize
	 *
	 * @return Return IUnityAdsListener that was set from setListener
	 */
	@Deprecated
	public static IUnityAdsListener getListener() {
		return AdsProperties.getListener();
	}

	/**
	 * Add listener for IUnityAdsListener callbacks
	 *
	 * @param listener New listener for IUnityAdsListener callbacks
	 */
	public static void addListener(IUnityAdsListener listener) {
		AdsProperties.addListener(listener);
	}

	/**
	 * Remove listener for IUnityAdsListener callbacks
	 *
	 * @param listener New listener for IUnityAdsListener callbacks
	 */
	public static void removeListener(IUnityAdsListener listener) {
		AdsProperties.removeListener(listener);
	}

	/**
	 * Checks if current device supports running Unity Ads
	 *
	 * @return If true, device supports Unity Ads. If false, device can't initialize or show Unity Ads.
	 */
	public static boolean isSupported() {
		return UnityServices.isSupported();
	}

	/**
	 * Get current SDK version
	 *
	 * @return Current SDK version name
	 */
	public static String getVersion() {
		return UnityServices.getVersion();
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
	public static UnityAds.PlacementState getPlacementState() {
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
	public static UnityAds.PlacementState getPlacementState(String placementId) {
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
			handleShowError("", com.unity3d.ads.UnityAds.UnityAdsError.NOT_INITIALIZED, "Unity Ads default placement is not initialized");
		}
	}

	/**
	 * Show one advertisement with custom placement.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 */
	public static void show(final Activity activity, final String placementId) {
		show(activity, placementId, new UnityAdsShowOptions());
	}

	/**
	 * Show one advertisement with custom placement and custom options.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @param showOptions Custom options.
	 */
	public static void show(final Activity activity, final String placementId, final UnityAdsShowOptions showOptions) {
		if(activity == null) {
			handleShowError(placementId, UnityAds.UnityAdsError.INVALID_ARGUMENT, "Activity must not be null");
			return;
		}

		if(isReady(placementId)) {
			DeviceLog.info("Unity Ads opening new ad unit for placement " + placementId);
			ClientProperties.setActivity(activity);
			_showExecutorService.submit(new Runnable() {
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
						options.put("options", showOptions.getData());
					} catch(JSONException e) {
						DeviceLog.exception("JSON error while constructing show options", e);
					}

					try {
						if(!AdUnitOpen.open(placementId, options)) {
							handleShowError(placementId, UnityAds.UnityAdsError.INTERNAL_ERROR, "Webapp timeout, shutting down Unity Ads");
						}
					}
					catch (NoSuchMethodException exception) {
						DeviceLog.exception("Could not get callback method", exception);
						handleShowError(placementId, UnityAds.UnityAdsError.SHOW_ERROR, "Could not get com.unity3d.ads.properties.showCallback method");
					}
				}
			});
		} else {
			if (!isSupported()) {
				handleShowError(placementId, UnityAds.UnityAdsError.NOT_INITIALIZED, "Unity Ads is not supported for this device");
			} else if(!isInitialized()) {
				handleShowError(placementId, UnityAds.UnityAdsError.NOT_INITIALIZED, "Unity Ads is not initialized");
			} else {
				handleShowError(placementId, UnityAds.UnityAdsError.SHOW_ERROR, "Placement \"" + placementId + "\" is not ready");
			}
		}
	}

	private static void handleShowError(final String placementId, final UnityAds.UnityAdsError error, final String message) {
		final String errorMessage = "Unity Ads show failed: " + message;
		DeviceLog.error(errorMessage);

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				for (IUnityAdsListener listener : AdsProperties.getListeners()) {
					listener.onUnityAdsError(error, errorMessage);
					if (placementId != null) {
						listener.onUnityAdsFinish(placementId, UnityAds.FinishState.ERROR);
					} else {
						listener.onUnityAdsFinish("", UnityAds.FinishState.ERROR);
					}
				}
			}
		});
	}

	/**
	 * Toggles debug mode on/off
	 *
	 * @param debugMode If true, debug mode is on and there will be lots of debug output from Unity Ads. If false, there will be only some short log messages from Unity Ads.
	 */
	public static void setDebugMode(boolean debugMode) {
		UnityServices.setDebugMode(debugMode);
	}

	/**
	 * Get current debug mode status
	 *
	 * @return If true, debug mode is on. If false, debug mode is off.
	 */
	public static boolean getDebugMode() {
		return UnityServices.getDebugMode();
	}

	public static String getDefaultPlacement() {
		return Placement.getDefaultPlacement();
	}

	public static void load(final String placementId, final UnityAdsLoadOptions loadOptions, final IUnityAdsLoadListener listener) {
		LoadModule.getInstance().load(placementId, loadOptions, listener);
	}

	public static String getToken() {
		return TokenStorage.getToken();
	}
}
