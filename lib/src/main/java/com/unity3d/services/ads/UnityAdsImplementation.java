package com.unity3d.services.ads;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.IUnityServicesListener;
import com.unity3d.services.UnityServices;
import com.unity3d.services.ads.adunit.AdUnitOpen;
import com.unity3d.services.ads.load.LoadModule;
import com.unity3d.services.ads.placement.Placement;
import com.unity3d.services.ads.properties.AdsProperties;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public final class UnityAdsImplementation {

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 * @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 */
	public static void initialize(final Activity activity, final String gameId, final IUnityAdsListener listener) {
		boolean testMode = false;
		initialize(activity, gameId, listener, testMode);
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
		boolean usePerPlacementLoad = false;
		initialize(activity, gameId, listener, testMode, usePerPlacementLoad);
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *  @param activity Current Android activity of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param listener Listener for IUnityAdsListener callbacks
	 * @param testMode If true, only test ads are shown
	 * @param enablePerPlacementLoad Set this flag to `YES` to disable automatic placement caching. When this is enabled, developer must call `load` on placements before calling show
	 */
	public static void initialize(final Activity activity, final String gameId, final IUnityAdsListener listener, final boolean testMode, final boolean enablePerPlacementLoad) {
		DeviceLog.entered();
		
		UnityAdsImplementation.addListener(listener);

		UnityServices.initialize(activity, gameId, new IUnityServicesListener() {
			@Override
			public void onUnityServicesError(UnityServices.UnityServicesError error, String message) {
				if (error == UnityServices.UnityServicesError.INIT_SANITY_CHECK_FAIL) {
					listener.onUnityAdsError(UnityAds.UnityAdsError.INIT_SANITY_CHECK_FAIL, message);
				}
				else if (error == UnityServices.UnityServicesError.INVALID_ARGUMENT) {
					listener.onUnityAdsError(UnityAds.UnityAdsError.INVALID_ARGUMENT, message);
				}
			}
		}, testMode, enablePerPlacementLoad);
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
		AdsProperties.addListener(listener);
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
	 * Get the first listener added for IUnityAdsListener callbacks
	 *
	 * @return First listener added for IUnityAdsListener callbacks
	 */
	@Deprecated
	public static IUnityAdsListener getListener() {
		// For now, return the first listener registered
		Iterator<IUnityAdsListener> it = AdsProperties.getListeners().iterator();
		if (it.hasNext()) {
			return it.next();
		} else {
			return null;
		}
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
		if(activity == null) {
			handleShowError(placementId, UnityAds.UnityAdsError.INVALID_ARGUMENT, "Activity must not be null");
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
							handleShowError(placementId, UnityAds.UnityAdsError.INTERNAL_ERROR, "Webapp timeout, shutting down Unity Ads");
						}
					}
					catch (NoSuchMethodException exception) {
						DeviceLog.exception("Could not get callback method", exception);
						handleShowError(placementId, UnityAds.UnityAdsError.SHOW_ERROR, "Could not get com.unity3d.ads.properties.showCallback method");
					}
				}
			}).start();
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

	public static void load(final String placementId) {
		LoadModule.getInstance().load(placementId);
	}
}
