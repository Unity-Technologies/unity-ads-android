package com.unity3d.services.ads.operation;


import com.unity3d.services.core.configuration.Configuration;
import com.unity3d.services.core.timer.BaseTimer;
import com.unity3d.services.core.webview.bridge.IWebViewSharedObject;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class OperationState  implements IWebViewSharedObject {
	private static String _emptyPlacementId = "";

	public String id;
	public String placementId;
	public Configuration configuration;
	public long startTime;
	public BaseTimer timeoutTimer;

	public OperationState(String placementId, Configuration configuration) {
		this.placementId = placementId == null ? _emptyPlacementId : placementId;
		this.configuration = configuration;
		id = UUID.randomUUID().toString();
	}

	@Override
	public String getId() {
		return id;
	}

	public void start() {
		startTime = System.nanoTime();
	}

	public long duration() {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
	}

}
