package com.unity3d.services.ar.configuration;

import com.unity3d.services.ads.configuration.IAdsModuleConfiguration;
import com.unity3d.services.ar.ARCheck;
import com.unity3d.services.ar.ARUtils;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.properties.ClientProperties;

import java.util.HashMap;
import java.util.Map;

public class ARModuleConfiguration implements IAdsModuleConfiguration {
	@Override
	public Class[] getWebAppApiClassList() {
		Class[] list = {
				com.unity3d.services.ar.api.AR.class
		};

		return list;
	}

	@Override
	public boolean resetState(Configuration configuration) {
		return true;
	}

	@Override
	public boolean initModuleState(Configuration configuration) {
		// ARCore caches the value so when we actually need the value it's probably not transient
		// anymore.
		if (ARCheck.isFrameworkPresent()) {
			ARUtils.isSupported(ClientProperties.getApplicationContext());
		}

		return true;
	}

	@Override
	public boolean initErrorState(Configuration configuration, String state, String message) {
		return true;
	}

	@Override
	public boolean initCompleteState(Configuration configuration) {
		return true;
	}

	public Map<String, Class> getAdUnitViewHandlers() {
		Map<String, Class> handlers = new HashMap<>();
		handlers.put("arview", com.unity3d.services.ar.view.ARViewHandler.class);

		return handlers;
	}
}
