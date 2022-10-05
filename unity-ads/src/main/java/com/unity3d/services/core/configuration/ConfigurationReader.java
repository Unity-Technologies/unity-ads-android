package com.unity3d.services.core.configuration;

import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.properties.SdkProperties;
import com.unity3d.services.core.webview.WebViewApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class ConfigurationReader {
	private Configuration _localConfiguration;

	public Configuration getCurrentConfiguration() {
		if (getRemoteConfiguration() != null) {
			return getRemoteConfiguration();
		}
		Configuration localConfig = getLocalConfiguration();

		return localConfig != null ? localConfig : new Configuration();
	}

	private Configuration getRemoteConfiguration() {
		if (WebViewApp.getCurrentApp() == null) return null;
		return WebViewApp.getCurrentApp().getConfiguration();
	}

	private Configuration getLocalConfiguration() {
		if (_localConfiguration != null) {
			return _localConfiguration;
		}

		File configFile = new File(SdkProperties.getLocalConfigurationFilepath());
		if (configFile.exists()) {
			try {
				String fileContent = new String(Utilities.readFileBytes(configFile));
				JSONObject loadedJson = new JSONObject(fileContent);
				_localConfiguration = new Configuration(loadedJson);
			} catch (IOException | JSONException exception) {
				DeviceLog.debug("Unable to read configuration from storage");
				_localConfiguration = null;
			}
		}

		return _localConfiguration;

	}
}
