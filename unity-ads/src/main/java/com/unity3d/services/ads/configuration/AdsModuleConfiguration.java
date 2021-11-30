package com.unity3d.services.ads.configuration;

import android.os.ConditionVariable;

import com.unity3d.services.ads.UnityAdsImplementation;
import com.unity3d.services.ads.adunit.AdUnitOpen;
import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.log.DeviceLog;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AdsModuleConfiguration implements IAdsModuleConfiguration {
	private InetAddress _address;

	public Class[] getWebAppApiClassList() {
		Class[] list = {
			com.unity3d.services.ads.api.AdUnit.class,
			com.unity3d.services.ads.api.VideoPlayer.class,
			com.unity3d.services.ads.api.WebPlayer.class,
			com.unity3d.services.ads.api.Load.class,
			com.unity3d.services.ads.api.Show.class,
			com.unity3d.services.ads.api.Token.class,
			com.unity3d.services.ads.api.GMAScar.class
		};

		return list;
	}

	public boolean resetState(Configuration configuration) {
		AdUnitOpen.setConfiguration(configuration);
		UnityAdsImplementation.setConfiguration(configuration);
		TokenStorage.deleteTokens();
		return true;
	}

	public boolean initModuleState(Configuration configuration) {
		DeviceLog.debug("Unity Ads init: checking for ad blockers");

		final String configHost;
		try {
			configHost = new URL(configuration.getConfigUrl()).getHost();
		} catch(MalformedURLException e) {
			return true;
		}

		final ConditionVariable cv = new ConditionVariable();

		new Thread() {
			@Override
			public void run() {
				try {
					_address = InetAddress.getByName(configHost);
					cv.open();
				} catch(Exception e) {
					DeviceLog.exception("Couldn't get address. Host: " + configHost, e);
					cv.open();
				}
			}
		}.start();

		// This is checking if config url is in /etc/hosts or equivalent. No need for long wait.
		boolean success = cv.block(2000);
		if(success && _address != null && _address.isLoopbackAddress()) {
			DeviceLog.error("Unity Ads init: halting init because Unity Ads config resolves to loopback address (due to ad blocker?)");
			return false;
		}
		AdUnitOpen.setConfiguration(configuration);
		UnityAdsImplementation.setConfiguration(configuration);

		return true;
	}

	public boolean initErrorState(Configuration configuration, String state, String errorMessage) {
		return true;
	}

	public boolean initCompleteState(Configuration configuration) {
		AdUnitOpen.setConfiguration(configuration);
		UnityAdsImplementation.setConfiguration(configuration);
		return true;
	}

	public Map<String, Class> getAdUnitViewHandlers() {
		Map<String, Class> handlers = new HashMap<>();
		handlers.put("videoplayer", com.unity3d.services.ads.adunit.VideoPlayerHandler.class);
		handlers.put("webplayer", com.unity3d.services.ads.adunit.WebPlayerHandler.class);
		handlers.put("webview", com.unity3d.services.ads.adunit.WebViewHandler.class);

		return handlers;
	}
}
