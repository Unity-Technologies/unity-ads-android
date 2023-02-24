package com.unity3d.services.core.configuration;

import com.unity3d.ads.UnityAds;
import com.unity3d.services.core.broadcast.BroadcastMonitor;
import com.unity3d.services.core.cache.CacheThread;
import com.unity3d.services.core.connectivity.ConnectivityMonitor;
import com.unity3d.services.core.device.AdvertisingId;
import com.unity3d.services.core.device.Device;
import com.unity3d.services.core.device.OpenAdvertisingId;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.device.VolumeChange;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.request.WebRequestThread;
import com.unity3d.services.core.request.metrics.Metric;
import com.unity3d.services.core.request.metrics.SDKMetrics;

import java.util.ArrayList;
import java.util.List;

public class CoreModuleConfiguration implements IModuleConfiguration {
	public Class[] getWebAppApiClassList() {
		Class[] list = {
				com.unity3d.services.core.api.Broadcast.class,
				com.unity3d.services.core.api.Cache.class,
				com.unity3d.services.core.api.Connectivity.class,
				com.unity3d.services.core.api.DeviceInfo.class,
				com.unity3d.services.core.api.ClassDetection.class,
				com.unity3d.services.core.api.Storage.class,
				com.unity3d.services.core.api.Sdk.class,
				com.unity3d.services.core.api.Request.class,
				com.unity3d.services.core.api.Resolve.class,
				com.unity3d.services.core.api.Intent.class,
				com.unity3d.services.core.api.Lifecycle.class,
				com.unity3d.services.core.api.Preferences.class,
				com.unity3d.services.core.api.SensorInfo.class,
				com.unity3d.services.core.api.Permissions.class
		};

		return list;
	}

	public boolean resetState(Configuration configuration) {
		BroadcastMonitor.getInstance().removeAllBroadcastListeners();
		CacheThread.cancel();
		WebRequestThread.cancel();
		ConnectivityMonitor.stopAll();

		StorageManager.init(ClientProperties.getApplicationContext());
		AdvertisingId.init(ClientProperties.getApplicationContext());
		OpenAdvertisingId.init(ClientProperties.getApplicationContext());
		VolumeChange.clearAllListeners();

		return true;
	}

	public boolean initModuleState(Configuration configuration) {
		return true;
	}

	public boolean initErrorState(Configuration configuration, ErrorState state, String errorMessage) {
		SDKMetrics.setConfiguration(configuration);
		final String message;
		final UnityAds.UnityAdsInitializationError error;
		switch (state) {
			case CreateWebApp:
				message = errorMessage;
				error = UnityAds.UnityAdsInitializationError.INTERNAL_ERROR;
				break;
			case InitModules:
				message = errorMessage;
				error = UnityAds.UnityAdsInitializationError.AD_BLOCKER_DETECTED;
				break;
			default:
				message = "Unity Ads failed to initialize due to internal error";
				error = UnityAds.UnityAdsInitializationError.INTERNAL_ERROR;
		}
    
		InitializationNotificationCenter.getInstance().triggerOnSdkInitializationFailed(message, state, 0);

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				SdkProperties.notifyInitializationFailed(error, message);
			}
		});
		return true;
	}

	public boolean initCompleteState(Configuration configuration) {
		SDKMetrics.setConfiguration(configuration);
		InitializationNotificationCenter.getInstance().triggerOnSdkInitialized();

		Utilities.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				SdkProperties.notifyInitializationComplete();
			}
		});
		collectMetrics();
		return true;
	}

	private void collectMetrics() {
		List<Metric> metrics = new ArrayList<>();
		int hasX264 = Device.hasX264Decoder() ? 1 : 0;
		int hasX265 = Device.hasX265Decoder() ? 1 : 0;
		metrics.add(new Metric("native_device_decoder_x264", hasX264, null));
		metrics.add(new Metric("native_device_decoder_x265", hasX265, null));
		SDKMetrics.getInstance().sendMetrics(metrics);
	}
}
