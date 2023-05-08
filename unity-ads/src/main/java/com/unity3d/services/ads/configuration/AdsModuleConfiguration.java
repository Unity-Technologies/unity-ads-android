package com.unity3d.services.ads.configuration;

import android.os.ConditionVariable;

import com.unity3d.services.ads.UnityAdsImplementation;
import com.unity3d.services.ads.adunit.AdUnitOpen;
import com.unity3d.services.ads.token.AsyncTokenStorage;
import com.unity3d.services.ads.token.TokenStorage;
import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.configuration.ErrorState;
import com.unity3d.services.core.log.DeviceLog;
import com.unity3d.services.core.misc.Utilities;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AdsModuleConfiguration implements IAdsModuleConfiguration {
	private InetAddress _address;
	private final TokenStorage tokenStorage = Utilities.getService(TokenStorage.class);
	private final AsyncTokenStorage asyncTokenStorage = Utilities.getService(AsyncTokenStorage.class);

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
		tokenStorage.deleteTokens();
		asyncTokenStorage.setConfiguration(configuration);
		return true;
	}

	public boolean initErrorState(Configuration configuration, ErrorState state, String errorMessage) {
		tokenStorage.setInitToken(null);
		tokenStorage.deleteTokens();
		return true;
	}

	public boolean initCompleteState(Configuration configuration) {
		AdUnitOpen.setConfiguration(configuration);
		UnityAdsImplementation.setConfiguration(configuration);
		asyncTokenStorage.setConfiguration(configuration);
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
