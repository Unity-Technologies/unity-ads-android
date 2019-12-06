package com.unity3d.services.core.api;

import com.unity3d.services.core.properties.ClientProperties;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

import org.json.JSONArray;

public class ClassDetection {
	@WebViewExposed
	public static void areClassesPresent(JSONArray classNames, WebViewCallback callback) {
		callback.invoke(ClientProperties.areClassesPresent(classNames));
	}
}
