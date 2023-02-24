package com.unity3d.services.ads;

import android.app.Activity;
import android.content.Context;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.IUnityAdsTokenListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.services.UnityServices;
import com.unity3d.services.ads.gmascar.managers.BiddingBaseManager;
import com.unity3d.services.ads.gmascar.managers.BiddingManagerFactory;
import com.unity3d.services.ads.operation.load.LoadModule;
import com.unity3d.services.ads.operation.load.LoadOperationState;
import com.unity3d.services.ads.operation.show.ShowModule;
import com.unity3d.services.ads.operation.show.ShowOperationState;
import com.unity3d.services.ads.token.AsyncTokenStorage;
import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.IExperiments;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.request.metrics.AdOperationMetric;
import com.unity3d.services.core.request.metrics.SDKMetrics;
import com.unity3d.services.core.webview.WebViewApp;
import com.unity3d.services.core.webview.bridge.WebViewBridgeInvoker;

public final class UnityAdsImplementation implements IUnityAds {
	private static Configuration configuration = null;
	private static WebViewBridgeInvoker webViewBridgeInvoker = new WebViewBridgeInvoker();
	private static IUnityAds instance;

	public static IUnityAds getInstance() {
		if (instance == null) {
			instance = new UnityAdsImplementation();
		}
		return instance;
	}

	/**
	 * Initializes Unity Ads. Unity Ads should be initialized when app starts.
	 *
	 *  @param context Current Android application context of calling app
	 * @param gameId Unique identifier for a game, given by Unity Ads admin tools or Unity editor
	 * @param testMode If true, only test ads are shown
	 * @param initializationListener Listener for IUnityAdsInitializationListener callbacks
	 */
	@Override
	public void initialize(final Context context, final String gameId, final boolean testMode, final IUnityAdsInitializationListener initializationListener) {
		DeviceLog.entered();

		UnityServices.initialize(context, gameId, testMode, initializationListener);
	}

	/**
	 * Checks if Unity Ads has been initialized. This might be useful for debugging initialization problems.
	 *
	 * @return If true, Unity Ads has been successfully initialized
	 */
	@Override
	public boolean isInitialized() {
		return UnityServices.isInitialized();
	}

	/**
	 * Checks if current device supports running Unity Ads
	 *
	 * @return If true, device supports Unity Ads. If false, device can't initialize or show Unity Ads.
	 */
	@Override
	public boolean isSupported() {
		return UnityServices.isSupported();
	}

	/**
	 * Get current SDK version
	 *
	 * @return Current SDK version name
	 */
	@Override
	public String getVersion() {
		return UnityServices.getVersion();
	}

	/**
	 * Show one advertisement with custom placement.
	 *
	 * @param activity    Current Android activity of calling app
	 * @param placementId Placement, as defined in Unity Ads admin tools
	 */
	public void show(final Activity activity, final String placementId) {
		show(activity, placementId, new UnityAdsShowOptions(), null);
	}

	/**
	 * Show one advertisement with custom placement.
	 *
	 * @param activity     Current Android activity of calling app
	 * @param placementId  Placement, as defined in Unity Ads admin tools
	 * @param showListener Listener for IUnityAdsShowListener callbacks
	 */
	public void show(final Activity activity, final String placementId, final IUnityAdsShowListener showListener) {
		show(activity, placementId, new UnityAdsShowOptions(), showListener);
	}

	/**
	 * Show one advertisement with custom placement and custom options.
	 *
	 * @param activity     Current Android activity of calling app
	 * @param placementId  Placement, as defined in Unity Ads admin tools
	 * @param showOptions  Custom options
	 * @param showListener Listener for IUnityAdsShowListener callbacks
	 */
	@Override
	public void show(final Activity activity, final String placementId, final UnityAdsShowOptions showOptions, final IUnityAdsShowListener showListener) {
		if (!isSupported()) {
			String showErrorMessage = "Unity Ads is not supported for this device";
			handleShowError(showListener, placementId, UnityAds.UnityAdsShowError.NOT_INITIALIZED, showErrorMessage);
			return;
		}
		if (!isInitialized()) {
			String showErrorMessage = "Unity Ads is not initialized";
			handleShowError(showListener, placementId, UnityAds.UnityAdsShowError.NOT_INITIALIZED, showErrorMessage);
			return;
		}
		if (activity == null) {
			String showErrorMessage = "Activity must not be null";
			handleShowError(showListener, placementId, UnityAds.UnityAdsShowError.INVALID_ARGUMENT, showErrorMessage);
			return;
		}
		Configuration config = configuration == null ? new Configuration() : configuration;
		ClientProperties.setActivity(activity);
		ShowModule.getInstance().executeAdOperation(WebViewApp.getCurrentApp(), new ShowOperationState(placementId, showListener, activity, showOptions, config));
	}

	private void handleShowError(IUnityAdsShowListener showListener, String placementId, UnityAds.UnityAdsShowError error, String message) {
		SDKMetrics.getInstance().sendMetricWithInitState(AdOperationMetric.newAdShowFailure(error, 0L));
		if (showListener == null) return;
		showListener.onUnityAdsShowFailure(placementId, error, message);
	}

	/**
	 * Toggles debug mode on/off
	 *
	 * @param debugMode If true, debug mode is on and there will be lots of debug output from Unity Ads. If false, there will be only some short log messages from Unity Ads.
	 */
	@Override
	public void setDebugMode(boolean debugMode) {
		UnityServices.setDebugMode(debugMode);
	}

	/**
	 * Get current debug mode status
	 *
	 * @return If true, debug mode is on. If false, debug mode is off.
	 */
	@Override
	public boolean getDebugMode() {
		return UnityServices.getDebugMode();
	}

	@Override
	public void load(final String placementId, final UnityAdsLoadOptions loadOptions, final IUnityAdsLoadListener listener) {
		Configuration config = configuration == null ? new Configuration() : configuration;
		LoadModule.getInstance().executeAdOperation(webViewBridgeInvoker, new LoadOperationState(placementId, listener, loadOptions, config));
	}

	@Override
	public String getToken() {
		// Getting the available token from storage
		final String token =  TokenStorage.getInstance().getToken();
		if (token == null || token.isEmpty()) {
			return null;
		}

		Configuration config = configuration == null ? new ConfigurationReader().getCurrentConfiguration() : configuration;
		BiddingBaseManager manager = BiddingManagerFactory.getInstance().createManager(null, config.getExperiments());
		manager.start();
		return manager.getFormattedToken(token);
	}

	@Override
	public void getToken(final IUnityAdsTokenListener listener) {
		if (listener == null) {
			// Invalidating listener
			DeviceLog.info("Please provide non-null listener to UnityAds.GetToken method");
			return;
		} else if (ClientProperties.getApplicationContext() == null) {
			// Invalidating app Context.
			listener.onUnityAdsTokenReady(null);
			return;
		}

		Configuration config = configuration == null ? new ConfigurationReader().getCurrentConfiguration() : configuration;
		BiddingBaseManager manager = BiddingManagerFactory.getInstance().createManager(listener, config.getExperiments());
		manager.start();
		AsyncTokenStorage.getInstance().getToken(manager);
	}

	public static void setConfiguration(Configuration configuration) {
		UnityAdsImplementation.configuration = configuration;
	}
}
