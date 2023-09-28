package com.unity3d.services.ads.operation.load;

import com.unity3d.services.core.configuration.ExperimentsReader;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;

import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.request.metrics.SDKMetricsSender;

import org.json.JSONObject;

public class LoadModule extends BaseLoadModule {

	static ILoadModule _instance;

	public static ILoadModule getInstance() {
		if (_instance == null) {
			LoadModule loadModule = new LoadModule(Utilities.getService(SDKMetricsSender.class));
			LoadModuleDecoratorInitializationBuffer bufferedLoadModule = new LoadModuleDecoratorInitializationBuffer(loadModule, InitializationNotificationCenter.getInstance());
			LoadModuleDecoratorTimeout timedLoadModule = new LoadModuleDecoratorTimeout(bufferedLoadModule, new ExperimentsReader());
			_instance = timedLoadModule;
		}
		return _instance;
	}

	public LoadModule(SDKMetricsSender sdkMetrics) {
		super(sdkMetrics);
	}

	@Override
	void addOptionalParameters(LoadOperationState state, JSONObject parameters) {
		// none needed
	}

}
