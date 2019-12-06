package com.unity3d.services.core.configuration;

import com.unity3d.services.core.broadcast.BroadcastMonitor;
import com.unity3d.services.core.cache.CacheThread;
import com.unity3d.services.core.connectivity.ConnectivityMonitor;
import com.unity3d.services.core.device.AdvertisingId;
import com.unity3d.services.core.device.StorageManager;
import com.unity3d.services.core.device.VolumeChange;
import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.request.WebRequestThread;

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

		BroadcastMonitor.removeAllBroadcastListeners();
		CacheThread.cancel();
		WebRequestThread.cancel();
		ConnectivityMonitor.stopAll();

		StorageManager.init(ClientProperties.getApplicationContext());
		AdvertisingId.init(ClientProperties.getApplicationContext());
		VolumeChange.clearAllListeners();

		return true;
	}

	public boolean initModuleState(Configuration configuration) {
		return true;
	}

	public boolean initErrorState(Configuration configuration, String state, String errorMessage) {
		return true;
	}

	public boolean initCompleteState(Configuration configuration) {
		return true;
	}
}
