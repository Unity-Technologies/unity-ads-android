package com.unity3d.services.ads.operation.load;

import com.unity3d.services.core.configuration.ConfigurationReader;
import com.unity3d.services.core.configuration.InitializationNotificationCenter;

import com.unity3d.services.core.request.metrics.ISDKMetrics;
import com.unity3d.services.core.request.metrics.SDKMetrics;

import org.json.JSONException;
import org.json.JSONObject;

public class LoadModule extends BaseLoadModule {

	static ILoadModule _instance;

	public static ILoadModule getInstance() {
		if (_instance == null) {
			LoadModule loadModule = new LoadModule(SDKMetrics.getInstance());
			LoadModuleDecoratorInitializationBuffer bufferedLoadModule = new LoadModuleDecoratorInitializationBuffer(loadModule, InitializationNotificationCenter.getInstance());
			LoadModuleDecoratorTimeout timedLoadModule = new LoadModuleDecoratorTimeout(bufferedLoadModule, new ConfigurationReader());
			_instance = timedLoadModule;
		}
		return _instance;
	}

	public LoadModule(ISDKMetrics sdkMetrics) {
		super(sdkMetrics);
	}

	@Override
	void addOptionalParameters(LoadOperationState state, JSONObject parameters) throws JSONException {
		// none needed
	}

}
