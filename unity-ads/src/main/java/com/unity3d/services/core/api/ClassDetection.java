package com.unity3d.services.core.api;

import com.unity3d.services.core.properties.MadeWithUnityDetector;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class ClassDetection {
	@WebViewExposed
	public static void isMadeWithUnity(WebViewCallback callback) {
		callback.invoke(MadeWithUnityDetector.isMadeWithUnity());
	}
}
