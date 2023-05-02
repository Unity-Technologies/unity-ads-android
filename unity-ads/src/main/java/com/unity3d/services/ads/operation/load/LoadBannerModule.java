package com.unity3d.services.ads.operation.load;

import com.unity3d.services.banners.BannerViewCache;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.bridge.BannerBridge;
import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;
import com.unity3d.services.core.request.metrics.SDKMetrics;

import org.json.JSONException;
import org.json.JSONObject;

public class LoadBannerModule extends BaseLoadModule {

	static ILoadModule _instance;

	public static ILoadModule getInstance() {
		if (_instance == null) {
			LoadBannerModule loadModule = new LoadBannerModule(Utilities.getService(SDKMetricsSender.class));
			LoadModuleDecoratorInitializationBuffer bufferedLoadModule = new LoadModuleDecoratorInitializationBuffer(loadModule, InitializationNotificationCenter.getInstance());
			LoadModuleDecoratorTimeout timedLoadModule = new LoadModuleDecoratorTimeout(bufferedLoadModule, new ConfigurationReader());
			_instance = timedLoadModule;
		}
		return _instance;
	}

	public LoadBannerModule(SDKMetricsSender sdkMetrics) {
		super(sdkMetrics);
	}

	@Override
	protected void addOptionalParameters(LoadOperationState state, JSONObject parameters) throws JSONException {
		if (state instanceof LoadBannerOperationState) {
			parameters.put("width", ((LoadBannerOperationState) state).getSize().getWidth());
			parameters.put("height", ((LoadBannerOperationState) state).getSize().getHeight());
		}
	}

	@Override
	public void onUnityAdsAdLoaded(String operationId) {
		final ILoadOperation loadOperation = get(operationId);
		if (loadOperation == null || loadOperation.getLoadOperationState() == null) return;
		final LoadOperationState state = loadOperation.getLoadOperationState();
		if (state instanceof LoadBannerOperationState) {
			String bannerAdId = state.getId();
			UnityBannerSize size = ((LoadBannerOperationState) state).getSize();
			boolean successfullyLoaded = BannerViewCache.getInstance().loadWebPlayer(bannerAdId, size);
			if (successfullyLoaded) {
				BannerBridge.didLoad(operationId);
			}
			super.onUnityAdsAdLoaded(operationId);
		}
	}
}
