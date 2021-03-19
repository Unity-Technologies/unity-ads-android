package com.unity3d.services.ads;

import android.app.Activity;
import android.content.Context;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.ads.properties.AdsProperties;
import com.unity3d.services.IUnityServicesListener;
import com.unity3d.services.UnityServices;
import com.unity3d.services.ads.operation.load.LoadOperationState;
import com.unity3d.services.ads.operation.load.LoadModule;
import com.unity3d.services.ads.operation.show.ShowOperationState;
import com.unity3d.services.ads.operation.show.ShowModule;
import com.unity3d.services.ads.placement.Placement;
import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.WebViewBridgeInvoker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class UnityAdsImplementation {
	static ExecutorService _showExecutorService = Executors.newSingleThreadExecutor();
	private static Configuration configuration = null;
	private static WebViewBridgeInvoker webViewBridgeInvoker = new WebViewBridgeInvoker();

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
			handleLegacyListenerShowError("", com.unity3d.ads.UnityAds.UnityAdsError.NOT_INITIALIZED, "Unity Ads default placement is not initialized");
		}
	}

	/**
	 * Show one advertisement with custom placement.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 */
	public static void show(final Activity activity, final String placementId) {
		show(activity, placementId, new UnityAdsShowOptions(), null);
	}

	/**
	 * Show one advertisement with custom placement.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @param showListener Listener for IUnityAdsShowListener callbacks
	 */
	public static void show(final Activity activity, final String placementId, final IUnityAdsShowListener showListener) {
		show(activity, placementId, new UnityAdsShowOptions(), showListener);
	}

	/**
	 * Show one advertisement with custom placement and custom options.
	 *
	 * @param activity Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 * @param showOptions Custom options
	 * @param showListener Listener for IUnityAdsShowListener callbacks
	 */
	public static void show(final Activity activity, final String placementId, final UnityAdsShowOptions showOptions, final IUnityAdsShowListener showListener) {
		if (!isSupported()) {
			String showErrorMessage = "Unity Ads is not supported for this device";
			handleLegacyListenerShowError(placementId, UnityAds.UnityAdsError.NOT_INITIALIZED, showErrorMessage);
			handleShowError(showListener, placementId, UnityAds.UnityAdsShowError.NOT_INITIALIZED, showErrorMessage);
			return;
		}
		if(!isInitialized()) {
			String showErrorMessage = "Unity Ads is not initialized";
			handleLegacyListenerShowError(placementId, UnityAds.UnityAdsError.NOT_INITIALIZED, showErrorMessage);
			handleShowError(showListener, placementId, UnityAds.UnityAdsShowError.NOT_INITIALIZED, showErrorMessage);
			return;
		}
		if(activity == null) {
			String showErrorMessage = "Activity must not be null";
			handleLegacyListenerShowError(placementId, UnityAds.UnityAdsError.INVALID_ARGUMENT, showErrorMessage);
			handleShowError(showListener, placementId, UnityAds.UnityAdsShowError.INVALID_ARGUMENT, showErrorMessage);
			return;
		}
		Configuration config = configuration == null ? new Configuration() : configuration;
		ClientProperties.setActivity(activity);
		ShowModule.getInstance().executeAdOperation(WebViewApp.getCurrentApp(), new ShowOperationState(placementId, showListener, activity, showOptions, config));
	}

	private static void handleShowError(IUnityAdsShowListener showListener, String placementId, UnityAds.UnityAdsShowError error, String message) {
		if (showListener == null) return;
		showListener.onUnityAdsShowFailure(placementId, error, message);
	}

	private static void handleLegacyListenerShowError(final String placementId, final UnityAds.UnityAdsError error, final String message) {
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

	public static void load(final String placementId, final UnityAdsLoadOptions loadOptions, final IUnityAdsLoadListener listener) {
		Configuration config = configuration == null ? new Configuration() : configuration;
		LoadModule.getInstance().executeAdOperation(webViewBridgeInvoker, new LoadOperationState(placementId, listener, loadOptions, config));
	}

	public static String getToken() {
		return TokenStorage.getToken();
	}

	public static void setConfiguration(Configuration configuration) {
		UnityAdsImplementation.configuration = configuration;
	}
}
