package com.unity3d.services.ads.operation.load;

import com.unity3d.services.core.configuration.ExperimentsReader;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;

import org.json.JSONException;
import org.json.JSONObject;

public class LoadBannerModule extends BaseLoadModule {

	static ILoadModule _instance;

	public static ILoadModule getInstance() {
		if (_instance == null) {
			LoadBannerModule loadModule = new LoadBannerModule(Utilities.getService(SDKMetricsSender.class));
			LoadModuleDecoratorInitializationBuffer bufferedLoadModule = new LoadModuleDecoratorInitializationBuffer(loadModule, InitializationNotificationCenter.getInstance());
			LoadModuleDecoratorTimeout timedLoadModule = new LoadModuleDecoratorTimeout(bufferedLoadModule, new ExperimentsReader());
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
}
