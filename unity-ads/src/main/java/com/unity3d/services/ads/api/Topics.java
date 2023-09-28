package com.unity3d.services.ads.api;

import com.unity3d.services.ads.topics.TopicsService;
import com.unity3d.services.core.misc.Utilities;
import com.unity3d.services.core.webview.bridge.WebViewCallback;
import com.unity3d.services.core.webview.bridge.WebViewExposed;

public class Topics {
	private static final TopicsService topicsService = Utilities.getService(TopicsService.class);

	@WebViewExposed
	public static void checkAvailability(WebViewCallback callback) {
		callback.invoke(topicsService.checkAvailability());
	}

	@WebViewExposed
	public static void getTopics(String adsSdkName, Boolean shouldRecordObservation, WebViewCallback callback) {
		topicsService.getTopics(adsSdkName, shouldRecordObservation);
		callback.invoke();
	}
}
