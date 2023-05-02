package com.unity3d.services.ads.adunit;

import com.unity3d.services.ads.configuration.IAdsModuleConfiguration;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.IModuleConfiguration;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.webview.WebViewApp;

import java.util.Map;

public class AdUnitViewHandlerFactory implements IAdUnitViewHandlerFactory {
	@Override
	public IAdUnitViewHandler createViewHandler(String name) {
		if (WebViewApp.getCurrentApp() != null) {
			Configuration configuration = WebViewApp.getCurrentApp().getConfiguration();
			Class[] list = configuration.getModuleConfigurationList();

			for (Class moduleClass : list) {
				IModuleConfiguration moduleConfig = configuration.getModuleConfiguration(moduleClass);
				if (moduleConfig instanceof IAdsModuleConfiguration) {
					Map<String, Class> adUnitViewHandlers = ((IAdsModuleConfiguration) moduleConfig).getAdUnitViewHandlers();
					if (adUnitViewHandlers != null && adUnitViewHandlers.containsKey(name)) {
						IAdUnitViewHandler viewHandler = null;
						try {
							viewHandler = (IAdUnitViewHandler)adUnitViewHandlers.get(name).newInstance();
						}
						catch (Exception e) {
							DeviceLog.error("Error creating view: " + name);
						}

						return viewHandler;
					}
				}
			}
		}

		return null;
	}
}
